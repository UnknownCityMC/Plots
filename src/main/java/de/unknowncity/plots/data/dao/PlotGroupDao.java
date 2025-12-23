package de.unknowncity.plots.data.dao;

import de.chojo.sadu.queries.api.configuration.QueryConfiguration;
import de.unknowncity.plots.plot.group.PlotGroup;
import org.intellij.lang.annotations.Language;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import static de.chojo.sadu.queries.api.call.Call.call;

public class PlotGroupDao {

    private final QueryConfiguration queryConfiguration;
    private final Logger logger = Logger.getLogger(PlotGroupDao.class.getName());

    public PlotGroupDao(QueryConfiguration queryConfiguration) {
        this.queryConfiguration = queryConfiguration;
    }

    public List<PlotGroup> readAll() {
        @Language("mariadb")
        var querySting = "SELECT name, display_item FROM plot_group";
        return queryConfiguration.query(querySting)
                .single()
                .map(row -> {
                    try {
                        var displayItemBytes = row.getBytes("display_item");
                        if (displayItemBytes != null) {
                            return new PlotGroup(
                                    row.getString("name"),
                                    displayItemBytes
                            );
                        }
                        return new PlotGroup(
                                row.getString("name")
                        );
                    } catch (Exception e) {
                        logger.warning("Could not read plot group from database: " + e.getMessage());
                        return null;
                    }
                }).all();
    }

    public Boolean write(PlotGroup plotGroup) {
        @Language("mariadb")
        var querySting = "INSERT INTO plot_group(name, display_item) VALUE (:name, :display_item) ON DUPLICATE KEY UPDATE display_item = VALUES(display_item);";
        return queryConfiguration.query(querySting)
                .single(call()
                        .bind("name", plotGroup.name())
                        .bind("display_item", plotGroup.displayItem().serializeAsBytes()))
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
