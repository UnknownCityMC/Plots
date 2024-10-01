package de.unknowncity.plots.data.repository;

import de.unknowncity.plots.data.dao.*;
import de.unknowncity.plots.data.model.plot.Plot;
import de.unknowncity.plots.data.model.plot.group.PlotGroup;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class PlotGroupRepository  {
    private final GroupDao plotGroupDao;
    private final PlotDao plotDao;
    private final PlotFlagDao plotFlagDao;
    private final PlotLocationDao plotLocationDao;
    private final PlotMemberDao plotMemberDao;
    private final PlotMetaDao plotMetaDao;

    public PlotGroupRepository(
            GroupDao plotGroupDao,
            PlotDao plotDao,
            PlotFlagDao plotFlagDao,
            PlotLocationDao plotLocationDao,
            PlotMemberDao plotMemberDao,
            PlotMetaDao plotMetaDao
    ) {
        this.plotGroupDao = plotGroupDao;
        this.plotDao = plotDao;
        this.plotFlagDao = plotFlagDao;
        this.plotLocationDao = plotLocationDao;
        this.plotMemberDao = plotMemberDao;
        this.plotMetaDao = plotMetaDao;
    }

    public void savePlotGroup(PlotGroup plotGroup) {
        plotGroupDao.write(plotGroup);
        plotGroup.plotsInGroup().forEach(this::savePlot);
    }

    public void savePlot(Plot plot) {
        plotDao.write(plot);
        plotMetaDao.write(plot.meta(), plot.id());
        plot.flags().forEach(plotFlag -> plotFlagDao.write(plotFlag, plot.id()));
        plot.members().forEach(plotMember -> plotMemberDao.write(plotMember, plot.id()));
        plot.locations().forEach(plotLocation -> plotLocationDao.write(plotLocation, plot.id()));
    }

    public void deletePlot(Plot plot) {
        plotDao.delete(plot.id());
    }

    public void deletePlotGroup(PlotGroup plotGroup) {
        plotGroupDao.delete(plotGroup.name());
    }

    public CompletableFuture<CompletableFuture<Optional<PlotGroup>>> getPlotGroupWithPlots(String name) {
        return plotGroupDao.read(name).thenApplyAsync((plotGroup) -> plotDao.readAllFromGroup(name).thenApplyAsync((plots) -> {
            plots.forEach(plot -> {
                plotFlagDao.readAll(plot.id()).thenApplyAsync(plotFlags -> plot.flags());
                plotMemberDao.readAll(plot.id()).thenApplyAsync(plotMembers -> plot.members());
                plotLocationDao.readAll(plot.id()).thenApplyAsync(plotLocations -> plot.locations());
            });

            plotGroup.ifPresent(group -> group.plotsInGroup(plots));
            return plotGroup;
        }));
    }

    public CompletableFuture<Optional<Plot>> getPlot(String id) {
        return plotDao.read(id).thenApplyAsync(plotOpt -> {
            plotOpt.ifPresent(plot -> {
                plotFlagDao.readAll(plot.id()).thenApplyAsync(plotFlags -> plot.flags());
                plotMemberDao.readAll(plot.id()).thenApplyAsync(plotMembers -> plot.members());
                plotLocationDao.readAll(plot.id()).thenApplyAsync(plotLocations -> plot.locations());
            });
            return plotOpt;
        });
    }
}
