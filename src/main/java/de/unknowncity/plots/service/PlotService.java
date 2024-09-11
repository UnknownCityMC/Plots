package de.unknowncity.plots.service;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import de.unknowncity.astralib.common.service.Service;
import de.unknowncity.plots.PlotsPlugin;
import de.unknowncity.plots.database.dao.PlotsDao;
import de.unknowncity.plots.plot.Plot;
import de.unknowncity.plots.plot.PlotPaymentType;
import de.unknowncity.plots.plot.PlotState;
import de.unknowncity.plots.plot.flag.PlotFlag;
import org.bukkit.World;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;

public class PlotService implements Service<PlotsPlugin> {
    private final Set<Plot> plotCache = new HashSet<>();
    private final PlotsDao plotsDao;

    public PlotService(PlotsDao plotsDao) {
        this.plotsDao = plotsDao;
    }

    @Override
    public void startup(PlotsPlugin plugin) {

    }

    @Override
    public void shutdown() {

    }

    public boolean existsPlot(ProtectedRegion region, World world) {
        return plotCache.stream().anyMatch(plot -> plot.plotID().equals(region.getId()) && plot.worldName().equals(world.getName()));
    }

    public boolean createSellPlotFromExisting(ProtectedRegion region, World world, double price) {
        var plot = new Plot(
                region.getId(),
                world.getName(),
                null,
                PlotState.AVAILABLE,
                PlotPaymentType.SELL,
                price,
                null,
                0,
                Set.of(),
                PlotFlag.defaults()
        );

        plotsDao.write(plot);
        plotCache.add(plot);
        return true;
    }

    public boolean createRentPlotFromExisting(ProtectedRegion region, World world, double price, Duration rentInterval) {
        var plot = new Plot(
                region.getId(),
                world.getName(),
                null,
                PlotState.AVAILABLE,
                PlotPaymentType.RENT,
                price,
                null,
                rentInterval.toMinutes(),
                Set.of(),
                PlotFlag.defaults()
        );
        plotsDao.write(plot);
        plotCache.add(plot);
        return true;
    }
}
