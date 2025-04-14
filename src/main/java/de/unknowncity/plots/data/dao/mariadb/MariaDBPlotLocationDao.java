package de.unknowncity.plots.data.dao.mariadb;

import de.chojo.sadu.queries.api.configuration.QueryConfiguration;
import de.unknowncity.plots.data.dao.PlotLocationDao;
import de.unknowncity.plots.plot.location.PlotLocationType;
import de.unknowncity.plots.plot.location.RelativePlotLocation;
import org.intellij.lang.annotations.Language;

import java.util.List;

import java.util.concurrent.CompletableFuture;

import static de.chojo.sadu.queries.api.call.Call.call;
import static de.chojo.sadu.queries.api.query.Query.query;

public class MariaDBPlotLocationDao implements PlotLocationDao {
    private final QueryConfiguration queryConfiguration;

    public MariaDBPlotLocationDao(QueryConfiguration queryConfiguration) {
        this.queryConfiguration = queryConfiguration;
    }

    @Override
    public CompletableFuture<Boolean> write(RelativePlotLocation plotLocation, String plotId) {
        @Language("mariadb")
        var queryString = """
                REPLACE INTO plot_location (plot_id, name, x, y, z, yaw, pitch)
                VALUES (:plotId, :name, :x, :y, :z, :yaw, :pitch)
                """;
        return CompletableFuture.supplyAsync(queryConfiguration.query(queryString)
                .single(call()
                        .bind("plotId", plotId)
                        .bind("name", plotLocation.type().toString())
                        .bind("x", plotLocation.x())
                        .bind("y", plotLocation.y())
                        .bind("z", plotLocation.z())
                        .bind("yaw", plotLocation.yaw())
                        .bind("pitch", plotLocation.pitch())
                )
                .insert()::changed
        );
    }

    @Override
    public CompletableFuture<List<RelativePlotLocation>> readAll(String plotId) {
        @Language("mariadb")
        var queryString = """
                SELECT name, x, y, z, yaw, pitch FROM plot_location WHERE plot_id = :plotId;
                """;
        return CompletableFuture.supplyAsync(queryConfiguration.query(queryString)
                .single(call().bind("plotId", plotId))
                .map(row -> new RelativePlotLocation(
                        PlotLocationType.valueOf(row.getString("name")),
                        row.getDouble("x"),
                        row.getDouble("y"),
                        row.getDouble("z"),
                        row.getFloat("yaw"),
                        row.getFloat("pitch")
                ))::all
        );
    }
}
