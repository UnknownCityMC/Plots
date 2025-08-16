package de.unknowncity.plots.data.dao.mariadb;

import de.chojo.sadu.mapper.reader.StandardReader;
import de.chojo.sadu.queries.api.configuration.QueryConfiguration;
import de.chojo.sadu.queries.call.adapter.UUIDAdapter;
import de.unknowncity.plots.data.dao.PlotMemberDao;
import de.unknowncity.plots.plot.access.entity.PlotMember;
import de.unknowncity.plots.plot.access.type.PlotMemberRole;
import org.bukkit.Bukkit;
import org.intellij.lang.annotations.Language;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static de.chojo.sadu.queries.api.call.Call.call;
import static de.chojo.sadu.queries.api.query.Query.query;

public class MariaDBPlotMemberDao implements PlotMemberDao {
    private final QueryConfiguration queryConfiguration;

    public MariaDBPlotMemberDao(QueryConfiguration queryConfiguration) {
        this.queryConfiguration = queryConfiguration;
    }

    @Override
    public CompletableFuture<Optional<PlotMember>> read(UUID memberId, String plotId) {
        @Language("mariadb")
        var queryString = """
                SELECT user_id, role  FROM plot_member WHERE plot_id = :plotId AND user_id = :userId;
                """;
        return CompletableFuture.supplyAsync(queryConfiguration.query(queryString)
                .single(call()
                        .bind("plotId", plotId)
                        .bind("userId", String.valueOf(memberId))
                )
                .map(row -> {
                            var role = row.getEnum("role", PlotMemberRole.class);
                            var name = Bukkit.getOfflinePlayer(memberId).getName();

                            return new PlotMember(
                                    memberId,
                                    name,
                                    role
                            );
                        }
                )::first
        );
    }

    @Override
    public CompletableFuture<Boolean> write(PlotMember plotMember, String plotId) {
        @Language("mariadb")
        var queryString = """
                REPLACE INTO plot_member (user_id, role, plot_id)
                VALUES (:userId, :role, :plotId);
                """;
        return CompletableFuture.supplyAsync(queryConfiguration.query(queryString)
                .single(call()
                        .bind("userId", plotMember.uuid(), UUIDAdapter.AS_STRING)
                        .bind("plotId", plotId)
                        .bind("role", plotMember.role())
                )
                .insert()::changed
        );
    }

    @Override
    public CompletableFuture<List<PlotMember>> readAll(String plotId) {
        @Language("mariadb")
        var queryString = """
                SELECT user_id, role  FROM plot_member WHERE plot_id = :plotId;;
                """;
        return CompletableFuture.supplyAsync(queryConfiguration.query(queryString)
                .single(call()
                        .bind("plotId", plotId)
                )
                .map(row -> {
                            var memberId = row.get("user_id", StandardReader.UUID_FROM_STRING);
                            var role = row.getEnum("role", PlotMemberRole.class);
                            var name = Bukkit.getOfflinePlayer(memberId).getName();

                            return new PlotMember(
                                    memberId,
                                    name,
                                    role
                            );
                        }
                )::all
        );
    }

    @Override
    public CompletableFuture<Boolean> delete(UUID memberId, String plotId) {
        @Language("mariadb")
        var querySting = "DELETE FROM plot_member WHERE user_id = :userId AND plot_id = :plotId;";
        return CompletableFuture.supplyAsync(queryConfiguration.query(querySting)
                .single(call()
                        .bind("userId", String.valueOf(memberId))
                        .bind("plotId", plotId)
                )
                .delete()::changed
        );
    }
}
