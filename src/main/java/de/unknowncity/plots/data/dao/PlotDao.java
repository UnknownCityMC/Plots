package de.unknowncity.plots.data.dao;

import de.unknowncity.plots.data.model.plot.Plot;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface PlotDao {
    CompletableFuture<Optional<? extends Plot>> read(String plotId);
    CompletableFuture<Boolean> write(Plot plot);
    CompletableFuture<List<? extends Plot>> readAll();
    CompletableFuture<List<? extends Plot>> readAllFromGroup(String groupName);
    CompletableFuture<Boolean> delete(String plotId);
}
