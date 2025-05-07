package de.unknowncity.plots.data.dao;

import de.unknowncity.plots.plot.location.PlotLocation;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface PlotLocationDao {
    CompletableFuture<Boolean> write(PlotLocation plotLocation, String plotId);
    CompletableFuture<Optional<PlotLocation>> read(String plotId);
}
