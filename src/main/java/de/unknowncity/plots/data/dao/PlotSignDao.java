package de.unknowncity.plots.data.dao;

import de.unknowncity.plots.plot.location.signs.PlotSign;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface PlotSignDao {
    CompletableFuture<Boolean> writeAll(List<PlotSign> plotSigns, String plotId);
    CompletableFuture<List<PlotSign>> readAll(String plotId);
    CompletableFuture<Boolean> delete(String plotId, int id);
    CompletableFuture<Boolean> deleteAll(String plotId);
}
