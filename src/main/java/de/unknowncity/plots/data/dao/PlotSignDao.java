package de.unknowncity.plots.data.dao;

import de.chojo.sadu.queries.api.configuration.QueryConfiguration;
import de.unknowncity.plots.plot.location.signs.PlotSign;
import org.intellij.lang.annotations.Language;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static de.chojo.sadu.queries.api.call.Call.call;

public class PlotSignDao {
    private final QueryConfiguration queryConfiguration;

    public PlotSignDao(QueryConfiguration queryConfiguration) {
        this.queryConfiguration = queryConfiguration;
    }

    public Boolean writeAll(List<PlotSign> plotSigns, String plotId) {
        @Language("mariadb")
        var queryString = """
                REPLACE INTO plot_sign (plot_id, id, x, y, z)
                VALUES (:plotId, :id, :x, :y, :z)
                """;
        return queryConfiguration.query(queryString)
                .batch(plotSigns.stream().map(plotSign -> call().bind("plotId", plotId)
                        .bind("id", plotSigns.indexOf(plotSign))
                        .bind("x", plotSign.x())
                        .bind("y", plotSign.y())
                        .bind("z", plotSign.z())))
                .insert().changed();
    }

    public List<PlotSign> readAll() {
        @Language("mariadb")
        var queryString = """
                SELECT plot_id, x, y, z FROM plot_sign WHERE plot_id = :plotId;
                """;
        return queryConfiguration.query(queryString)
                .single()
                .mapAs(PlotSign.class)
                .all();
    }

    public Boolean delete(String plotId, int id) {
        @Language("mariadb")
        var queryString = """
                DELETE FROM plot_sign WHERE plot_id = :plotId AND id = :id;
                """;

        return queryConfiguration.query(queryString)
                .single(call().bind("plotId", plotId).bind("id", id))
                .delete().changed();
    }

    public Boolean deleteAll(String plotId) {
        @Language("mariadb")
        var queryString = """
                DELETE FROM plot_sign WHERE plot_id = :plotId;
                """;

        return queryConfiguration.query(queryString)
                .single(call().bind("plotId", plotId))
                .delete().changed();
    }
}
