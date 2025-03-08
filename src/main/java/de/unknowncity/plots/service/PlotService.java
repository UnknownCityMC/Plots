package de.unknowncity.plots.service;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import de.unknowncity.astralib.common.service.Service;
import de.unknowncity.plots.PlotsPlugin;
import de.unknowncity.plots.data.model.plot.*;
import de.unknowncity.plots.data.model.plot.group.PlotGroup;
import de.unknowncity.plots.data.repository.PlotGroupRepository;
import de.unknowncity.plots.util.PlotId;
import org.bukkit.World;

import java.time.Duration;
import java.util.HashMap;

public class PlotService implements Service<PlotsPlugin> {
    private final PlotGroupRepository plotGroupRepository;

    private HashMap<String, Plot> plotCache = new HashMap<>();
    private HashMap<String, PlotGroup> plotGroupCache = new HashMap<>();

    public PlotService(PlotGroupRepository plotGroupRepository) {
        this.plotGroupRepository = plotGroupRepository;
        this.cacheAll();
    }


    @Override
    public void shutdown() {

    }

    public void cacheAll() {
        plotGroupRepository.loadPlotCache().whenComplete((plotCache, thr1) -> {
            this.plotCache = plotCache;
            plotGroupRepository.loadPlotGroupCache(plotCache).whenComplete((plotGroupCache, thr2) -> {
                this.plotGroupCache = plotGroupCache;
            });
        });
    }

    public boolean existsPlot(String id) {
        return plotCache.containsKey(id);
    }

    public boolean existsPlot(ProtectedRegion region, World world) {
        return existsPlot(PlotId.generate(world, region));
    }

    public boolean createBuyPlotFromRegion(ProtectedRegion region, World world, double price, String plotGroupName) {
        var plotId = PlotId.generate(world, region);
        if (plotCache.containsKey(plotId)) {
            return false;
        }
        var plot = new BuyPlot(plotId, plotGroupName, region.getId(), price, world.getName());

        addPlotToPlotGroup(plot, plotGroupName);

        plotGroupRepository.savePlot(plot);
        plotCache.put(plot.id(), plot);
        return true;
    }

    public boolean createRentPlotFromRegion(ProtectedRegion region, World world, double price, String plotGroupName, Duration rentInterval) {
        var plotId = PlotId.generate(world, region);
        if (plotCache.containsKey(plotId)) {
            return false;
        }
        var plot = new RentPlot(plotId, plotGroupName, region.getId(), price, world.getName(), null, rentInterval.toMinutes());

        addPlotToPlotGroup(plot, plotGroupName);

        plotGroupRepository.savePlot(plot);
        plotCache.put(plot.id(), plot);
        return true;
    }

    public void addPlotToPlotGroup(Plot plot, String plotGroupName) {
        if (plotGroupName != null) {
            var plotGroup = plotGroupCache.get(plotGroupName);
            plotGroup.plotsInGroup().put(plot.id(), plot);
            savePlotGroup(plotGroup);
        }
    }

    public boolean createPlotGroup(String name) {
        var plotGroup = new PlotGroup(name);
        plotGroupRepository.savePlotGroup(plotGroup);
        plotGroupCache.put(name, plotGroup);
        return true;
    }

    public boolean deletePlotGroup(String name) {
        var plotGroup = plotGroupCache.get(name);
        if (plotGroup == null) {
            return false;
        }

        plotGroupRepository.deletePlotGroup(plotGroup);
        this.deletePlotGroup(plotGroup);
        return true;
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
        plotGroupCache.get(plot.groupName()).plotsInGroup().remove(plot.id());
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

    public Plot getPlotFromGroup(String id, String groupName) {
        return plotGroupCache.get(groupName).plotsInGroup().get(id);
    }
}
