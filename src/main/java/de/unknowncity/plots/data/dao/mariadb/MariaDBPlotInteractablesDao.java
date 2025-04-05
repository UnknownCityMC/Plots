package de.unknowncity.plots.data.dao.mariadb;

import de.chojo.sadu.queries.api.configuration.QueryConfiguration;
import de.unknowncity.plots.data.dao.PlotInteractablesDao;
import de.unknowncity.plots.plot.access.type.PlotAccessModifier;
import de.unknowncity.plots.plot.flag.PlotInteractable;
import org.bukkit.Material;
import org.intellij.lang.annotations.Language;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static de.chojo.sadu.queries.api.call.Call.call;

public class MariaDBPlotInteractablesDao implements PlotInteractablesDao {
    private final QueryConfiguration queryConfiguration;

    public MariaDBPlotInteractablesDao(QueryConfiguration queryConfiguration) {
        this.queryConfiguration = queryConfiguration;
    }

    @Override
    public CompletableFuture<List<PlotInteractable>> readAll(String plotId) {
        @Language("mariadb")
        var querySting = "SELECT block_type, access_modifier FROM plot_interactables WHERE plot_id = :plotId";
        return CompletableFuture.supplyAsync(queryConfiguration.query(querySting)
                .single(call()
                        .bind("plotId", plotId)
                )
                .map(row -> PlotInteractable.create(
                        Material.valueOf(row.getString("block_type")),
                        row.getEnum("access_modifier", PlotAccessModifier.class)
                ))::all
        );
    }

    @Override
    public CompletableFuture<Boolean> write(PlotInteractable plotInteractable, String plotId) {
        @Language("mariadb")
        var querySting = """
                REPLACE INTO plot_interactables (block_type, plot_id, access_modifier)
                VALUES (:block_type, :plot_id, :access_modifier)
                """;
        return CompletableFuture.supplyAsync(queryConfiguration.query(querySting)
                .single(call()
                        .bind("plot_id", plotId)
                        .bind("block_type", plotInteractable.blockType().name())
                        .bind("access_modifier", plotInteractable.accessModifier())
                )
                .insert()::changed
        );
    }
}
