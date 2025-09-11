package de.unknowncity.plots.data.dao;

import de.chojo.sadu.queries.api.configuration.QueryConfiguration;
import de.unknowncity.plots.plot.access.type.PlotAccessModifier;
import de.unknowncity.plots.plot.flag.PlotInteractable;
import org.bukkit.Material;
import org.intellij.lang.annotations.Language;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static de.chojo.sadu.queries.api.call.Call.call;

public class PlotInteractablesDao {
    private final QueryConfiguration queryConfiguration;

    public PlotInteractablesDao(QueryConfiguration queryConfiguration) {
        this.queryConfiguration = queryConfiguration;
    }

    public List<PlotInteractable> readAll() {
        @Language("mariadb")
        var querySting = "SELECT plot_id, block_type, access_modifier FROM plot_interactables";
        return queryConfiguration.query(querySting)
                .single()
                .map(row -> {
                            var plotId = row.getString("plot_id");
                            var material = Material.valueOf(row.getString("block_type"));
                            var accessModifier = row.getEnum("access_modifier", PlotAccessModifier.class);

                            return PlotInteractable.create(plotId, material, accessModifier);
                        }
                ).all();
    }

    public Boolean write(PlotInteractable plotInteractable, String plotId) {
        @Language("mariadb")
        var querySting = """
                REPLACE INTO plot_interactables (block_type, plot_id, access_modifier)
                VALUES (:block_type, :plot_id, :access_modifier)
                """;
        return queryConfiguration.query(querySting)
                .single(call()
                        .bind("plot_id", plotId)
                        .bind("block_type", plotInteractable.blockType().name())
                        .bind("access_modifier", plotInteractable.accessModifier())
                )
                .insert().changed();
    }
}
