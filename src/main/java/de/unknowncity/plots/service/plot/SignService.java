package de.unknowncity.plots.service.plot;

import de.chojo.sadu.queries.api.configuration.QueryConfiguration;
import de.unknowncity.astralib.common.service.Service;
import de.unknowncity.plots.PlotsPlugin;
import de.unknowncity.plots.data.dao.PlotSignDao;
import de.unknowncity.plots.plot.location.signs.PlotSign;
import de.unknowncity.plots.plot.model.Plot;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class SignService extends Service<PlotsPlugin> {
    private final PlotSignDao signDao;
    private final Set<PlotSign> plotSignCache = new HashSet<>();

    public SignService(PlotSignDao signDao, QueryConfiguration queryConfiguration) {
        this.signDao = signDao;
    }

    public void cacheAll() {
        CompletableFuture.supplyAsync(signDao::readAll).whenCompleteAsync((plotSigns, throwable) -> {
            plotSignCache.addAll(plotSigns);
        });
    }

    public PlotSign addSign(Plot plot, org.bukkit.Location location) {
        var sign = plot.addSign(location);
        plotSignCache.add(sign);
        CompletableFuture.runAsync(() -> signDao.save(sign));
        return sign;
    }

    public void removeSign(Plot plot, org.bukkit.Location location) {
        plot.removeSign(location);
        plotSignCache.removeIf(plotSign -> plotSign.isAt(location));
        CompletableFuture.runAsync(() -> signDao.delete(plot.id(), location.getBlockX(), location.getBlockY(), location.getBlockZ()));
    }

    public Set<PlotSign> plotSignCache() {
        return plotSignCache;
    }
}
