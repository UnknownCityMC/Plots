package de.unknowncity.plots.data.dao;

import de.unknowncity.plots.data.model.PlotFlagWrapper;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface PlotFlagDao {
    CompletableFuture<Boolean> write(String plotId, String flagId, String value);
    CompletableFuture<List<PlotFlagWrapper<Object>>> readAll(String plotId);
}
