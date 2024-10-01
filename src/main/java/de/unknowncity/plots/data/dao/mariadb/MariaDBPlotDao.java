package de.unknowncity.plots.data.dao.mariadb;

import de.unknowncity.plots.data.dao.PlotDao;
import de.unknowncity.plots.data.model.plot.*;
import org.intellij.lang.annotations.Language;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static de.chojo.sadu.queries.api.call.Call.*;
import static de.chojo.sadu.queries.api.query.Query.*;

public class MariaDBPlotDao implements PlotDao {

    @Override
    public CompletableFuture<Optional<Plot>> read(String plotId) {
        @Language("mariadb")
        var queryString = """
                SELECT region_id, group_name
                FROM plot
                WHERE id = :plotId
                """;
        return CompletableFuture.supplyAsync(query(queryString)
                .single(call().bind("plotId", plotId))
                .map(row -> new Plot(
                        plotId,
                        row.getString("group_name"),
                        row.getString("region_id")
                ))::first
        );
    }

    @Override
    public CompletableFuture<Boolean> write(Plot plot) {
        @Language("mariadb")
        var queryString = """
                REPLACE INTO plot (id, group_name, region_id)
                VALUES (:id, :groupName, :regionId)
                """;
        return CompletableFuture.supplyAsync(query(queryString)
                .single(call()
                        .bind("plotId", plot.id())
                        .bind("groupName", plot.groupName())
                        .bind("regionId", plot.regionId())
                )
                .insert()::changed
        );
    }

    @Override
    public CompletableFuture<List<Plot>> readAll() {
        @Language("mariadb")
        var queryString = """
                SELECT id, region_id, group_name
                FROM plot
                """;
        return CompletableFuture.supplyAsync(query(queryString)
                .single()
                .map(row -> new Plot(
                        row.getString("plot_id"),
                        row.getString("group_name"),
                        row.getString("region_id")
                ))::all
        );
    }

    @Override
    public CompletableFuture<List<Plot>> readAllFromGroup(String groupName) {
        @Language("mariadb")
        var queryString = """
                SELECT region_id, group_name
                FROM plot
                WHERE group_name = :groupName
                """;
        return CompletableFuture.supplyAsync(query(queryString)
                .single(call().bind("groupName", groupName))
                .map(row -> new Plot(
                        row.getString("plot_id"),
                        row.getString("group_name"),
                        row.getString("region_id")
                ))::all
        );
    }

    @Override
    public CompletableFuture<Boolean> delete(String plotId) {
        @Language("mariadb")
        var queryString = """
                DELETE FROM plot WHERE id = :plotId
                """;
        return CompletableFuture.supplyAsync(query(queryString)
                .single(call().bind("plotId", plotId))
                .delete()::changed
        );
    }
}