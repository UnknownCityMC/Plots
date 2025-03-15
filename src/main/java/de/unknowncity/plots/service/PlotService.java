package de.unknowncity.plots.service;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.BuiltInClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardWriter;
import com.sk89q.worldedit.extent.world.BiomeQuirkExtent;
import com.sk89q.worldedit.function.biome.BiomeReplace;
import com.sk89q.worldedit.function.biome.ExtentBiomeCopy;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.function.visitor.RegionVisitor;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import de.unknowncity.astralib.common.service.Service;
import de.unknowncity.plots.PlotsPlugin;
import de.unknowncity.plots.data.model.plot.*;
import de.unknowncity.plots.data.model.plot.flag.PlotInteractable;
import de.unknowncity.plots.data.model.plot.group.PlotGroup;
import de.unknowncity.plots.data.repository.PlotGroupRepository;
import de.unknowncity.plots.util.PlotId;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.spongepowered.configurate.NodePath;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;

public class PlotService implements Service<PlotsPlugin> {
    private final PlotGroupRepository plotGroupRepository;
    private final PlotsPlugin plugin;

    private final HashMap<String, Plot> plotCache = new HashMap<>();
    private final HashMap<String, PlotGroup> plotGroupCache = new HashMap<>();
    private final EconomyService economyService;

    public PlotService(PlotGroupRepository plotGroupRepository, EconomyService economyService, PlotsPlugin plugin) {
        this.plotGroupRepository = plotGroupRepository;
        this.economyService = economyService;
        this.plugin = plugin;
    }

    @Override
    public void shutdown() {

    }

    public void cacheAll() {
        plotGroupRepository.loadPlotCache().whenComplete((plotCache, thr1) -> {
            this.plotCache.putAll(plotCache);
            plotGroupRepository.loadPlotGroupCache(plotCache).whenComplete((plotGroupCache, thr2) -> {
                this.plotGroupCache.putAll(plotGroupCache);
            });
        });
    }

    public boolean existsPlot(String id) {
        return plotCache.containsKey(id);
    }

    public boolean existsGroup(String id) {
        return plotGroupCache.containsKey(id);
    }

    public boolean existsPlot(ProtectedRegion region, World world) {
        return existsPlot(PlotId.generate(world, region));
    }

    public boolean createBuyPlotFromRegion(ProtectedRegion region, World world, double price, String plotGroupName) {
        var plotId = PlotId.generate(world, region);
        if (plotCache.containsKey(plotId)) {
            return false;
        }
        var plot = new BuyPlot(plotId, null, plotGroupName, region.getId(), price, world.getName(), PlotState.AVAILABLE);

        createPlot(region, plot, plotGroupName);
        return true;
    }

    public boolean createRentPlotFromRegion(ProtectedRegion region, World world, double price, String plotGroupName, Duration rentInterval) {
        var plotId = PlotId.generate(world, region);
        if (plotCache.containsKey(plotId)) {
            return false;
        }
        var plot = new RentPlot(plotId, null, plotGroupName, region.getId(), price, world.getName(), PlotState.AVAILABLE, null, rentInterval.toMinutes());

        createPlot(region, plot, plotGroupName);
        return true;
    }

    private void createPlot(ProtectedRegion region, Plot plot, String plotGroupName) {
        addPlotToPlotGroup(plot, plotGroupName);

        region.setFlag(Flags.INTERACT, StateFlag.State.ALLOW);
        region.setFlag(Flags.USE, StateFlag.State.ALLOW);

        plot.interactables(PlotInteractable.defaults());

        savePlot(plot);
    }

