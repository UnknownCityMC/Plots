package de.unknowncity.plots.data.dao.mariadb;

import de.chojo.sadu.queries.api.configuration.QueryConfiguration;
import de.unknowncity.plots.data.dao.PlotFlagDao;
import de.unknowncity.plots.data.model.plot.flag.PlotFlag;
import de.unknowncity.plots.data.model.plot.flag.PlotFlagAccessModifier;
import org.intellij.lang.annotations.Language;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static de.chojo.sadu.queries.api.call.Call.call;
import static de.chojo.sadu.queries.api.query.Query.query;

public class MariaDBPlotFlagDao implements PlotFlagDao {
    private final QueryConfiguration queryConfiguration;

    public MariaDBPlotFlagDao(QueryConfiguration queryConfiguration) {
        this.queryConfiguration = queryConfiguration;
    }

    @Override
    public CompletableFuture<List<PlotFlag>> readAll(String plotId) {
        @Language("mariadb")
        var querySting = "SELECT action_id, access_modifier FROM plot_flag WHERE plot_id = :plotId";
        return CompletableFuture.supplyAsync(queryConfiguration.query(querySting)
                .single(call()
                        .bind("plotId", plotId)
                )
                .map(row -> PlotFlag.create(
                        row.getString("actionId"),
                        row.getEnum("access_modifier", PlotFlagAccessModifier.class)
                ))::all
        );
    }

    @Override
    public CompletableFuture<Boolean> write(PlotFlag plotFlag, String plotId) {
        @Language("mariadb")
        var querySting = """
                REPLACE INTO plot_flag (action_id, plot_id, access_modifier)
                VALUES (:action_id, :plot_id, :access_modifier)
                """;
        return CompletableFuture.supplyAsync(queryConfiguration.query(querySting)
                .single(call()
                        .bind("actionId", plotFlag.actionId())
                        .bind("plot_id", plotId)
                        .bind("access_modifier", plotFlag.accessModifier())
                )
                .insert()::changed
        );
    }
}
