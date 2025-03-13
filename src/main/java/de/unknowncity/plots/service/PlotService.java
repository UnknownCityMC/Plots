package de.unknowncity.plots.service;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import de.unknowncity.astralib.common.service.Service;
import de.unknowncity.plots.PlotsPlugin;
import de.unknowncity.plots.data.model.plot.BuyPlot;
import de.unknowncity.plots.data.model.plot.Plot;
import de.unknowncity.plots.data.model.plot.PlotState;
import de.unknowncity.plots.data.model.plot.RentPlot;
import de.unknowncity.plots.data.model.plot.group.PlotGroup;
import de.unknowncity.plots.data.repository.PlotGroupRepository;
import de.unknowncity.plots.util.PlotId;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;

public class PlotService implements Service<PlotsPlugin> {
    private final PlotGroupRepository plotGroupRepository;

    private HashMap<String, Plot> plotCache = new HashMap<>();
    private HashMap<String, PlotGroup> plotGroupCache = new HashMap<>();
    private final EconomyService economyService;

    public PlotService(PlotGroupRepository plotGroupRepository, EconomyService economyService) {
        this.plotGroupRepository = plotGroupRepository;
        this.economyService = economyService;
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

        addPlotToPlotGroup(plot, plotGroupName);

        savePlot(plot);
        return true;
    }

    public boolean createRentPlotFromRegion(ProtectedRegion region, World world, double price, String plotGroupName, Duration rentInterval) {
        var plotId = PlotId.generate(world, region);
        if (plotCache.containsKey(plotId)) {
            return false;
        }
        var plot = new RentPlot(plotId, null, plotGroupName, region.getId(), price, world.getName(), PlotState.AVAILABLE, null, rentInterval.toMinutes());

        addPlotToPlotGroup(plot, plotGroupName);

        savePlot(plot);
        return true;
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
    }

    public void unClaimPlot(Plot plot) {
        economyService.deposit(plot.owner(), plot.price());

        plot.state(PlotState.AVAILABLE);
        plot.owner(null);
        plot.flags(new ArrayList<>());
        plot.members(new ArrayList<>());
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

    public PlotGroup getGroup(String id) {
        return plotGroupCache.get(id);
    }

    public Plot getPlot(World world, ProtectedRegion region) {
        return plotCache.get(PlotId.generate(world, region));
    }

    public Plot getPlotFromGroup(String id, String groupName) {
        return plotGroupCache.get(groupName).plotsInGroup().get(id);
    }
}
