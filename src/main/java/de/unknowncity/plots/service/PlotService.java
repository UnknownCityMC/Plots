package de.unknowncity.plots.service;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import de.chojo.sadu.queries.api.configuration.QueryConfiguration;
import de.unknowncity.astralib.common.service.Service;
import de.unknowncity.plots.PlotsPlugin;
import de.unknowncity.plots.event.PlotClaimPlayerEvent;
import de.unknowncity.plots.event.PlotSellPlayerEvent;
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
import de.unknowncity.plots.plot.location.signs.SignManager;
import de.unknowncity.plots.service.backup.BackupService;
import de.unknowncity.plots.service.plot.*;
import de.unknowncity.plots.util.LocationUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class PlotService extends Service<PlotsPlugin> {
    private final PlotsPlugin plugin;
    private final FlagRegistry flagRegistry;
    private final QueryConfiguration queryConfiguration;

    private final Cache<String, Plot> plotCache = CacheBuilder.newBuilder().build();
    private final Cache<String, PlotGroup> plotGroupCache = CacheBuilder.newBuilder().build();
    private final Logger logger = JavaPlugin.getPlugin(PlotsPlugin.class).getLogger();

    private final PlotDao plotDao;
    private final PlotGroupDao groupDao;

    private final PlotMemberDao memberDao;
    private final PlotDeniedPlayerDao deniedDao;
    private final PlotSignDao signDao;
    private final PlotLocationDao locationDao;
    private final PlotFlagDao flagDao;
    private final PlotInteractablesDao interactablesDao;

    private final SchematicManager schematicManager;

    public PlotService(PlotsPlugin plugin, FlagRegistry flagRegistry, SchematicManager schematicManager, QueryConfiguration queryConfiguration) {
        this.plugin = plugin;
        this.flagRegistry = flagRegistry;
        this.queryConfiguration = queryConfiguration;
        this.schematicManager = schematicManager;

        this.plotDao = new PlotDao(queryConfiguration);
        this.groupDao = new PlotGroupDao(queryConfiguration);
        this.memberDao = new PlotMemberDao(queryConfiguration);
        this.deniedDao = new PlotDeniedPlayerDao(queryConfiguration);
        this.signDao = new PlotSignDao(queryConfiguration);
        this.locationDao = new PlotLocationDao(queryConfiguration);
        this.flagDao = new PlotFlagDao(queryConfiguration, flagRegistry);
        this.interactablesDao = new PlotInteractablesDao(queryConfiguration);
    }

    @Override
    public void startup() {
        PlotFlags.registerAllFlags(this);

        schematicManager.makeDirectories();
        this.plugin.serviceRegistry().register(new BackupService(schematicManager, this));
        var accessService = new AccessService(queryConfiguration, memberDao, deniedDao, logger);
        var biomeService = new BiomeService(logger);
        var flagService = new FlagService(flagDao, flagRegistry, queryConfiguration);
        var interactablesService = new InteractablesService(interactablesDao, queryConfiguration);
        var locationService = new PlotLocationService(locationDao, queryConfiguration);
        var signService = new SignService(signDao);

        plugin.serviceRegistry().register(accessService);
        plugin.serviceRegistry().register(biomeService);
        plugin.serviceRegistry().register(flagService);
        plugin.serviceRegistry().register(interactablesService);
        plugin.serviceRegistry().register(locationService);
        plugin.serviceRegistry().register(signService);
    }

    public boolean existsPlot(String id) {
        return plotCache.getIfPresent(id) != null;
    }

    public boolean existsPlot(ProtectedRegion region) {
        return existsPlot(region.getId());
    }

    public boolean existsGroup(String id) {
        return plotGroupCache.getIfPresent(id) != null;
    }


    public void createPlotGroup(String name) {
        var plotGroup = new PlotGroup(name);
        CompletableFuture.runAsync(() -> groupDao.write(plotGroup));
        plotGroupCache.put(name, plotGroup);
    }

    public void deletePlotGroup(String name) {
        plotGroupCache.invalidate(name);
        CompletableFuture.runAsync(() -> groupDao.delete(name));
    }

    public void setPlotGroupDisplayItem(PlotGroup plotGroup, @NotNull ItemStack itemStack) {
        plotGroup.displayItem(itemStack);
        groupDao.write(plotGroup);
    }

    public void unsetPlotGroupDisplayItem(PlotGroup plotGroup) {
        plotGroup.resetDisplayItem();
        groupDao.write(plotGroup);
    }

    public Optional<Plot> createBuyPlotFromRegion(ProtectedRegion region, World world, double price, String plotGroupName) {
        var plotId = region.getId();
        if (existsPlot(plotId)) {
            return Optional.empty();
        }
        var plot = new BuyPlot(plotId, null, plotGroupName, region.getId(), price, world.getName(), PlotState.AVAILABLE, null);

        return createPlot(region, plot, plotGroupName);
    }

    public Optional<Plot> createRentPlotFromRegion(ProtectedRegion region, World world, double price, String plotGroupName, Duration rentInterval) {
        var plotId = region.getId();
        if (existsPlot(plotId)) {
            return Optional.empty();
        }
        var plot = new RentPlot(plotId, null, plotGroupName, region.getId(), price, world.getName(), PlotState.AVAILABLE, null, null, rentInterval.toMinutes());

        return createPlot(region, plot, plotGroupName);
    }

    private Optional<Plot> createPlot(ProtectedRegion region, Plot plot, String plotGroupName) {
        plot.groupName(plotGroupName);

        region.setFlag(Flags.INTERACT, StateFlag.State.ALLOW);
        region.setFlag(Flags.USE, StateFlag.State.ALLOW);

        setDefaults(plot);

        savePlot(plot);
        return Optional.of(plot);
    }

    private void setDefaults(Plot plot) {
        plot.state(PlotState.AVAILABLE);
        plot.owner(null);
        flagRegistry.getAllRegistered().forEach(plotFlag -> plot.setFlag(plotFlag, plotFlag.defaultValue()));
        if (plot instanceof RentPlot rentPlot) {
            rentPlot.lastRentPayed(null);
        }

        if (plot instanceof RentPlot rentPlot) {
            rentPlot.lastRentPayed(null);
        }

        var optLocation = LocationUtil.findSuitablePlotLocation(plot.world(), plot.protectedRegion());
        if (optLocation.isPresent()) {
            var location = optLocation.get();
            var plotHome = new PlotLocation(plot.id(), plot.id(), true, location.x(), location.y(), location.z(), 0, 0);
            plot.plotHome(plotHome);
        }

        plot.interactables(PlotInteractable.defaults());
    }

    public void deletePlot(String id) {
        plotCache.invalidate(id);
        CompletableFuture.runAsync(() -> plotDao.delete(id));
    }

    public void setPlotOwner(OfflinePlayer player, Plot plot) {
        plot.state(PlotState.SOLD);
        plot.owner(new PlotPlayer(plot.id(), player.getUniqueId(), player.getName()));
        CompletableFuture.runAsync(() -> plotDao.write(queryConfiguration, plot));
    }

    public void setPlotPrice(double price, Plot plot) {
        plot.updatePrice(price);
        CompletableFuture.runAsync(() -> plotDao.write(queryConfiguration, plot));
    }

    public void setPlotGroup(String groupName, Plot plot) {
        plot.groupName(groupName);
        CompletableFuture.runAsync(() -> plotDao.write(queryConfiguration, plot));
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

        return getPlot(plotId);
    }

    public Optional<Plot> findPlotForRegion(ProtectedRegion region) {
        return getPlot(region.getId());
    }

    public Optional<Plot> getPlot(String id) {
        return Optional.ofNullable(plotCache.getIfPresent(id));
    }

    public Optional<PlotGroup> getGroup(String id) {
        return Optional.ofNullable(plotGroupCache.getIfPresent(id));
    }

    public Plot getPlot(ProtectedRegion region) {
        return plotCache.getIfPresent(region.getId());
    }

    public List<Plot> findPlotsByOwnerUUID(UUID uuid) {
        return plotCache.asMap().values().stream().filter(plot -> plot.isOwner(uuid))
                .sorted(Comparator.comparing(Plot::claimed))
                .toList();
    }

    public List<Plot> findPlotsByMember(UUID memberOrOwner) {
        return plotCache.asMap().values().stream()
                .filter(plot -> plot.isMember(memberOrOwner))
                .sorted(Comparator.comparing(Plot::claimed))
                .toList();
    }

    public List<Plot> findAvailablePlots(String groupName) {
        return plotCache.asMap().values().stream()
                .filter(plot -> plot.state().equals(PlotState.AVAILABLE))
                .filter(plot -> plot.groupName() != null && plot.groupName().equals(groupName))
                .toList();
    }

    public List<Plot> findPlotsByOwnerUUIDForGroup(UUID uuid, String groupName) {
        if (groupName == null) {
            return findPlotsByOwnerUUID(uuid);
        }
        return findPlotsByOwnerUUID(uuid).stream()
                .filter(plot -> plot.groupName() != null && plot.groupName().equals(groupName))
                .toList();
    }

    public Optional<Plot> getPlotForSignLocation(Location location) {

        var signCache = plugin.serviceRegistry().getRegistered(SignService.class).plotSignCache();
        var signOpt = signCache.stream().filter(sign -> sign.isAt(location)).findFirst();
        if (signOpt.isEmpty()) {
            return Optional.empty();
        }
        return getPlot(signOpt.get().plotId());
    }


    public void resetPlot(Plot plot) {
        setDefaults(plot);
        savePlot(plot);
        var accessService = plugin.serviceRegistry().getRegistered(AccessService.class);
        accessService.clearMembers(plot);
        accessService.clearDeniedPlayers(plot);
        plot.protectedRegion().getOwners().removeAll();
        plot.protectedRegion().getMembers().removeAll();


        SignManager.updateSings(plot, plugin.messenger());
    }

    public boolean claimPlot(Player player, Plot plot) {
        var event = new PlotClaimPlayerEvent(plot, player);
        event.callEvent();
        if (event.isCancelled()) {
            return false;
        }

        if (plot instanceof RentPlot rentPlot) {
            rentPlot.lastRentPayed(LocalDateTime.now());
        }

        plugin.serviceRegistry().getRegistered(EconomyService.class).withdraw(player.getUniqueId(), plot.price());

        plot.state(PlotState.SOLD);
        plot.owner(new PlotPlayer(plot.id(), player.getUniqueId(), player.getName()));
        plotDao.write(queryConfiguration, plot);

        plot.protectedRegion().getOwners().addPlayer(player.getUniqueId());

        if (!plugin.configuration().fb().noSchematic().contains(plot.world().getName())) {
            schematicManager.createPreSaleSchematic(plot);
        }

        SignManager.updateSings(plot, plugin.messenger());
        return true;
    }

    public void unClaimPlot(Plot plot) {
        var event = new PlotSellPlayerEvent(plot, Bukkit.getPlayer(plot.owner().uuid()));
        event.callEvent();
        if (event.isCancelled()) {
            return;
        }
        if (plot instanceof BuyPlot) {
            plugin.serviceRegistry().getRegistered(EconomyService.class).deposit(plot.owner().uuid(), plot.price() * 0.8);
        }

        resetPlot(plot);
    }

    public void savePlot(Plot plot) {
        plotCache.put(plot.id(), plot);

        CompletableFuture.runAsync(() -> {
            try (var conn = queryConfiguration.withSingleTransaction()) {
                //logger.info("Saving plot with ID: " + plot.id());
                plotDao.write(conn, plot);
                plugin.serviceRegistry().getRegistered(FlagService.class).saveCurrentFlags(conn, plot);
                plugin.serviceRegistry().getRegistered(InteractablesService.class).saveCurrentInteractables(conn, plot);
                plugin.serviceRegistry().getRegistered(PlotLocationService.class).setPlotHome(conn, plot, plot.plotHome());
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Failed to save plot " + plot.id(), e);
            }
        }).exceptionally(throwable -> {
            logger.log(Level.SEVERE, "Failed to save plot " + plot.id(), throwable);
            return null;
        }).whenComplete((unused, throwable) -> {
            if (throwable != null) {
                logger.log(Level.SEVERE, "Failed to save plot " + plot.id(), throwable);
            } else {
                SignManager.updateSings(plot, plugin.messenger());
            }
        });
    }

    /* Load everything in memory as we need access to all plots, members, flags, etc. when a location in
    the world is interacted with, to check if the interaction is allowed.
    For example, a member is allowed to build on a plot (if the access modifier is set to this value), but a non-member is not.
     */
    public void cacheAll() {
        var logger = JavaPlugin.getPlugin(PlotsPlugin.class).getLogger();
        logger.warning("Loading plots from database...");
        var plotsFuture = CompletableFuture.supplyAsync(plotDao::readAll);
        var membersFuture = CompletableFuture.supplyAsync(memberDao::readAll);
        var deniedFuture = CompletableFuture.supplyAsync(deniedDao::readAll);
        var signsFuture = CompletableFuture.supplyAsync(signDao::readAll);
        var warpsFuture = CompletableFuture.supplyAsync(locationDao::readAll);
        var flagsFuture = CompletableFuture.supplyAsync(flagDao::readAll);
        var interactablesFuture = CompletableFuture.supplyAsync(interactablesDao::readAll);
        var groupsFuture = CompletableFuture.supplyAsync(groupDao::readAll);

        CompletableFuture.allOf(
                plotsFuture,
                membersFuture,
                deniedFuture,
                signsFuture,
                warpsFuture,
                flagsFuture,
                interactablesFuture,
                groupsFuture
        ).whenComplete((result, throwable) -> {
            if (throwable != null) {
                logger.log(Level.SEVERE, "Failed to load plots from database.", throwable);
                return;
            }
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
                }
                plugin.serviceRegistry().getRegistered(SignService.class).plotSignCache().add(plotSign);
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
            groups.forEach(plotGroup -> plotGroupCache.put(plotGroup.name(), plotGroup));
        });
    }

    public PlotsPlugin plugin() {
        return plugin;
    }

    public FlagRegistry flagRegistry() {
        return flagRegistry;
    }

    public Cache<String, Plot> plotCache() {
        return plotCache;
    }

    public Cache<String, PlotGroup> plotGroupCache() {
        return plotGroupCache;
    }
}