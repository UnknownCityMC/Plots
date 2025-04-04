package de.unknowncity.plots.data.dao;

import de.unknowncity.plots.plot.location.RelativePlotLocation;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface PlotLocationDao {
    CompletableFuture<Boolean> write(RelativePlotLocation plotLocation, String plotId);
    CompletableFuture<List<RelativePlotLocation>> readAll(String plotId);
}
