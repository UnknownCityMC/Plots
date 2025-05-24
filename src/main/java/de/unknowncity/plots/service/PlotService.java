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
import com.sk89q.worldedit.function.biome.BiomeReplace;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.function.visitor.RegionVisitor;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import de.chojo.sadu.queries.api.configuration.QueryConfiguration;
import de.unknowncity.astralib.common.service.Service;
import de.unknowncity.plots.PlotsPlugin;
import de.unknowncity.plots.data.dao.mariadb.*;
import de.unknowncity.plots.data.repository.PlotGroupRepository;
import de.unknowncity.plots.plot.BuyPlot;
import de.unknowncity.plots.plot.Plot;
import de.unknowncity.plots.plot.RentPlot;
import de.unknowncity.plots.plot.access.PlotState;
import de.unknowncity.plots.plot.access.entity.PlotMember;
import de.unknowncity.plots.plot.access.type.PlotMemberRole;
import de.unknowncity.plots.plot.flag.FlagRegistry;
import de.unknowncity.plots.plot.flag.PlotFlags;
import de.unknowncity.plots.plot.flag.PlotInteractable;
import de.unknowncity.plots.plot.group.PlotGroup;
import de.unknowncity.plots.plot.location.PlotLocation;
import de.unknowncity.plots.plot.location.signs.SignManager;
import de.unknowncity.plots.util.LocationUtil;
import de.unknowncity.plots.util.PlotId;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.logging.Level;

public class PlotService extends Service<PlotsPlugin> {
    private final QueryConfiguration queryConfiguration;
    private final PlotsPlugin plugin;

    private final HashMap<String, Plot> plotCache = new HashMap<>();
    private final HashMap<String, PlotGroup> plotGroupCache = new HashMap<>();
    private final EconomyService economyService;

    private final FlagRegistry flagRegistry;
    private final SignManager signManager;
    private PlotGroupRepository plotGroupRepository;

    public PlotService(QueryConfiguration queryConfiguration, EconomyService economyService, PlotsPlugin plugin) {
        this.flagRegistry = new FlagRegistry(plugin);
        this.queryConfiguration = queryConfiguration;
        this.economyService = economyService;
        this.plugin = plugin;

        this.signManager = new SignManager(plugin);
    }

    @Override
    public void startup() {
        PlotFlags.registerAllFlags(this);
        var plotDao = new MariaDBPlotDao(queryConfiguration);
        var plotFlagDao = new MariaDBPlotFlagDao(queryConfiguration, flagRegistry);
        var plotInteractablesDao = new MariaDBPlotInteractablesDao(queryConfiguration);
        var plotGroupDao = new MariaDBGroupDao(queryConfiguration);
        var plotLocationDao = new MariaDBPlotLocationDao(queryConfiguration);
        var plotSignDao = new MariaDBPlotSignDao(queryConfiguration);
        var plotMemberDao = new MariaDBPlotMemberDao(queryConfiguration);

        this.plotGroupRepository = new PlotGroupRepository(
                plotGroupDao, plotDao, plotFlagDao, plotInteractablesDao, plotLocationDao, plotSignDao, plotMemberDao
        );


    }

