package de.unknowncity.plots.service;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.function.biome.BiomeReplace;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.function.visitor.RegionVisitor;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import de.chojo.sadu.queries.api.configuration.QueryConfiguration;
import de.unknowncity.astralib.common.service.Service;
import de.unknowncity.plots.PlotsPlugin;
import de.unknowncity.plots.data.dao.mariadb.*;
import de.unknowncity.plots.plot.BuyPlot;
import de.unknowncity.plots.plot.Plot;
import de.unknowncity.plots.plot.RentPlot;
import de.unknowncity.plots.plot.SchematicManager;
import de.unknowncity.plots.plot.access.PlotState;
import de.unknowncity.plots.plot.access.entity.PlotPlayer;
import de.unknowncity.plots.plot.flag.FlagRegistry;
import de.unknowncity.plots.plot.flag.PlotFlags;
import de.unknowncity.plots.plot.flag.PlotInteractable;
import de.unknowncity.plots.plot.group.PlotGroup;
import de.unknowncity.plots.plot.location.PlotLocation;
import de.unknowncity.plots.plot.location.signs.PlotSign;
import de.unknowncity.plots.plot.location.signs.SignManager;
import de.unknowncity.plots.service.backup.BackupService;
import de.unknowncity.plots.util.LocationUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.io.File;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

public class PlotService extends Service<PlotsPlugin> {
    private final QueryConfiguration queryConfiguration;
    private final PlotsPlugin plugin;

    private final HashMap<String, Plot> plotCache = new HashMap<>();
    private final HashMap<String, PlotGroup> plotGroupCache = new HashMap<>();
    private final HashMap<PlotSign, Plot> plotSignCache = new HashMap<>();
    private final EconomyService economyService;

    private final FlagRegistry flagRegistry;
    private final SignManager signManager;
    private MariaDBPlotDao plotDao;
    private MariaDBPlotFlagDao plotFlagDao;
    private MariaDBPlotInteractablesDao plotInteractablesDao;
    private MariaDBGroupDao plotGroupDao;
    private MariaDBPlotLocationDao plotLocationDao;
    private MariaDBPlotSignDao plotSignDao;
    private MariaDBPlotMemberDao plotMemberDao;
    private MariaDBPlotDeniedPlayerDao plotDeniedPlayerDao;

    private final SchematicManager schematicManager;

    public PlotService(QueryConfiguration queryConfiguration, EconomyService economyService, PlotsPlugin plugin) {
        this.flagRegistry = new FlagRegistry(plugin);
        this.queryConfiguration = queryConfiguration;
        this.economyService = economyService;
        this.plugin = plugin;
        this.schematicManager = new SchematicManager(plugin);

        this.signManager = new SignManager(plugin);
    }

    @Override
    public void startup() {
        PlotFlags.registerAllFlags(this);

        schematicManager.makeDirectories();

        this.plotDao = new MariaDBPlotDao(queryConfiguration);
        this.plotFlagDao = new MariaDBPlotFlagDao(queryConfiguration, flagRegistry);
        this.plotInteractablesDao = new MariaDBPlotInteractablesDao(queryConfiguration);
        this.plotGroupDao = new MariaDBGroupDao(queryConfiguration);
        this.plotLocationDao = new MariaDBPlotLocationDao(queryConfiguration);
        this.plotSignDao = new MariaDBPlotSignDao(queryConfiguration);
        this.plotMemberDao = new MariaDBPlotMemberDao(queryConfiguration);
        this.plotDeniedPlayerDao = new MariaDBPlotDeniedPlayerDao(queryConfiguration);

        this.plugin.serviceRegistry().register(new BackupService(schematicManager, this));
    }

    public void cacheAll() {
        loadPlotCache();
    }

    public void loadPlotCache() {
        plotDao.readAll().whenComplete((plots, throwable) -> {
            plots.forEach(plot -> {
                plotFlagDao.readAll(plot.id()).thenAccept(plotFlagWrappers -> {
                    var updatedflags = new ArrayList<>(flagRegistry.getAllRegistered());
                    plotFlagWrappers.forEach(plotFlagWrapper -> {
                        updatedflags.removeIf(flag -> flag.flagId().equals(plotFlagWrapper.flag().flagId()));
                        plot.setFlag(plotFlagWrapper.flag(), plotFlagWrapper.flagValue());
                    });

                    updatedflags.forEach(plotFlag -> {
                        plot.setFlag(plotFlag, plotFlag.defaultValue());
                    });
                });
                plotMemberDao.readAll(plot.id()).thenAccept(plot::members);
                plotDeniedPlayerDao.readAll(plot.id()).thenAccept(plot::deniedPlayers);
                plotLocationDao.read(plot.id()).thenAccept(plotLocation -> plotLocation.ifPresent(plot::plotHome));
                plotSignDao.readAll(plot.id()).thenAccept(plot::signs);
                plotInteractablesDao.readAll(plot.id()).thenAccept(plotInteractables -> {
                    var updatedInteractables = new ArrayList<>(PlotInteractable.defaults());
                    plotInteractables.forEach(interactable -> {
                        updatedInteractables.removeIf(pi -> pi.blockType() == interactable.blockType());
                    });
                    updatedInteractables.addAll(plotInteractables);
                    plot.interactables(updatedInteractables);
                });
                plotCache.put(plot.id(), plot);
            });
        }).thenRun(() -> {
            loadPlotGroupCache(plotCache);
            loadPlotSignCache(plotCache);
        });
    }

