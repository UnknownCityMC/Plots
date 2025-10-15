package de.unknowncity.plots.data.dao;

import de.chojo.sadu.queries.api.configuration.QueryConfiguration;
import de.unknowncity.plots.plot.location.PlotHome;
import org.intellij.lang.annotations.Language;

import java.util.List;

import static de.chojo.sadu.queries.api.call.Call.call;

public class PlotHomeDao {
    private final QueryConfiguration queryConfiguration;

    public PlotHomeDao(QueryConfiguration queryConfiguration) {
        this.queryConfiguration = queryConfiguration;
    }

    public Boolean write(QueryConfiguration connection, String plotId, PlotHome plotHome) {
        @Language("mariadb")
        var queryString = """
                INSERT INTO plot_home (
                    plot_id,
                    name,
                    public,
                    x,
                    y,
                    z,
                    yaw,
                    pitch
                )
                VALUES (
                    :plotId,
                    :name,
                    :public,
                    :x,
                    :y,
                    :z,
                    :yaw,
                    :pitch
                )
                ON DUPLICATE KEY UPDATE
                    public = VALUES(public),
                    x      = VALUES(x),
                    y      = VALUES(y),
                    z      = VALUES(z),
                    yaw    = VALUES(yaw),
                    pitch  = VALUES(pitch);
                
                """;
        return connection.query(queryString)
                .single(call()
                        .bind("plotId", plotId)
                        .bind("name", plotHome.name())
                        .bind("public", plotHome.isPublic())
                        .bind("x", plotHome.x())
                        .bind("y", plotHome.y())
                        .bind("z", plotHome.z())
                        .bind("yaw", plotHome.yaw())
                        .bind("pitch", plotHome.pitch())
                )
                .insert().changed();
    }

    public List<PlotHome> readAll() {
        @Language("mariadb")
        var queryString = """
                SELECT plot_id, name, public, x, y, z, yaw, pitch FROM plot_home;
                """;
        return queryConfiguration.query(queryString)
                .single()
                .map(row -> new PlotHome(
                        row.getString("plot_id"),
                        row.getString("name"),
                        row.getBoolean("public"),
                        row.getDouble("x"),
                        row.getDouble("y"),
                        row.getDouble("z"),
                        row.getFloat("yaw"),
                        row.getFloat("pitch")
                )).all();
    }
}
