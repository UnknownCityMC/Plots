package de.unknowncity.plots.service.plot;

import de.chojo.sadu.queries.api.configuration.QueryConfiguration;
import de.unknowncity.astralib.common.service.Service;
import de.unknowncity.plots.PlotsPlugin;
import de.unknowncity.plots.data.dao.PlotLocationDao;
import de.unknowncity.plots.plot.location.PlotLocation;
import de.unknowncity.plots.plot.model.Plot;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public class PlotLocationService extends Service<PlotsPlugin> {
    private final PlotLocationDao plotLocationDao;
    private final QueryConfiguration queryConfiguration;

    public PlotLocationService(PlotLocationDao locationDao, QueryConfiguration queryConfiguration) {
        this.plotLocationDao = locationDao;
        this.queryConfiguration = queryConfiguration;
    }

    public void setPlotHome(Plot plot, boolean isPublic, Location location) {
        var plotHome = new PlotLocation(plot.id(), "", isPublic, location);
        CompletableFuture.runAsync(() -> setPlotHome(queryConfiguration, plot, plotHome)).whenComplete((unused, throwable) -> {
            if (throwable != null) {
                JavaPlugin.getPlugin(PlotsPlugin.class).getLogger().log(Level.SEVERE, "Error while saving plot data: ", throwable);
            }
        });
    }

    public void setPlotHome(QueryConfiguration configuration, Plot plot,  PlotLocation plotLocation) {
        plot.plotHome(plotLocation);
        CompletableFuture.runAsync(() -> plotLocationDao.write(configuration, plot.id(), plotLocation)).whenComplete((unused, throwable) -> {
            if (throwable != null) {
                JavaPlugin.getPlugin(PlotsPlugin.class).getLogger().log(Level.SEVERE, "Error while saving plot data: ", throwable);
            }
        });
    }
}