    public void loadPlotGroupCache(HashMap<String, Plot> plotCache) {
        plotGroupDao.readAll().whenComplete((plotGroups, throwable) -> {
            plotGroups.forEach(plotGroup -> {
                var plots = new HashMap<String, Plot>();
                plotCache.values().forEach(plot -> {
                    if (plot.groupName() != null && plot.groupName().equals(plotGroup.name())) {
                        plots.put(plot.id(), plot);
                    }
                });
                plotGroup.plotsInGroup(plots);
                plotGroupCache.put(plotGroup.name(), plotGroup);
            });
        });
    }

    private void loadPlotSignCache(HashMap<String, Plot> plotCache) {
        plotCache.forEach((id, plot) -> {
            plot.signs().forEach(plotSign -> {
                plotSignCache.put(plotSign, plot);
            });
        });
    }

    public boolean existsPlot(String id) {
        return plotCache.containsKey(id);
    }

    public boolean existsGroup(String id) {
        return plotGroupCache.containsKey(id);
    }

    public boolean existsPlot(ProtectedRegion region) {
        return existsPlot(region.getId());
    }

    public void savePlotGroup(PlotGroup plotGroup) {
        plotGroupDao.write(plotGroup);
        plotGroupCache.put(plotGroup.name(), plotGroup);
        plotGroup.plotsInGroup().values().forEach(this::savePlot);
    }

    public void deletePlotGroup(String name) {
        var plotGroup = plotGroupCache.get(name);
        deletePlotGroup(plotGroup);
    }

    public void deletePlotGroup(PlotGroup plotGroup) {
        plotGroup.plotsInGroup().values().forEach(plot -> {
            plot.groupName(null);
            savePlot(plot);
        });
        plotGroupCache.remove(plotGroup.name());
        plotGroupDao.delete(plotGroup.name());
    }

    public boolean createBuyPlotFromRegion(ProtectedRegion region, World world, double price, String plotGroupName) {
        var plotId = region.getId();
        if (plotCache.containsKey(plotId)) {
            return false;
        }
        var plot = new BuyPlot(plotId, null, plotGroupName,  region.getId(), price, world.getName(), PlotState.AVAILABLE, null);

        return createPlot(region, plot, plotGroupName);
    }

    public boolean createRentPlotFromRegion(ProtectedRegion region, World world, double price, String plotGroupName, Duration rentInterval) {
        var plotId = region.getId();
        if (plotCache.containsKey(plotId)) {
            return false;
        }
        var plot = new RentPlot(plotId, null, plotGroupName, region.getId(), price, world.getName(), PlotState.AVAILABLE, null, null, rentInterval.toMinutes());

        return createPlot(region, plot, plotGroupName);
    }

    private boolean createPlot(ProtectedRegion region, Plot plot, String plotGroupName) {
        addPlotToPlotGroup(plot, plotGroupName);

        region.setFlag(Flags.INTERACT, StateFlag.State.ALLOW);
        region.setFlag(Flags.USE, StateFlag.State.ALLOW);

        flagRegistry.getAllRegistered().forEach(plotFlag -> plot.setFlag(plotFlag, plotFlag.defaultValue()));

        var optLocation = LocationUtil.findSuitablePlotLocation(plot.world(), region);
        if (optLocation.isPresent()) {
            var location = optLocation.get();
            var plotHome = new PlotLocation(plot.id(), true, location.x(), location.y(), location.z(), 0, 0);
            plot.plotHome(plotHome);
        } else {
            return false;
        }

        plot.interactables(PlotInteractable.defaults());

        savePlot(plot);
        return true;
    }

    public void setBiome(Plot plot, BiomeType biome) {
        var world = plot.world();
        final var biomeExtend = 3;

        try (EditSession editSession = WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(world))) {
            var region = new CuboidRegion(plot.protectedRegion().getMinimumPoint(), plot.protectedRegion().getMaximumPoint());
            region.expand(new BlockVector3(biomeExtend, 0, 0));
            region.expand(new BlockVector3(-biomeExtend, 0, 0));
            region.expand(new BlockVector3(0, 0, biomeExtend));
            region.expand(new BlockVector3(0, 0, -biomeExtend));

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
        plot.owner(new PlotPlayer(player.getUniqueId(), player.getName()));
        savePlot(plot);

        if (!plugin.configuration().fb().noSchematic().contains(plot.world().getName())) {
            schematicManager.createPreSaleSchematic(plot);
        }

        SignManager.updateSings(plot, plugin.messenger());
    }

    public void unClaimPlot(Plot plot) {
        economyService.deposit(plot.owner().uuid(), plot.price());

        resetPlot(plot);
    }

