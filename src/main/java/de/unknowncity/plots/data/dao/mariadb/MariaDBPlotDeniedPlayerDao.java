package de.unknowncity.plots.data.dao.mariadb;

import de.chojo.sadu.mapper.reader.StandardReader;
import de.chojo.sadu.queries.api.configuration.QueryConfiguration;
import de.chojo.sadu.queries.call.adapter.UUIDAdapter;
import de.unknowncity.plots.data.dao.PlotDeniedPlayerDao;
import de.unknowncity.plots.plot.access.entity.PlotPlayer;
import org.bukkit.Bukkit;
import org.intellij.lang.annotations.Language;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static de.chojo.sadu.queries.api.call.Call.call;

public class MariaDBPlotDeniedPlayerDao implements PlotDeniedPlayerDao {
    private final QueryConfiguration queryConfiguration;

    public MariaDBPlotDeniedPlayerDao(QueryConfiguration queryConfiguration) {
        this.queryConfiguration = queryConfiguration;
    }

    @Override
    public CompletableFuture<Optional<PlotPlayer>> read(UUID memberId, String plotId) {
        @Language("mariadb")
        var queryString = """
                SELECT user_id  FROM plot_denied WHERE plot_id = :plotId AND user_id = :userId;
                """;
        return CompletableFuture.supplyAsync(queryConfiguration.query(queryString)
                .single(call()
                        .bind("plotId", plotId)
                        .bind("userId", String.valueOf(memberId))
                )
                .map(row -> {
                            var name = Bukkit.getOfflinePlayer(memberId).getName();

                            return new PlotPlayer(
                                    memberId,
                                    name
                            );
                        }
                )::first
        );
    }

    @Override
    public CompletableFuture<Boolean> write(PlotPlayer plotPlayer, String plotId) {
        @Language("mariadb")
        var queryString = """
                REPLACE INTO plot_denied (user_id, plot_id)
                VALUES (:userId, :plotId);
                """;
        return CompletableFuture.supplyAsync(queryConfiguration.query(queryString)
                .single(call()
                        .bind("userId", plotPlayer.uuid(), UUIDAdapter.AS_STRING)
                        .bind("plotId", plotId)
                )
                .insert()::changed
        );
    }

    @Override
    public CompletableFuture<List<PlotPlayer>> readAll(String plotId) {
        @Language("mariadb")
        var queryString = """
                SELECT user_id FROM plot_denied WHERE plot_id = :plotId;;
                """;
        return CompletableFuture.supplyAsync(queryConfiguration.query(queryString)
                .single(call()
                        .bind("plotId", plotId)
                )
                .map(row -> {
                            var memberId = row.get("user_id", StandardReader.UUID_FROM_STRING);
                            var name = Bukkit.getOfflinePlayer(memberId).getName();

                            return new PlotPlayer(
                                    memberId,
                                    name
                            );
                        }
                )::all
        );
    }

    @Override
    public CompletableFuture<Boolean> delete(UUID playerId, String plotId) {
        @Language("mariadb")
        var querySting = "DELETE FROM plot_denied WHERE user_id = :userId AND plot_id = :plotId;";
        return CompletableFuture.supplyAsync(queryConfiguration.query(querySting)
                .single(call()
                        .bind("userId", String.valueOf(playerId))
                        .bind("plotId", plotId)
                )
                .delete()::changed
        );
    }
}
