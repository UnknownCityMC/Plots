package de.unknowncity.plots.database.dao;

import de.unknowncity.plots.plot.Plot;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface PlotsDao {

    CompletableFuture<Optional<Plot>> read(String plotID, String world);

    CompletableFuture<Boolean> write(Plot plot);

    CompletableFuture<Boolean> update(Plot plot);

    CompletableFuture<Boolean> delete(String plotID, String world);
}
