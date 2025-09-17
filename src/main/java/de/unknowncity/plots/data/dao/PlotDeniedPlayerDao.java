package de.unknowncity.plots.data.dao;

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

    public Boolean write(PlotPlayer plotPlayer, String plotId) {
        @Language("mariadb")
        var queryString = """
                REPLACE INTO plot_denied (player_id, plot_id)
                VALUES (:playerId, :plotId);
                """;
        return queryConfiguration.query(queryString)
                .single(call()
                        .bind("playerId", plotPlayer.uuid(), UUIDAdapter.AS_STRING)
                        .bind("plotId", plotId)
                )
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

    public Boolean delete(UUID playerId, String plotId) {
        @Language("mariadb")
        var querySting = "DELETE FROM plot_denied WHERE player_id = :playerId AND plot_id = :plotId;";
        return queryConfiguration.query(querySting)
                .single(call()
                        .bind("playerId", String.valueOf(playerId))
                        .bind("plotId", plotId)
                )
                .delete().changed();
    }
}
