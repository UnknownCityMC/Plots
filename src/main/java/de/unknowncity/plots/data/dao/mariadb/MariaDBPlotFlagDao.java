package de.unknowncity.plots.data.dao.mariadb;

import com.google.gson.Gson;
import de.chojo.sadu.queries.api.configuration.QueryConfiguration;
import de.unknowncity.plots.data.dao.PlotFlagDao;
import de.unknowncity.plots.data.model.PlotFlagWrapper;
import de.unknowncity.plots.plot.flag.FlagRegistry;
import de.unknowncity.plots.plot.flag.PlotFlag;
import org.intellij.lang.annotations.Language;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static de.chojo.sadu.queries.api.call.Call.call;

public class MariaDBPlotFlagDao implements PlotFlagDao {
    private final QueryConfiguration queryConfiguration;
    private final FlagRegistry flagRegistry;
    private final Gson gson = new Gson();

    public MariaDBPlotFlagDao(QueryConfiguration queryConfiguration, FlagRegistry flagRegistry) {
        this.queryConfiguration = queryConfiguration;
        this.flagRegistry = flagRegistry;
    }

    @Override
    public CompletableFuture<List<PlotFlagWrapper<Object>>> readAll(String plotId) {
        @Language("mariadb")
        var querySting = "SELECT flag_id, value FROM plot_flag WHERE plot_id = :plotId";
        return CompletableFuture.supplyAsync(queryConfiguration.query(querySting)
                .single(call()
                        .bind("plotId", plotId)
                )
                .map(row -> {
                    var id = row.getString("flag_id");
                    var valueAsString = row.getString("value");

                    var flag = flagRegistry.getRegistered(id);

                    if (flag == null) {
                        return null;
                    }

                    return new PlotFlagWrapper<Object>((PlotFlag<Object>) flag, flag.unmarshall(valueAsString));
                })::all
        );
    }


    @Override
    public CompletableFuture<Boolean> write(String plotId, String flagId, String value) {
        @Language("mariadb")
        var querySting = """
                REPLACE INTO plot_flag (plot_id, flag_id, value)
                VALUES (:plot_id, :flag_id, :value)
                """;
        return CompletableFuture.supplyAsync(queryConfiguration.query(querySting)
                .single(call()
                        .bind("plot_id", plotId)
                        .bind("flag_id", flagId)
                        .bind("value", value)
                )
                .insert()::changed
        );
    }
}