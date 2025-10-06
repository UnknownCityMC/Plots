package de.unknowncity.plots.data;

import de.unknowncity.plots.data.dao.*;
import de.unknowncity.plots.plot.group.PlotGroup;
import de.unknowncity.plots.plot.location.signs.PlotSign;
import de.unknowncity.plots.plot.model.Plot;

import java.util.concurrent.*;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class PlotCacheLoader {

    private final PlotDao plotDao;
    private final PlotMemberDao memberDao;
    private final PlotDeniedPlayerDao deniedDao;
    private final PlotSignDao signDao;
    private final PlotLocationDao locationDao;
    private final PlotFlagDao flagDao;
    private final PlotInteractablesDao interactablesDao;
    private final PlotGroupDao groupDao;

    private final Logger logger;
    private final Executor executor = Executors.newFixedThreadPool(8);

    public PlotCacheLoader(
            PlotDao plotDao,
            PlotMemberDao memberDao,
            PlotDeniedPlayerDao deniedDao,
            PlotSignDao signDao,
            PlotLocationDao locationDao,
            PlotFlagDao flagDao,
            PlotInteractablesDao interactablesDao,
            PlotGroupDao groupDao,
            Logger logger
    ) {
        this.plotDao = plotDao;
        this.memberDao = memberDao;
        this.deniedDao = deniedDao;
        this.signDao = signDao;
        this.locationDao = locationDao;
        this.flagDao = flagDao;
        this.interactablesDao = interactablesDao;
        this.groupDao = groupDao;
        this.logger = logger;
    }

    public PlotCacheResult loadAll() {
        logger.warning("Loading plots from database...");

        // Async DB calls
        var plotsFuture = CompletableFuture.supplyAsync(plotDao::readAll, executor);
        var membersFuture = CompletableFuture.supplyAsync(memberDao::readAll, executor);
        var deniedFuture = CompletableFuture.supplyAsync(deniedDao::readAll, executor);
        var signsFuture = CompletableFuture.supplyAsync(signDao::readAll, executor);
        var warpsFuture = CompletableFuture.supplyAsync(locationDao::readAll, executor);
        var flagsFuture = CompletableFuture.supplyAsync(flagDao::readAll, executor);
        var interactablesFuture = CompletableFuture.supplyAsync(interactablesDao::readAll, executor);
        var groupsFuture = CompletableFuture.supplyAsync(groupDao::readAll, executor);

        CompletableFuture.allOf(
                plotsFuture, membersFuture, deniedFuture, signsFuture,
                warpsFuture, flagsFuture, interactablesFuture, groupsFuture
        ).join();

        // Construct results
        var plots = plotsFuture.join();
        var plotCache = plots.stream()
                .collect(Collectors.toConcurrentMap(Plot::id, Function.identity()));

        var plotSignCache = new ConcurrentHashMap<PlotSign, String>();
        var plotGroupCache = new ConcurrentHashMap<String, PlotGroup>();

        // --- Members ---
        membersFuture.join().forEach(member -> {
            var plot = plotCache.get(member.plotId());
            if (plot != null) plot.members().add(member);
        });

        // --- Denied Players ---
        deniedFuture.join().forEach(denied -> {
            var plot = plotCache.get(denied.plotId());
            if (plot != null) plot.deniedPlayers().add(denied);
        });

        // --- Signs ---
        signsFuture.join().forEach(sign -> {
            var plot = plotCache.get(sign.plotId());
            if (plot != null) {
                plot.signs().add(sign);
                plotSignCache.put(sign, plot.id());
            }
        });

        // --- Warps ---
        warpsFuture.join().forEach(location -> {
            var plot = plotCache.get(location.plotId());
            if (plot != null) plot.plotHome(location);
        });

        // --- Flags ---
        flagsFuture.join().forEach(flag -> {
            var plot = plotCache.get(flag.plotId());
            if (plot != null) plot.setFlag(flag.flag(), flag.flagValue());
        });

        // --- Interactables ---
        interactablesFuture.join().forEach(interactable -> {
            var plot = plotCache.get(interactable.plotId());
            if (plot != null) plot.interactables().add(interactable);
        });

        // --- Groups ---
        groupsFuture.join().forEach(group -> {
            plotGroupCache.put(group.name(), group);
        });

        logger.info("Loaded " + plots.size() + " plots and related data.");
        return new PlotCacheResult(plotCache, plotSignCache, plotGroupCache);
    }

    public record PlotCacheResult(
            ConcurrentMap<String, ? extends Plot> plots,
            ConcurrentMap<PlotSign, String> signCache,
            ConcurrentMap<String, PlotGroup> groupCache
    ) {}

}
