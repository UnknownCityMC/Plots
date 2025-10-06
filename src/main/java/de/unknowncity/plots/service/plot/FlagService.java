package de.unknowncity.plots.service.plot;

import de.chojo.sadu.queries.api.configuration.QueryConfiguration;
import de.unknowncity.astralib.common.service.Service;
import de.unknowncity.plots.PlotsPlugin;
import de.unknowncity.plots.data.dao.PlotFlagDao;
import de.unknowncity.plots.plot.flag.FlagRegistry;
import de.unknowncity.plots.plot.flag.PlotFlag;
import de.unknowncity.plots.plot.model.Plot;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public class FlagService extends Service<PlotsPlugin> {
    private final PlotFlagDao plotFlagDao;
    private final FlagRegistry flagRegistry;
    private final QueryConfiguration queryConfiguration;

    public FlagService(PlotFlagDao plotFlagDao, FlagRegistry flagRegistry, QueryConfiguration queryConfiguration) {
        this.plotFlagDao = plotFlagDao;
        this.flagRegistry = flagRegistry;
        this.queryConfiguration = queryConfiguration;
    }

    public void saveCurrentFlags(Plot plot) {
        CompletableFuture.runAsync(() -> saveCurrentFlags(queryConfiguration, plot)).whenComplete((unused, throwable) -> {
            if (throwable != null) {
                JavaPlugin.getPlugin(PlotsPlugin.class).getLogger().log(Level.SEVERE, "Error while saving plot data: ", throwable);
            }
        });
    }

    public void saveCurrentFlags(QueryConfiguration configuration, Plot plot) {
        plotFlagDao.save(configuration, plot.id(), plot.flags());
    }

    public void setDefaults(Plot plot) {
        flagRegistry.getAllRegistered().forEach(plotFlag -> plot.setFlag(plotFlag, plotFlag.defaultValue()));

        CompletableFuture.runAsync(() -> plotFlagDao.save(queryConfiguration, plot.id(), plot.flags())).whenComplete((unused, throwable) -> {
            if (throwable != null) {
                JavaPlugin.getPlugin(PlotsPlugin.class).getLogger().log(Level.SEVERE, "Error while saving plot data: ", throwable);
            }
        });
    }
}
