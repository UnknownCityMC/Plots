package de.unknowncity.plots.service.plot;

import de.unknowncity.astralib.common.service.Service;
import de.unknowncity.plots.PlotsPlugin;
import de.unknowncity.plots.data.dao.PlotSignDao;
import de.unknowncity.plots.plot.location.signs.PlotSign;
import de.unknowncity.plots.plot.model.Plot;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public class SignService extends Service<PlotsPlugin> {
    private final PlotSignDao signDao;
    private final Set<PlotSign> plotSignCache = new HashSet<>();

    public SignService(PlotSignDao signDao) {
        this.signDao = signDao;
    }

    public PlotSign addSign(Plot plot, org.bukkit.Location location) {
        var sign = plot.addSign(location);
        plotSignCache.add(sign);
        CompletableFuture.runAsync(() -> signDao.save(sign)).whenComplete((unused, throwable) -> {
            if (throwable != null) {
                JavaPlugin.getPlugin(PlotsPlugin.class).getLogger().log(Level.SEVERE, "Error while saving plot data: ", throwable);
            }
        });
        return sign;
    }

    public void removeSign(Plot plot, org.bukkit.Location location) {
        plot.removeSign(location);
        plotSignCache.removeIf(plotSign -> plotSign.isAt(location));
        CompletableFuture.runAsync(() -> signDao.delete(plot.id(), location.getBlockX(), location.getBlockY(), location.getBlockZ())).whenComplete((unused, throwable) -> {
            if (throwable != null) {
                JavaPlugin.getPlugin(PlotsPlugin.class).getLogger().log(Level.SEVERE, "Error while saving plot data: ", throwable);
            }
        });
    }

    public void removeAll(Plot plot) {
        plot.signs().clear();
        plotSignCache.removeIf(plotSign -> plotSign.plotId().equals(plot.id()));
        CompletableFuture.runAsync(() -> signDao.deleteAll(plot.id())).whenComplete((unused, throwable) -> {
            if (throwable != null) {
                JavaPlugin.getPlugin(PlotsPlugin.class).getLogger().log(Level.SEVERE, "Error while saving plot data: ", throwable);
            }
        });
    }

    public Set<PlotSign> plotSignCache() {
        return plotSignCache;
    }
}
