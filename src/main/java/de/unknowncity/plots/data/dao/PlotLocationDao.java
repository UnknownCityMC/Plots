package de.unknowncity.plots.data.dao;

import de.chojo.sadu.queries.api.configuration.QueryConfiguration;
import de.unknowncity.plots.plot.location.PlotLocation;
import org.intellij.lang.annotations.Language;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static de.chojo.sadu.queries.api.call.Call.call;
import static de.chojo.sadu.queries.api.query.Query.query;

public class PlotLocationDao {
    private final QueryConfiguration queryConfiguration;

    public PlotLocationDao(QueryConfiguration queryConfiguration) {
        this.queryConfiguration = queryConfiguration;
    }

    public Boolean write(PlotLocation plotLocation, String plotId) {
        @Language("mariadb")
        var queryString = """
                REPLACE INTO plot_location (plot_id, name, public, x, y, z, yaw, pitch)
                VALUES (:plotId, :name, :public, :x, :y, :z, :yaw, :pitch)
                """;
        return queryConfiguration.query(queryString)
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
                .mapAs(PlotLocation.class).all();
    }
}
