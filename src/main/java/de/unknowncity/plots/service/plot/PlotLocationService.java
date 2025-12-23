package de.unknowncity.plots.service.plot;

import de.chojo.sadu.queries.api.configuration.QueryConfiguration;
import de.unknowncity.astralib.common.service.Service;
import de.unknowncity.plots.PlotsPlugin;
import de.unknowncity.plots.data.dao.PlotHomeDao;
import de.unknowncity.plots.data.dao.PlotHomeResetsDao;
import de.unknowncity.plots.plot.location.PlotHome;
import de.unknowncity.plots.plot.location.PlotPosition;
import de.unknowncity.plots.plot.model.Plot;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public class PlotLocationService extends Service<PlotsPlugin> {
    private final PlotHomeDao plotHomeDao;
    private final PlotHomeResetsDao plotHomeResetsDao;
    private final QueryConfiguration queryConfiguration;

    public PlotLocationService(PlotHomeDao locationDao, PlotHomeResetsDao plotHomeResetsDao, QueryConfiguration queryConfiguration) {
        this.plotHomeDao = locationDao;
        this.plotHomeResetsDao = plotHomeResetsDao;
        this.queryConfiguration = queryConfiguration;
    }

    public void setPlotHome(Plot plot, boolean isPublic, Location location) {
        var plotHome = new PlotHome(plot.id(), "", isPublic, location);
        CompletableFuture.runAsync(() -> setPlotHome(queryConfiguration, plot, plotHome)).whenComplete((unused, throwable) -> {
            if (throwable != null) {
                JavaPlugin.getPlugin(PlotsPlugin.class).getLogger().log(Level.SEVERE, "Error while saving plot data: ", throwable);
            }
        });
    }

    public void setPlotHome(QueryConfiguration configuration, Plot plot,  PlotHome plotHome) {
        plot.plotHome(plotHome);
        CompletableFuture.runAsync(() -> plotHomeDao.write(configuration, plot.id(), plotHome)).whenComplete((unused, throwable) -> {
            if (throwable != null) {
                JavaPlugin.getPlugin(PlotsPlugin.class).getLogger().log(Level.SEVERE, "Error while saving plot data: ", throwable);
            }
        });
    }

    public void setPlotHomeResetLocation(QueryConfiguration configuration, Plot plot, PlotPosition plotPosition) {
        CompletableFuture.runAsync(() -> plotHomeResetsDao.write(configuration, plot.id(), plotPosition)).whenComplete((unused, throwable) -> {
            if (throwable != null) {
                JavaPlugin.getPlugin(PlotsPlugin.class).getLogger().log(Level.SEVERE, "Error while saving plot data: ", throwable);
            }
        });
    }

    public void setPlotHomeResetLocation(Plot plot, PlotPosition plotPosition) {
        setPlotHomeResetLocation(queryConfiguration, plot, plotPosition);
    }

    public CompletableFuture<Optional<PlotPosition>> getHomeResetLocation(Plot plot) {
        return CompletableFuture.supplyAsync(() -> plotHomeResetsDao.read(plot.id()));
    }
}
