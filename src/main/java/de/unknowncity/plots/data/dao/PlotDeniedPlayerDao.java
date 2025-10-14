package de.unknowncity.plots.data.dao;

import de.chojo.sadu.queries.api.configuration.ConnectedQueryConfiguration;
import de.chojo.sadu.queries.api.configuration.QueryConfiguration;
import de.chojo.sadu.queries.call.adapter.UUIDAdapter;
import de.unknowncity.plots.plot.model.PlotPlayer;
import org.intellij.lang.annotations.Language;

import java.util.List;
import java.util.UUID;

import static de.chojo.sadu.queries.api.call.Call.call;

public class PlotDeniedPlayerDao {
    private final QueryConfiguration queryConfiguration;

    public PlotDeniedPlayerDao(QueryConfiguration queryConfiguration) {
        this.queryConfiguration = queryConfiguration;
    }

    public void write(ConnectedQueryConfiguration configuration, String plotId, PlotPlayer plotPlayer) {
        @Language("mariadb")
        var queryString = """
                       INSERT INTO plot_denied (
                           player_id,
                           plot_id
                       )
                       VALUES (
                           :playerId,
                           :plotId
                           )
                ON DUPLICATE KEY UPDATE
                           player_id = VALUES(player_id);
                """;

        configuration.query(queryString)
                .single(call()
                        .bind("playerId", plotPlayer.uuid(), UUIDAdapter.AS_STRING)
                        .bind("plotId", plotId))
                .insert().changed();
    }

    public void write(String plotId, PlotPlayer plotPlayer) {
        @Language("mariadb")
        var queryString = """
                       INSERT INTO plot_denied (
                           player_id,
                           plot_id
                       )
                       VALUES (
                           :playerId,
                           :plotId
                           )
                ON DUPLICATE KEY UPDATE
                           player_id = VALUES(player_id);
                """;

        queryConfiguration.query(queryString)
                .single(call()
                        .bind("playerId", plotPlayer.uuid(), UUIDAdapter.AS_STRING)
                        .bind("plotId", plotId))
                .insert().changed();
    }

    public List<PlotPlayer> readAll() {
        @Language("mariadb")
        var queryString = """
                SELECT plot_id, player_id FROM plot_denied;
                """;
        return queryConfiguration.query(queryString)
                .single()
                .mapAs(PlotPlayer.class)
                .all();
    }

    public boolean delete(String plotId, UUID playerId) {
        @Language("mariadb")
        var querySting = "DELETE FROM plot_denied WHERE player_id = :playerId AND plot_id = :plotId;";
        return queryConfiguration.query(querySting)
                .single(call()
                        .bind("playerId", String.valueOf(playerId))
                        .bind("plotId", plotId)
                )
                .delete().changed();
    }

    public boolean delete(ConnectedQueryConfiguration configuration, String plotId, UUID playerId) {
        @Language("mariadb")
        var querySting = "DELETE FROM plot_denied WHERE player_id = :playerId AND plot_id = :plotId;";
        return configuration.query(querySting)
                .single(call()
                        .bind("playerId", String.valueOf(playerId))
                        .bind("plotId", plotId)
                )
                .delete().changed();
    }

    public boolean deleteAll(String plotId) {
        @Language("mariadb")
        var querySting = "DELETE FROM plot_denied WHERE plot_id = :plotId;";
        return queryConfiguration.query(querySting)
                .single(call()
                        .bind("plotId", plotId)
                )
                .delete().changed();
    }
}
