package de.unknowncity.plots.data.repository;

import de.unknowncity.plots.data.dao.*;
import de.unknowncity.plots.plot.Plot;
import de.unknowncity.plots.plot.flag.PlotInteractable;
import de.unknowncity.plots.plot.group.PlotGroup;
import de.unknowncity.plots.plot.location.signs.PlotSign;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

public class PlotGroupRepository {
    private final GroupDao plotGroupDao;
    private final PlotDao plotDao;
    private final PlotFlagDao plotFlagDao;
    private final PlotInteractablesDao plotInteractablesDao;
    private final PlotLocationDao plotLocationDao;
    private final PlotSignDao plotSignDao;
    private final PlotMemberDao plotMemberDao;

    public PlotGroupRepository(
            GroupDao plotGroupDao,
            PlotDao plotDao,
            PlotFlagDao plotFlagDao,
            PlotInteractablesDao plotInteractablesDao,
            PlotLocationDao plotLocationDao,
            PlotSignDao plotSignDao,
            PlotMemberDao plotMemberDao
    ) {
        this.plotGroupDao = plotGroupDao;
        this.plotDao = plotDao;
        this.plotFlagDao = plotFlagDao;
        this.plotInteractablesDao = plotInteractablesDao;
        this.plotLocationDao = plotLocationDao;
        this.plotSignDao = plotSignDao;
        this.plotMemberDao = plotMemberDao;
    }
}