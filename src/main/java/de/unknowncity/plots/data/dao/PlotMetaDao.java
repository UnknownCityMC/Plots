package de.unknowncity.plots.data.dao;

import de.unknowncity.plots.data.model.plot.PlotMeta;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface PlotMetaDao {
    CompletableFuture<Optional<PlotMeta>> read(String plotId);
    CompletableFuture<Boolean> write(PlotMeta plotMeta, String plotId);
}
