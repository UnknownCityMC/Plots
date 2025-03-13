package de.unknowncity.plots.data.dao.mariadb;

import de.chojo.sadu.queries.api.configuration.QueryConfiguration;
import de.unknowncity.plots.data.dao.PlotLocationDao;
import de.unknowncity.plots.data.model.plot.PlotLocationType;
import de.unknowncity.plots.data.model.plot.RelativePlotLocation;
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
                REPLACE INTO plot_location (plot_id, type, x, y, z, yaw, pitch)
                VALUES (:plotId, :type, :x, :y, :z, :yaw, :pitch)
                """;
        return CompletableFuture.supplyAsync(queryConfiguration.query(queryString)
                .single(call()
                        .bind("plotId", plotId)
                        .bind("type", plotLocation.type())
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
                SELECT type, x, y, z, yaw, pitch FROM plot_location WHERE plot_id = :plotId;
                """;
        return CompletableFuture.supplyAsync(queryConfiguration.query(queryString)
                .single(call().bind("plotId", plotId))
                .map(row -> new RelativePlotLocation(
                        row.getEnum("type", PlotLocationType.class),
                        row.getDouble("x"),
                        row.getDouble("y"),
                        row.getDouble("z"),
                        row.getDouble("yaw"),
                        row.getDouble("pitch")
                ))::all
        );
    }
}
