package de.unknowncity.plots.data.dao;

import de.unknowncity.plots.data.model.plot.flag.PlotInteractable;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface PlotInteractablesDao {
    CompletableFuture<Boolean> write(PlotInteractable plotInteractable, String plotId);
    CompletableFuture<List<PlotInteractable>> readAll(String plotId);
}
