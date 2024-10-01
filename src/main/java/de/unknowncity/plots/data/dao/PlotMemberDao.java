package de.unknowncity.plots.data.dao;

import de.unknowncity.plots.data.model.plot.PlotMember;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface PlotMemberDao {
    CompletableFuture<Optional<PlotMember>> read(UUID memberId, String plotId);
    CompletableFuture<Boolean> write(PlotMember plotMember, String plotId);
    CompletableFuture<List<PlotMember>> readAll(String plotId);
    CompletableFuture<Boolean> delete(UUID memberId, String plotId);
}