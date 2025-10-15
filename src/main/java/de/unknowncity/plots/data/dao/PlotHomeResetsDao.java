package de.unknowncity.plots.data.dao;

import de.chojo.sadu.queries.api.call.Call;
import de.chojo.sadu.queries.api.configuration.QueryConfiguration;
import de.unknowncity.plots.plot.PlotLocations;
import de.unknowncity.plots.plot.location.PlotHome;
import de.unknowncity.plots.plot.location.PlotPosition;
import org.intellij.lang.annotations.Language;

import java.util.List;
import java.util.Optional;

import static de.chojo.sadu.queries.api.call.Call.call;

public class PlotHomeResetsDao {
    private final QueryConfiguration queryConfiguration;

    public PlotHomeResetsDao(QueryConfiguration queryConfiguration) {
        this.queryConfiguration = queryConfiguration;
    }

    public Boolean write(QueryConfiguration connection, String plotId, PlotPosition plotHomeResetLocation) {
        @Language("mariadb")
        var queryString = """
                INSERT INTO plot_home_resets (
                    plot_id,
                    x,
                    y,
                    z,
                    yaw,
                    pitch
                )
                VALUES (
                    :plotId,
                    :x,
                    :y,
                    :z,
                    :yaw,
                    :pitch
                )
                ON DUPLICATE KEY UPDATE
                    x      = VALUES(x),
                    y      = VALUES(y),
                    z      = VALUES(z),
                    yaw    = VALUES(yaw),
                    pitch  = VALUES(pitch);
                
                """;
        return connection.query(queryString)
                .single(call()
                        .bind("plotId", plotId)
                        .bind("x", plotHomeResetLocation.x())
                        .bind("y", plotHomeResetLocation.y())
                        .bind("z", plotHomeResetLocation.z())
                        .bind("yaw", plotHomeResetLocation.yaw())
                        .bind("pitch", plotHomeResetLocation.pitch())
                )
                .insert().changed();
    }

    public Optional<PlotPosition> read(String plotId) {
        @Language("mariadb")
        var queryString = """
                SELECT x, y, z, yaw, pitch FROM plot_home_resets WHERE plot_id = :plotId;
                """;
        return queryConfiguration.query(queryString).single(Call.call().bind("plotId", plotId)).map(row -> {
            var x = row.getDouble("x");
            var y = row.getDouble("y");
            var z = row.getDouble("z");
            var yaw = row.getFloat("yaw");
            var pitch = row.getFloat("pitch");

            return new PlotPosition(plotId, x, y, z, yaw, pitch);
        }).first();
    }

    public List<PlotPosition> readAll() {
        @Language("mariadb")
        var queryString = """
                SELECT plot_id, x, y, z, yaw, pitch FROM plot_home_resets;
                """;
        return queryConfiguration.query(queryString)
                .single()
                .map(row -> new PlotPosition(
                        row.getString("plot_id"),
                        row.getDouble("x"),
                        row.getDouble("y"),
                        row.getDouble("z"),
                        row.getFloat("yaw"),
                        row.getFloat("pitch")
                )).all();
    }
}
