package de.unknowncity.plots.data.dao;

import de.unknowncity.plots.data.model.plot.group.PlotGroup;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface GroupDao {

    CompletableFuture<Optional<PlotGroup>> read(String groupName);
    CompletableFuture<List<PlotGroup>> readAll();
    CompletableFuture<Boolean> write(PlotGroup plotGroup);
    CompletableFuture<Boolean> delete(String groupName);
}
