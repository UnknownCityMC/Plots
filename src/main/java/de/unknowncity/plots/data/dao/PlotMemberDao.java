package de.unknowncity.plots.data.dao;

import de.chojo.sadu.mapper.reader.StandardReader;
import de.chojo.sadu.queries.api.configuration.ConnectedQueryConfiguration;
import de.chojo.sadu.queries.api.configuration.QueryConfiguration;
import de.chojo.sadu.queries.call.adapter.UUIDAdapter;
import de.unknowncity.plots.plot.model.PlotMember;
import de.unknowncity.plots.plot.access.type.PlotMemberRole;
import org.bukkit.Bukkit;
import org.intellij.lang.annotations.Language;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static de.chojo.sadu.queries.api.call.Call.call;
import static de.chojo.sadu.queries.api.query.Query.query;

public class PlotMemberDao {
    private final QueryConfiguration queryConfiguration;

    public PlotMemberDao(QueryConfiguration queryConfiguration) {
        this.queryConfiguration = queryConfiguration;
    }

    public Optional<PlotMember> read(UUID playerId, String plotId) {
        @Language("mariadb")
        var queryString = """
                SELECT plot_id, player_id, role  FROM plot_member WHERE plot_id = :plotId AND player_id = :playerId;
                """;
        return queryConfiguration.query(queryString)
                .single(call()
                        .bind("plotId", plotId)
                        .bind("playerId", String.valueOf(playerId))
                )
                .map(row -> {
                    var role = row.getEnum("role", PlotMemberRole.class);
                    var name = Bukkit.getOfflinePlayer(playerId).getName();

                    return new PlotMember(
                            plotId,
                            playerId,
                            name,
                            role
                    );
                }).first();
    }

    public Boolean write(ConnectedQueryConfiguration connection, List<PlotMember> plotMembers, String plotId) {
        @Language("mariadb")
        var queryString = """
                INSERT INTO plot_member (
                    player_id,
                    role,
                    plot_id
                )
                VALUES (
                    :playerId,
                    :role,
                    :plotId
                )
                ON DUPLICATE KEY UPDATE
                    role = VALUES(role);
                
                """;
        return connection.query(queryString)
                .batch(plotMembers.stream().map(plotMember -> call()
                        .bind("playerId", plotMember.uuid(), UUIDAdapter.AS_STRING)
                        .bind("plotId", plotId)
                        .bind("role", plotMember.role()))
                )
                .insert().changed();
    }

    public Boolean write(String plotId, PlotMember plotMember) {
        @Language("mariadb")
        var queryString = """
                INSERT INTO plot_member (
                    player_id,
                    role,
                    plot_id
                )
                VALUES (
                    :playerId,
                    :role,
                    :plotId
                )
                ON DUPLICATE KEY UPDATE
                    role = VALUES(role);
                
                """;
        return queryConfiguration.query(queryString)
                .single(call()
                        .bind("playerId", plotMember.uuid(), UUIDAdapter.AS_STRING)
                        .bind("plotId", plotId)
                        .bind("role", plotMember.role())
                )
                .insert().changed();
    }

    public Boolean write(ConnectedQueryConfiguration configuration, String plotId, PlotMember plotMember) {
        @Language("mariadb")
        var queryString = """
                INSERT INTO plot_member (
                    player_id,
                    role,
                    plot_id
                )
                VALUES (
                    :playerId,
                    :role,
                    :plotId
                )
                ON DUPLICATE KEY UPDATE
                    role = VALUES(role);
                
                """;
        return configuration.query(queryString)
                .single(call()
                        .bind("playerId", plotMember.uuid(), UUIDAdapter.AS_STRING)
                        .bind("plotId", plotId)
                        .bind("role", plotMember.role())
                )
                .insert().changed();
    }

    public List<PlotMember> readAll() {
        @Language("mariadb")
        var queryString = """
                SELECT plot_id, player_id, role  FROM plot_member;
                """;
        return queryConfiguration.query(queryString)
                .single()
                .map(row -> {
                    var plotId = row.getString("plot_id");
                    var playerId = row.get("player_id", StandardReader.UUID_FROM_STRING);
                    var role = row.getEnum("role", PlotMemberRole.class);
                    var name = Bukkit.getOfflinePlayer(playerId).getName();

                    return new PlotMember(
                            plotId,
                            playerId,
                            name,
                            role
                    );
                })
                .all();
    }

    public boolean delete(String plotId, UUID playerId) {
        @Language("mariadb")
        var querySting = "DELETE FROM plot_member WHERE player_id = :playerId AND plot_id = :plotId;";
        return queryConfiguration.query(querySting)
                .single(call()
                        .bind("playerId", String.valueOf(playerId))
                        .bind("plotId", plotId)
                )
                .delete().changed();
    }

    public boolean delete(ConnectedQueryConfiguration configuration, String plotId, UUID playerId) {
        @Language("mariadb")
        var querySting = "DELETE FROM plot_member WHERE player_id = :playerId AND plot_id = :plotId;";
        return configuration.query(querySting)
                .single(call()
                        .bind("playerId", String.valueOf(playerId))
                        .bind("plotId", plotId)
                )
                .delete().changed();
    }

    public boolean update(String plotId, PlotMember plotMember) {
        @Language("mariadb")
        var querySting = "UPDATE plot_member SET role = :role WHERE player_id = :playerId AND plot_id = :plotId;";
        return queryConfiguration.query(querySting)
                .single(call()
                        .bind("playerId", plotMember.uuid(), UUIDAdapter.AS_STRING)
                        .bind("plotId", plotId)
                        .bind("role", plotMember.role())
                )
                .update().changed();
    }

    public boolean deleteAll(String plotId) {
        @Language("mariadb")
        var querySting = "DELETE FROM plot_member WHERE plot_id = :plotId;";
        return queryConfiguration.query(querySting)
                .single(call()
                        .bind("plotId", plotId)
                )
                .delete().changed();
    }
}
