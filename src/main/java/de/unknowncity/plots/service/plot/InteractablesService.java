package de.unknowncity.plots.service.plot;

import de.chojo.sadu.queries.api.configuration.QueryConfiguration;
import de.unknowncity.astralib.common.service.Service;
import de.unknowncity.plots.PlotsPlugin;
import de.unknowncity.plots.data.dao.PlotInteractablesDao;
import de.unknowncity.plots.plot.access.type.PlotAccessModifier;
import de.unknowncity.plots.plot.flag.PlotInteractable;
import de.unknowncity.plots.plot.model.Plot;
import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public class InteractablesService extends Service<PlotsPlugin> {
    private final PlotInteractablesDao interactablesDao;
    private final QueryConfiguration queryConfiguration;

    public InteractablesService(PlotInteractablesDao interactablesDao, QueryConfiguration queryConfiguration) {
        this.interactablesDao = interactablesDao;
        this.queryConfiguration = queryConfiguration;
    }

    public void setDefaults(Plot plot) {
        plot.interactables(PlotInteractable.defaults());
        CompletableFuture.runAsync(() -> interactablesDao.write(queryConfiguration, plot.id(), plot.interactables())).whenComplete((unused, throwable) -> {
            if (throwable != null) {
                JavaPlugin.getPlugin(PlotsPlugin.class).getLogger().log(Level.SEVERE, "Error while saving plot data: ", throwable);
            }
        });
    }

    public void setInteractable(Plot plot, Material material, PlotAccessModifier modifier) {
        plot.updateInteractable(material, modifier);
    }

    public void saveCurrentInteractables(Plot plot) {
        CompletableFuture.runAsync(() -> interactablesDao.write(queryConfiguration, plot.id(), plot.interactables())).whenComplete((unused, throwable) -> {
            if (throwable != null) {
                JavaPlugin.getPlugin(PlotsPlugin.class).getLogger().log(Level.SEVERE, "Error while saving plot data: ", throwable);
            }
        });
    }

    public void saveCurrentInteractables(QueryConfiguration configuration, Plot plot) {
        interactablesDao.write(configuration, plot.id(), plot.interactables());
    }
}
