package de.unknowncity.plots.service;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
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
import de.unknowncity.plots.data.dao.*;
import de.unknowncity.plots.plot.model.BuyPlot;
import de.unknowncity.plots.plot.model.Plot;
import de.unknowncity.plots.plot.model.RentPlot;
import de.unknowncity.plots.plot.SchematicManager;
import de.unknowncity.plots.plot.access.PlotState;
import de.unknowncity.plots.plot.model.PlotPlayer;
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
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

public class PlotService extends Service<PlotsPlugin> {
    private final QueryConfiguration queryConfiguration;
    private final PlotsPlugin plugin;

    private final Cache<String, Plot> plotCache = CacheBuilder.newBuilder().build();
    private final Cache<String, PlotGroup> plotGroupCache = CacheBuilder.newBuilder().build();
    private final Cache<PlotSign, Plot> plotSignCache = CacheBuilder.newBuilder().build();
    private final EconomyService economyService;

    private final FlagRegistry flagRegistry;
    private final SignManager signManager;
    private PlotDao plotDao;
    private PlotFlagDao plotFlagDao;
    private PlotInteractablesDao plotInteractablesDao;
    private PlotGroupDao plotGroupDao;
    private PlotLocationDao plotLocationDao;
    private PlotSignDao plotSignDao;
    private PlotMemberDao plotMemberDao;
    private PlotDeniedPlayerDao plotDeniedPlayerDao;

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

        this.plotDao = new PlotDao(queryConfiguration);
        this.plotFlagDao = new PlotFlagDao(queryConfiguration, flagRegistry);
        this.plotInteractablesDao = new PlotInteractablesDao(queryConfiguration);
        this.plotGroupDao = new PlotGroupDao(queryConfiguration);
        this.plotLocationDao = new PlotLocationDao(queryConfiguration);
        this.plotSignDao = new PlotSignDao(queryConfiguration);
        this.plotMemberDao = new PlotMemberDao(queryConfiguration);
        this.plotDeniedPlayerDao = new PlotDeniedPlayerDao(queryConfiguration);