    public void cacheAll() {
        plotGroupRepository.loadPlotCache().whenComplete((plotCache, thr1) -> {
            this.plotCache.putAll(plotCache);
            plotGroupRepository.loadPlotGroupCache(plotCache).whenComplete((plotGroupCache, thr2) -> {
                this.plotGroupCache.putAll(plotGroupCache);
            }).whenComplete((stringPlotGroupHashMap, throwable) -> signManager.collectGarbage());
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
        var plot = new BuyPlot(plotId, plotGroupName, null, region.getId(), price, world.getName(), PlotState.AVAILABLE, null);

        createPlot(region, plot, plotGroupName);
        return true;
    }

    public boolean createRentPlotFromRegion(ProtectedRegion region, World world, double price, String plotGroupName, Duration rentInterval) {
        var plotId = PlotId.generate(world, region);
        if (plotCache.containsKey(plotId)) {
            return false;
        }
        var plot = new RentPlot(plotId, null, plotGroupName, region.getId(), price, world.getName(), PlotState.AVAILABLE, null, null, rentInterval.toMinutes());

        createPlot(region, plot, plotGroupName);
        return true;
    }

    private void createPlot(ProtectedRegion region, Plot plot, String plotGroupName) {
        addPlotToPlotGroup(plot, plotGroupName);

        region.setFlag(Flags.INTERACT, StateFlag.State.ALLOW);
        region.setFlag(Flags.USE, StateFlag.State.ALLOW);

        flagRegistry.getAllRegistered().forEach(plotFlag -> plot.setFlag(plotFlag, plotFlag.defaultValue()));

        var optLocation = LocationUtil.findSuitablePlotLocation(plot.world(), region);
        if (optLocation.isPresent()) {
            var location = optLocation.get();
            var plotHome = new PlotLocation(plot.id(), true, location.x(), location.y(), location.z(), 0, 0);
            plot.plotHome(plotHome);
        }

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
            plot.groupName(plotGroupName);
            savePlot(plot);
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

        if (!plugin.configuration().fb().noSchematic().contains(plot.world().getName())) {
            createSchematic(plot);
        }

        SignManager.updateSings(plot, plugin.messenger());
    }

    public void unClaimPlot(Plot plot) {
        economyService.deposit(plot.owner(), plot.price());

        plot.state(PlotState.AVAILABLE);
        plot.owner(null);
        flagRegistry.getAllRegistered().forEach(plotFlag -> {
            plot.setFlag(plotFlag, plotFlag.defaultValue());
        });
        plot.members(new ArrayList<>());
        savePlot(plot);
        if (!plugin.configuration().fb().noSchematic().contains(plot.world().getName())) {
            loadSchematic(plot);
        }

        SignManager.updateSings(plot, plugin.messenger());
    }

    public boolean backup(Plot plot, UUID owner) {
        return createSchematicBackup(plot, owner);
    }

    public boolean hasBackup(Plot plot, UUID owner) {
        var path = "/schematics/backups/" + owner.toString() + "_" + plot.id() + ".schem";
        File file = new File(plugin.getDataPath() + path);
        return file.exists();
    }

    public void loadBackup(Plot plot, Player player) {
        economyService.withdraw(player.getUniqueId(), plot.price());
        if (plot instanceof RentPlot rentPlot) {
            rentPlot.lastRentPayed(LocalDateTime.now());
        }

        plot.state(PlotState.SOLD);
        plot.owner(player.getUniqueId());
        savePlot(plot);
        if (!plugin.configuration().fb().noSchematic().contains(plot.world().getName())) {
            createSchematic(plot);
        }
        loadSchematicBackup(plot, player.getUniqueId());
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
        SignManager.updateSings(plot, plugin.messenger());
    }

    public void setPlotPrice(double price, Plot plot) {
        plot.price(price);
        savePlot(plot);
        SignManager.updateSings(plot, plugin.messenger());
    }

    public void setPlotGroup(String groupName, Plot plot) {
        plot.state(PlotState.SOLD);
        if (plot.groupName() != null && !plot.groupName().isEmpty()) {
            plotGroupCache.get(plot.groupName()).plotsInGroup().remove(plot.id());
        }
        plot.groupName(groupName);
        plotGroupCache.get(groupName).plotsInGroup().put(plot.id(), plot);
        savePlot(plot);
        SignManager.updateSings(plot, plugin.messenger());
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
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            plotGroupRepository.savePlot(plot);
            plotCache.put(plot.id(), plot);
        });
    }

    public Optional<Plot> findPlotAt(Location location) {
        var regionService = plugin.serviceRegistry().getRegistered(RegionService.class);
        var possibleRegion = regionService.getSuitableRegion(location);

        if (possibleRegion.isEmpty()) {
            return Optional.empty();
        }

        var plotId = PlotId.generate(location.getWorld(), possibleRegion.get());

        if (!existsPlot(plotId)) {
            return Optional.empty();
        }

        return Optional.of(getPlot(plotId));
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
        createWorldEditSchematic(plot, "/schematics/");
    }


    public boolean createSchematicBackup(Plot plot, UUID owner) {
        return createWorldEditSchematic(plot, "/schematics/backups/" + owner.toString() + "_");
    }

    private boolean createWorldEditSchematic(Plot plot, String path) {
        CuboidRegion region = new CuboidRegion(plot.protectedRegion().getMinimumPoint(), plot.protectedRegion().getMaximumPoint());
        BlockArrayClipboard clipboard = new BlockArrayClipboard(region);

        ForwardExtentCopy forwardExtentCopy = new ForwardExtentCopy(
                BukkitAdapter.adapt(plot.world()), region, clipboard, region.getMinimumPoint()
        );

        try {
            Operations.complete(forwardExtentCopy);
        } catch (WorldEditException e) {
            plugin.getLogger().log(Level.SEVERE, e.getMessage());
            return false;
        }

        File file = new File(plugin.getDataPath() + path + plot.id() + ".schem");
        try (ClipboardWriter writer = BuiltInClipboardFormat.SPONGE_V3_SCHEMATIC.getWriter(new FileOutputStream(file))) {
            writer.write(clipboard);
            return true;
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, e.getMessage());
            return false;
        }
    }

    public void loadSchematic(Plot plot) {
        loadWorldEditSchematic(plot, "/schematics/");
    }

    public void loadSchematicBackup(Plot plot, UUID owner) {
        loadWorldEditSchematic(plot, "/schematics/backups/" + owner.toString() + "_");
    }

    public void loadWorldEditSchematic(Plot plot, String path) {
        File file = new File(plugin.getDataPath() + path + plot.id() + ".schem");
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

    public List<Plot> findPlotsByOwnerUUID(UUID uuid) {
        return plotCache.values().stream().filter(plot -> plot.owner().equals(uuid)).sorted(Comparator.comparing(Plot::claimed)).toList();
    }

    public HashMap<String, PlotGroup> groupCache() {
        return plotGroupCache;
    }

    public HashMap<String, Plot> plotCache() {
        return plotCache;
    }

    public FlagRegistry flagRegistry() {
        return flagRegistry;
    }

    public PlotGroupRepository plotGroupRepository() {
        return plotGroupRepository;
    }

    public PlotsPlugin plugin() {
        return plugin;
    }

    public SignManager signManager() {
        return signManager;
    }
}