    public boolean backup(Plot plot, UUID owner) {
        return schematicManager.createSchematicBackup(plot, owner);
    }

    public boolean hasBackup(Plot plot, UUID owner) {
        var path = "/schematics/backups/" + owner.toString() + "_" + plot.id() + ".schem";
        File file = new File(plugin.getDataPath() + path);
        return file.exists();
    }

    public void loadBackupForPlayer(Plot plot, Player player) {
        economyService.withdraw(player.getUniqueId(), plot.price());
        if (plot instanceof RentPlot rentPlot) {
            rentPlot.lastRentPayed(LocalDateTime.now());
        }

        plot.state(PlotState.SOLD);
        plot.owner(new PlotPlayer(player.getUniqueId(), player.getName()));
        savePlot(plot);
        if (!plugin.configuration().fb().noSchematic().contains(plot.world().getName())) {
            schematicManager.createPreSaleSchematic(plot);
        }
        schematicManager.pasteOwnedBackupSchematic(plot, player.getUniqueId());
    }

    public void setPlotOwner(OfflinePlayer player, Plot plot) {
        plot.state(PlotState.SOLD);
        plot.owner(new PlotPlayer(player.getUniqueId(), player.getName()));
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
        savePlotGroup(plotGroup);
        plotGroupCache.put(name, plotGroup);
    }

    public void savePlot(Plot plot) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            plotDao.write(plot);

            plot.flags().forEach((plotFlag, value) -> plotFlagDao.write(plot.id(), plotFlag.flagId(), plotFlag.marshall(value)));
            plot.interactables().forEach(plotInteractable -> plotInteractablesDao.write(plotInteractable, plot.id()));
            plot.members().forEach(plotMember -> plotMemberDao.write(plotMember, plot.id()));
            plot.deniedPlayers().forEach(plotDeniedPlayer -> plotDeniedPlayerDao.write(plotDeniedPlayer, plot.id()));
            plotLocationDao.write(plot.plotHome(), plot.id());
            plotSignDao.deleteAll(plot.id());
            plotSignDao.writeAll(plot.signs(), plot.id());
            plotCache.put(plot.id(), plot);
        });

        SignManager.updateSings(plot, plugin.messenger());
    }

    public Optional<Plot> findPlotAt(Location location) {
        var regionService = plugin.serviceRegistry().getRegistered(RegionService.class);
        var possibleRegion = regionService.getSuitableRegion(location);

        if (possibleRegion.isEmpty()) {
            return Optional.empty();
        }

        var plotId = possibleRegion.get().getId();

        if (!existsPlot(plotId)) {
            return Optional.empty();
        }

        return Optional.of(getPlot(plotId));
    }

    public Optional<Plot> findPlotForRegion(ProtectedRegion region) {
        var plotId = region.getId();
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
        plotDao.delete(plot.id());
        plotCache.remove(plot.id());
        if (plot.groupName() != null) {
            plotGroupCache.get(plot.groupName()).plotsInGroup().remove(plot.id());
        }
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

    public Plot getPlot(ProtectedRegion region) {
        return plotCache.get(region.getId());
    }

    public Plot getPlotFromGroup(String id, String groupName) {
        return plotGroupCache.get(groupName).plotsInGroup().get(id);
    }



    public List<Plot> findPlotsByOwnerUUID(UUID uuid) {
        return plotCache.values().stream().filter(plot -> plot.owner() != null && plot.owner().uuid()
                .equals(uuid)).sorted(Comparator.comparing(Plot::claimed)).toList();
    }

    public List<Plot> findAvailablePlots() {
        return plotCache.values().stream().filter(plot -> plot.state().equals(PlotState.AVAILABLE)).toList();
    }

    public Optional<Plot> getPlotForSignLocation(Location location) {
        var plotSign = new PlotSign(
                location.getBlockX(),
                location.getBlockY(),
                location.getBlockZ()
        );

        return plotSignCache.keySet().stream().anyMatch(sign -> sign.equals(plotSign))? Optional.of(plotSignCache.get(plotSign)) : Optional.empty();
    }

    public HashMap<String, PlotGroup> groupCache() {
        return plotGroupCache;
    }

    public HashMap<String, Plot> plotCache() {
        return plotCache;
    }

    public HashMap<PlotSign, Plot> plotSignCache() {
        return plotSignCache;
    }

    public FlagRegistry flagRegistry() {
        return flagRegistry;
    }

    public PlotsPlugin plugin() {
        return plugin;
    }

    public SignManager signManager() {
        return signManager;
    }

    public void resetPlot(Plot plot) {
        plot.state(PlotState.AVAILABLE);
        plot.owner(null);
        flagRegistry.getAllRegistered().forEach(plotFlag -> {
            plot.setFlag(plotFlag, plotFlag.defaultValue());
        });
        plot.members().clear();
        plot.deniedPlayers().clear();
        savePlot(plot);

        if (!plugin.configuration().fb().noSchematic().contains(plot.world().getName())) {
            schematicManager.pastePresaleSchematic(plot);
        }

        SignManager.updateSings(plot, plugin.messenger());
    }
}