        this.plugin.serviceRegistry().register(new BackupService(schematicManager, this));
    }

    public void cacheAll() {
        loadPlotCache();
    }

    /* Load everything in memory as we need access to all plots, members, flags, etc. when a location in
    the world is interacted with, to check if the interaction is allowed.
    For example, a member is allowed to build on a plot (if the access modifier is set to this value), but a non-member is not.
     */

    public void loadPlotCache() {
        var plotsFuture = CompletableFuture.supplyAsync(plotDao::readAll);
        var membersFuture = CompletableFuture.supplyAsync(plotMemberDao::readAll);
        var deniedFuture = CompletableFuture.supplyAsync(plotDeniedPlayerDao::readAll);
        var signsFuture = CompletableFuture.supplyAsync(plotSignDao::readAll);
        var warpsFuture = CompletableFuture.supplyAsync(plotLocationDao::readAll);
        var flagsFuture = CompletableFuture.supplyAsync(plotFlagDao::readAll);
        var interactablesFuture = CompletableFuture.supplyAsync(plotInteractablesDao::readAll);
        var groupsFuture = CompletableFuture.supplyAsync(plotGroupDao::readAll);

        CompletableFuture.allOf(
                plotsFuture,
                membersFuture,
                deniedFuture,
                signsFuture,
                warpsFuture,
                flagsFuture,
                interactablesFuture,
                groupsFuture
        ).thenRun(() -> {
            var plots = plotsFuture.join();
            plotCache.putAll(plots.stream().collect(Collectors.toMap(Plot::id, Function.identity())));

            var members = membersFuture.join();
            members.forEach(plotMember -> {
                var plot = plotCache.getIfPresent(plotMember.plotId());
                if (plot != null) {
                    plot.members().add(plotMember);
                }
            });

            var deniedPlayers = deniedFuture.join();
            deniedPlayers.forEach(plotPlayer -> {
                var plot = plotCache.getIfPresent(plotPlayer.plotId());
                if (plot != null) {
                    plot.deniedPlayers().add(plotPlayer);
                }
            });

            var signs = signsFuture.join();
            signs.forEach(plotSign -> {
                var plot = plotCache.getIfPresent(plotSign.plotId());
                if (plot != null) {
                    plot.signs().add(plotSign);

                    // Mapping between PlotSign and Plot for easy access when a player clicks a plot sign
                    plotSignCache.put(plotSign, plot);
                }
            });

            var warps = warpsFuture.join();
            warps.forEach(plotLocation -> {
                var plot = plotCache.getIfPresent(plotLocation.plotId());
                if (plot != null) {
                    plot.plotHome(plotLocation);
                }
            });

            var flags = flagsFuture.join();
            flags.forEach(plotFlagWrapper -> {
                var plot = plotCache.getIfPresent(plotFlagWrapper.plotId());
                if (plot != null) {
                    plot.setFlag(plotFlagWrapper.flag(), plotFlagWrapper.flagValue());
                }
            });

            var interactables = interactablesFuture.join();
            interactables.forEach(plotInteractable -> {
                var plot = plotCache.getIfPresent(plotInteractable.plotId());
                if (plot != null) {
                    plot.interactables().add(plotInteractable);
                }
            });

            var groups = groupsFuture.join();
            groups.forEach(plotGroup -> {
                plotGroupCache.put(plotGroup.name(), plotGroup);
                plotCache.asMap().values().forEach(plot -> {
                    if (plot.groupName() != null && plot.groupName().equals(plotGroup.name())) {
                        plotGroup.plotsInGroup().put(plot.id(), plot);
                    }
                });
            });
        });
    }


    public boolean existsPlot(String id) {
        return plotCache.getIfPresent(id) != null;
    }

    public boolean existsGroup(String id) {
        return plotGroupCache.getIfPresent(id) != null;
    }

    public boolean existsPlot(ProtectedRegion region) {
        return existsPlot(region.getId());
    }

    public void savePlotGroup(PlotGroup plotGroup) {
        CompletableFuture.runAsync(() -> plotGroupDao.write(plotGroup));
        plotGroupCache.put(plotGroup.name(), plotGroup);
        plotGroup.plotsInGroup().values().forEach(this::savePlot);
    }

    public void deletePlotGroup(String name) {
        var plotGroup = plotGroupCache.getIfPresent(name);
        if (plotGroup == null) {
            return;
        }
        deletePlotGroup(plotGroup);
    }

    public void deletePlotGroup(PlotGroup plotGroup) {
        plotGroup.plotsInGroup().values().forEach(plot -> {
            plot.groupName(null);
            savePlot(plot);
        });
        plotGroupCache.invalidate(plotGroup.name());
        plotGroupDao.delete(plotGroup.name());
    }

    public boolean createBuyPlotFromRegion(ProtectedRegion region, World world, double price, String plotGroupName) {
        var plotId = region.getId();
        if (existsPlot(plotId)) {
            return false;
        }
        var plot = new BuyPlot(plotId, null, plotGroupName,  region.getId(), price, world.getName(), PlotState.AVAILABLE, null);

        return createPlot(region, plot, plotGroupName);
    }

    public boolean createRentPlotFromRegion(ProtectedRegion region, World world, double price, String plotGroupName, Duration rentInterval) {
        var plotId = region.getId();
        if (existsPlot(plotId)) {
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
            var plotHome = new PlotLocation(plot.id(), plot.id(), true, location.x(), location.y(), location.z(), 0, 0);
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
            var plotGroup = plotGroupCache.getIfPresent(plotGroupName);
            if (plotGroup == null) {
                return;
            }
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
        plot.owner(new PlotPlayer(plot.id(), player.getUniqueId(), player.getName()));
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
        claimPlot(player, plot);
    }

    public void setPlotOwner(OfflinePlayer player, Plot plot) {
        plot.state(PlotState.SOLD);
        plot.owner(new PlotPlayer(plot.id(), player.getUniqueId(), player.getName()));
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

        // remove all associations with the old group
        if (plot.groupName() != null && !plot.groupName().isEmpty()) {
            var plotGroup = plotGroupCache.getIfPresent(plot.groupName());
            if (plotGroup != null) {
                plotGroup.plotsInGroup().remove(plot.id());
            }
        }
        var plotGroup = plotGroupCache.getIfPresent(plot.groupName());
        if (plotGroup != null) {
            plotGroup.plotsInGroup().put(plot.id(), plot);
        }

        savePlot(plot);
        SignManager.updateSings(plot, plugin.messenger());
    }

    public void createPlotGroup(String name) {
        var plotGroup = new PlotGroup(name);
        savePlotGroup(plotGroup);
        plotGroupCache.put(name, plotGroup);
    }

    public void savePlot(Plot plot) {
        CompletableFuture.runAsync(() -> {
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
        var plot = plotCache.getIfPresent(id);
        if (plot == null) {
            return;
        }
        deletePlot(plot);
    }

    public void deletePlot(Plot plot) {
        plotDao.delete(plot.id());
        plotCache.invalidate(plot.id());
        if (plot.groupName() != null) {
            var plotGroup = plotGroupCache.getIfPresent(plot.groupName());
            if (plotGroup == null) {
                return;
            }
            plotGroup.plotsInGroup().remove(plot.id());
        }
    }

    public PlotGroup getPlotGroupWithPlots(String name) {
        return plotGroupCache.getIfPresent(name);
    }

    public Plot getPlot(String id) {
        return plotCache.getIfPresent(id);
    }

    public PlotGroup getGroup(String id) {
        return plotGroupCache.getIfPresent(id);
    }

    public Plot getPlot(ProtectedRegion region) {
        return plotCache.getIfPresent(region.getId());
    }

    public Plot getPlotFromGroup(String id, String groupName) {
        return plotGroupCache.getIfPresent(groupName).plotsInGroup().get(id);
    }



    public List<Plot> findPlotsByOwnerUUID(UUID uuid) {
        return plotCache.asMap().values().stream().filter(plot -> plot.owner() != null && plot.owner().uuid()
                .equals(uuid)).sorted(Comparator.comparing(Plot::claimed)).toList();
    }

    public List<Plot> findAvailablePlots() {
        return plotCache.asMap().values().stream().filter(plot -> plot.state().equals(PlotState.AVAILABLE)).toList();
    }

    public Optional<Plot> getPlotForSignLocation(Location location) {
        var plotSign = new PlotSign(
                "",
                location.getBlockX(),
                location.getBlockY(),
                location.getBlockZ()
        );

        return plotSignCache.asMap().keySet().stream().anyMatch(sign -> sign.equals(plotSign)) ? Optional.of(plotSignCache.getIfPresent(plotSign)) : Optional.empty();
    }

    public Cache<String, PlotGroup> groupCache() {
        return plotGroupCache;
    }

    public Cache<String, Plot> plotCache() {
        return plotCache;
    }

    public Cache<PlotSign, Plot> plotSignCache() {
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