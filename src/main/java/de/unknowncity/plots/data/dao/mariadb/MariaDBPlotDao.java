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
    public CompletableFuture<Optional<? extends Plot>> read(String plotId) {
        @Language("mariadb")
        var queryString = """
                SELECT id, region_id, group_name, world, state, payment_type, price, rent_interval, last_rent_paid
                FROM plot
                WHERE id = :plotId
                """;
        return CompletableFuture.supplyAsync(query(queryString)
                .single(call().bind("plotId", plotId))
                .map(Plot.map())::first
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
    public CompletableFuture<List<? extends Plot>> readAll() {
        @Language("mariadb")
        var queryString = """
                SELECT id, region_id, group_name
                FROM plot
                """;
        return CompletableFuture.supplyAsync(query(queryString)
                .single()
                .map(Plot.map())::all
        );
    }

    @Override
    public CompletableFuture<List<? extends Plot>> readAllFromGroup(String groupName) {
        @Language("mariadb")
        var queryString = """
                SELECT region_id, group_name
                FROM plot
                WHERE group_name = :groupName
                """;
        return CompletableFuture.supplyAsync(query(queryString)
                .single(call().bind("groupName", groupName))
                .map(Plot.map())::all
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