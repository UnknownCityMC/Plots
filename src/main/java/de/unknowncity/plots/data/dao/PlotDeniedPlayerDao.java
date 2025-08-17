package de.unknowncity.plots.data.dao;

import de.unknowncity.plots.plot.access.entity.PlotPlayer;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface PlotDeniedPlayerDao {
    CompletableFuture<Optional<PlotPlayer>> read(UUID uuid, String plotId);
    CompletableFuture<Boolean> write(PlotPlayer plotMember, String plotId);
    CompletableFuture<List<PlotPlayer>> readAll(String plotId);
    CompletableFuture<Boolean> delete(UUID memberId, String plotId);
}