    public void setBiome(Plot plot, BiomeType biome) {
        var world = plot.world();

        try (EditSession editSession = WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(world))) {
            var region = new CuboidRegion(plot.protectedRegion().getMinimumPoint(), plot.protectedRegion().getMaximumPoint());

            var replace = new BiomeReplace(editSession, biome);
            var visitor = new RegionVisitor(region, replace);
            Operations.complete(visitor);
        } catch (WorldEditException e) {
            plugin.getLogger().warning("Failed to change biome for plot " + plot.id());
        }
    }

    public void addPlotToPlotGroup(Plot plot, String plotGroupName) {
        if (plotGroupName != null) {
            var plotGroup = plotGroupCache.get(plotGroupName);
            plotGroup.plotsInGroup().put(plot.id(), plot);
            savePlotGroup(plotGroup);
        }
    }

    public void claimPlot(Player player, Plot plot) {
        economyService.withdraw(player.getUniqueId(), plot.price());
        if (plot instanceof RentPlot rentPlot) {
            rentPlot.lastRentPayed(LocalDateTime.now());
        }

        plot.state(PlotState.SOLD);
        plot.owner(player.getUniqueId());
        savePlot(plot);
        createSchematic(plot);
    }

    public void unClaimPlot(Plot plot) {
        economyService.deposit(plot.owner(), plot.price());

        plot.state(PlotState.AVAILABLE);
        plot.owner(null);
        plot.flags(new ArrayList<>());
        plot.members(new ArrayList<>());
        savePlot(plot);
        loadSchematic(plot);
    }

    public void addMember(OfflinePlayer player, PlotMemberRole role, Plot plot) {
        plot.members().add(new PlotMember(player.getUniqueId(), role, player.getName()));
        savePlot(plot);
    }

    public void removeMember(OfflinePlayer player, Plot plot) {
        plot.members().removeIf(plotMember -> plotMember.memberID().equals(player.getUniqueId()));
        savePlot(plot);
    }

    public void setPlotOwner(Player player, Plot plot) {
        plot.state(PlotState.SOLD);
        plot.owner(player.getUniqueId());
        savePlot(plot);
    }

    public void setPlotGroup(String groupName, Plot plot) {
        plot.state(PlotState.SOLD);
        if (plot.groupName() != null && !plot.groupName().isEmpty()) {
            plotGroupCache.get(plot.groupName()).plotsInGroup().remove(plot.id());
        }
        plot.groupName(groupName);
        plotGroupCache.get(groupName).plotsInGroup().put(plot.id(), plot);
        savePlot(plot);
    }

    public void createPlotGroup(String name) {
        var plotGroup = new PlotGroup(name);
        plotGroupRepository.savePlotGroup(plotGroup);
        plotGroupCache.put(name, plotGroup);
    }

    public void deletePlotGroup(String name) {
        var plotGroup = plotGroupCache.get(name);
        plotGroupRepository.deletePlotGroup(plotGroup);
        this.deletePlotGroup(plotGroup);
    }

    public void savePlotGroup(PlotGroup plotGroup) {
        plotGroupRepository.savePlotGroup(plotGroup);
        plotGroupCache.put(plotGroup.name(), plotGroup);
    }

    public void savePlot(Plot plot) {
        plotGroupRepository.savePlot(plot);
        plotCache.put(plot.id(), plot);
    }

    public void deletePlot(String id) {
        var plot = plotCache.get(id);
        deletePlot(plot);
    }

    public void deletePlot(Plot plot) {
        plotGroupRepository.deletePlot(plot);
        plotCache.remove(plot.id());
        if (plot.groupName() != null) {
            plotGroupCache.get(plot.groupName()).plotsInGroup().remove(plot.id());
        }
    }

    public void deletePlotGroup(PlotGroup plotGroup) {
        plotGroup.plotsInGroup().values().forEach(plot -> {
            plot.groupName(null);
            savePlot(plot);
        });
        plotGroupRepository.deletePlotGroup(plotGroup);
        plotGroupCache.remove(plotGroup.name());
    }


    public PlotGroup getPlotGroupWithPlots(String name) {
        return plotGroupCache.get(name);
    }

    public Plot getPlot(String id) {
        return plotCache.get(id);
    }

    public PlotGroup getGroup(String id) {
        return plotGroupCache.get(id);
    }

    public Plot getPlot(World world, ProtectedRegion region) {
        return plotCache.get(PlotId.generate(world, region));
    }

    public Plot getPlotFromGroup(String id, String groupName) {
        return plotGroupCache.get(groupName).plotsInGroup().get(id);
    }

    public void createSchematic(Plot plot) {
        CuboidRegion region = new CuboidRegion(plot.protectedRegion().getMinimumPoint(), plot.protectedRegion().getMaximumPoint());
        BlockArrayClipboard clipboard = new BlockArrayClipboard(region);

        ForwardExtentCopy forwardExtentCopy = new ForwardExtentCopy(
                BukkitAdapter.adapt(plot.world()), region, clipboard, region.getMinimumPoint()
        );

        try {
            Operations.complete(forwardExtentCopy);
        } catch (WorldEditException e) {
            plugin.getLogger().log(Level.SEVERE, e.getMessage());
        }

        File file = new File(plugin.getDataPath() + "/schematics/" + plot.id() + ".schem");
        try (ClipboardWriter writer = BuiltInClipboardFormat.SPONGE_V3_SCHEMATIC.getWriter(new FileOutputStream(file))) {
            writer.write(clipboard);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, e.getMessage());
        }
    }

    public void loadSchematic(Plot plot) {
        File file = new File(plugin.getDataPath() + "/schematics/" + plot.id() + ".schem");
        ClipboardFormat format = BuiltInClipboardFormat.SPONGE_V3_SCHEMATIC;
        Clipboard clipboard;
        try (ClipboardReader reader = format.getReader(new FileInputStream(file))) {
            clipboard = reader.read();
            EditSession editSession = WorldEdit.getInstance().newEditSession((BukkitAdapter.adapt(plot.world())));
            Operation operation = new ClipboardHolder(clipboard)
                    .createPaste(editSession)
                    .to(clipboard.getOrigin())
                    .ignoreAirBlocks(false)
                    .build();
            Operations.complete(operation);
            editSession.close();
        } catch (WorldEditException | IOException e) {
            plugin.getLogger().log(Level.SEVERE, e.getMessage());
        }

    }

    public HashMap<String, PlotGroup> groupCache() {
        return plotGroupCache;
    }

    public HashMap<String, Plot> plotCache() {
        return plotCache;
    }
}
