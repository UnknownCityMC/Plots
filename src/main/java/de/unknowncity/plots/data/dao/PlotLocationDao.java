package de.unknowncity.plots.data.dao;

import de.chojo.sadu.queries.api.configuration.QueryConfiguration;
import de.unknowncity.plots.plot.location.PlotLocation;
import org.intellij.lang.annotations.Language;

import java.util.List;

import static de.chojo.sadu.queries.api.call.Call.call;

public class PlotLocationDao {
    private final QueryConfiguration queryConfiguration;

    public PlotLocationDao(QueryConfiguration queryConfiguration) {
        this.queryConfiguration = queryConfiguration;
    }

    public Boolean write(QueryConfiguration connection, String plotId, PlotLocation plotLocation) {
        @Language("mariadb")
        var queryString = """
                INSERT INTO plot_location (
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
                        .bind("name", plotLocation.name())
                        .bind("public", plotLocation.isPublic())
                        .bind("x", plotLocation.x())
                        .bind("y", plotLocation.y())
                        .bind("z", plotLocation.z())
                        .bind("yaw", plotLocation.yaw())
                        .bind("pitch", plotLocation.pitch())
                )
                .insert().changed();
    }

    public List<PlotLocation> readAll() {
        @Language("mariadb")
        var queryString = """
                SELECT plot_id, name, public, x, y, z, yaw, pitch FROM plot_location;
                """;
        return queryConfiguration.query(queryString)
                .single()
                .map(row -> new PlotLocation(
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
