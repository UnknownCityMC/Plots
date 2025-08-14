package de.unknowncity.plots.data.dao.mariadb;

import de.chojo.sadu.queries.api.configuration.QueryConfiguration;
import de.unknowncity.plots.data.dao.PlotSignDao;
import de.unknowncity.plots.plot.location.signs.PlotSign;
import org.intellij.lang.annotations.Language;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static de.chojo.sadu.queries.api.call.Call.call;

public class MariaDBPlotSignDao implements PlotSignDao {
    private final QueryConfiguration queryConfiguration;

    public MariaDBPlotSignDao(QueryConfiguration queryConfiguration) {
        this.queryConfiguration = queryConfiguration;
    }

    @Override
    public CompletableFuture<Boolean> writeAll(List<PlotSign> plotSigns, String plotId) {
        @Language("mariadb")
        var queryString = """
                REPLACE INTO plot_sign (plot_id, id, x, y, z)
                VALUES (:plotId, :id, :x, :y, :z)
                """;
        return CompletableFuture.supplyAsync(queryConfiguration.query(queryString)
                .batch(plotSigns.stream().map(plotSign -> call().bind("plotId", plotId)
                        .bind("id", plotSigns.indexOf(plotSign))
                        .bind("x", plotSign.x())
                        .bind("y", plotSign.y())
                        .bind("z", plotSign.z())))
                .insert()::changed
        );
    }

    @Override
    public CompletableFuture<List<PlotSign>> readAll(String plotId) {
        @Language("mariadb")
        var queryString = """
                SELECT x, y, z FROM plot_sign WHERE plot_id = :plotId;
                """;
        return CompletableFuture.supplyAsync(queryConfiguration.query(queryString)
                .single(call().bind("plotId", plotId))
                .map(row -> new PlotSign(
                        row.getInt("x"),
                        row.getInt("y"),
                        row.getInt("z")
                ))::all
        );
    }

    public CompletableFuture<Boolean> delete(String plotId, int id) {
        @Language("mariadb")
        var queryString = """
                DELETE FROM plot_sign WHERE plot_id = :plotId AND id = :id;
                """;

        return CompletableFuture.supplyAsync(queryConfiguration.query(queryString)
                .single(call().bind("plotId", plotId).bind("id", id))
                .delete()::changed
        );
    }

    public CompletableFuture<Boolean> deleteAll(String plotId) {
        @Language("mariadb")
        var queryString = """
                DELETE FROM plot_sign WHERE plot_id = :plotId;
                """;

        return CompletableFuture.supplyAsync(queryConfiguration.query(queryString)
                .single(call().bind("plotId", plotId))
                .delete()::changed
        );
    }
}
