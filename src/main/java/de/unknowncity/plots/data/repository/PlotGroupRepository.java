package de.unknowncity.plots.data.repository;

import de.unknowncity.plots.data.dao.*;
import de.unknowncity.plots.plot.Plot;
import de.unknowncity.plots.plot.flag.PlotInteractable;
import de.unknowncity.plots.plot.group.PlotGroup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

public class PlotGroupRepository {
    private final GroupDao plotGroupDao;
    private final PlotDao plotDao;
    private final PlotFlagDao plotFlagDao;
    private final PlotInteractablesDao plotInteractablesDao;
    private final PlotLocationDao plotLocationDao;
    private final PlotMemberDao plotMemberDao;

    public PlotGroupRepository(
            GroupDao plotGroupDao,
            PlotDao plotDao,
            PlotFlagDao plotFlagDao,
            PlotInteractablesDao plotInteractablesDao,
            PlotLocationDao plotLocationDao,
            PlotMemberDao plotMemberDao
    ) {
        this.plotGroupDao = plotGroupDao;
        this.plotDao = plotDao;
        this.plotFlagDao = plotFlagDao;
        this.plotInteractablesDao = plotInteractablesDao;
        this.plotLocationDao = plotLocationDao;
        this.plotMemberDao = plotMemberDao;
    }

    public CompletableFuture<HashMap<String, Plot>> loadPlotCache() {
        return plotDao.readAll().thenApplyAsync(plots -> {
            var plotsCache = new HashMap<String, Plot>();
            plots.forEach(plot -> {
                plotFlagDao.readAll(plot.id()).thenAccept(plotFlagWrappers -> {
                    plotFlagWrappers.forEach(plotFlagWrapper -> {
                        plot.setFlag(plotFlagWrapper.flag(), plotFlagWrapper.flagValue());
                    });
                });
                plotMemberDao.readAll(plot.id()).thenAccept(plot::members);
                plotLocationDao.readAll(plot.id()).thenAccept(plot::locations);
                plotInteractablesDao.readAll(plot.id()).thenAccept(plotInteractables -> {
                    var updatedInteractables = new ArrayList<>(PlotInteractable.defaults());
                    plotInteractables.forEach(interactable -> {
                        updatedInteractables.removeIf(pi -> pi.blockType() == interactable.blockType());
                    });
                    updatedInteractables.addAll(plotInteractables);
                    plot.interactables(updatedInteractables);
                });
                plotsCache.put(plot.id(), plot);
            });
            return plotsCache;
        });
    }

    public CompletableFuture<HashMap<String, PlotGroup>> loadPlotGroupCache(HashMap<String, Plot> plotCache) {
        return plotGroupDao.readAll().thenApply(plotGroups -> {
            var plotGroupCache = new HashMap<String, PlotGroup>();
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
            return plotGroupCache;
        });
    }

    public void savePlotGroup(PlotGroup plotGroup) {
        plotGroupDao.write(plotGroup);
        plotGroup.plotsInGroup().values().forEach(this::savePlot);
    }

    public void savePlot(Plot plot) {
        plotDao.write(plot);

        plot.flags().forEach((plotFlag, value) -> plotFlagDao.write(plot.id(), plotFlag.flagId(), plotFlag.marshall(value)));
        plot.interactables().forEach(plotInteractable -> plotInteractablesDao.write(plotInteractable, plot.id()));
        plot.members().forEach(plotMember -> plotMemberDao.write(plotMember, plot.id()));
        plot.locations().forEach(plotLocation -> plotLocationDao.write(plotLocation, plot.id()));
    }

    public void deletePlot(Plot plot) {
        plotDao.delete(plot.id());
    }

    public void deletePlotGroup(PlotGroup plotGroup) {
        plotGroup.plotsInGroup().values().forEach(plot -> {
            plot.groupName(null);
            savePlot(plot);
        });
        plotGroupDao.delete(plotGroup.name());
    }
}