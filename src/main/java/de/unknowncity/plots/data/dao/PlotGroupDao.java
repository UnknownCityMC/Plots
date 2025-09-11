package de.unknowncity.plots.data.dao;

import de.chojo.sadu.queries.api.configuration.QueryConfiguration;
import de.unknowncity.plots.plot.group.PlotGroup;
import org.intellij.lang.annotations.Language;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static de.chojo.sadu.queries.api.call.Call.call;

public class PlotGroupDao {

    private final QueryConfiguration queryConfiguration;

    public PlotGroupDao(QueryConfiguration queryConfiguration) {
        this.queryConfiguration = queryConfiguration;
    }

    public Optional<PlotGroup> read(String groupName) {
        @Language("mariadb")
        var querySting = "SELECT name FROM plot_group WHERE name = :name";
        return queryConfiguration.query(querySting)
                .single(call().bind("name", groupName))
                .map(row -> new PlotGroup(
                        groupName
                )).first();
    }

    public List<PlotGroup> readAll() {
        @Language("mariadb")
        var querySting = "SELECT name FROM plot_group";
        return queryConfiguration.query(querySting)
                .single()
                .map(row -> new PlotGroup(
                        row.getString("name")
                )).all();
    }

    public Boolean write(PlotGroup plotGroup) {
        @Language("mariadb")
        var querySting = "REPLACE INTO plot_group(name) VALUE (:name)";
        return queryConfiguration.query(querySting)
                .single(call().bind("name", plotGroup.name()))
                .insert().changed();
    }

    public Boolean delete(String groupName) {
        @Language("mariadb")
        var querySting = "DELETE FROM plot_group WHERE name = :name";
        return queryConfiguration.query(querySting)
                .single(call().bind("name", groupName))
                .delete().changed();
    }
}
