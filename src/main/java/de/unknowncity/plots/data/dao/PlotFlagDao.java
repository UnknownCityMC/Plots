package de.unknowncity.plots.data.dao;

import de.unknowncity.plots.data.model.plot.flag.PlotFlag;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface PlotFlagDao {
    CompletableFuture<Boolean> write(PlotFlag plotFlag, String plotId);
    CompletableFuture<List<PlotFlag>> readAll(String plotId);
}
