package de.unknowncity.plots.data.dao;

import com.google.gson.Gson;
import de.chojo.sadu.queries.api.configuration.QueryConfiguration;
import de.unknowncity.plots.data.model.PlotFlagWrapper;
import de.unknowncity.plots.plot.flag.FlagRegistry;
import de.unknowncity.plots.plot.flag.PlotFlag;
import org.intellij.lang.annotations.Language;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static de.chojo.sadu.queries.api.call.Call.call;

public class PlotFlagDao {
    private final QueryConfiguration queryConfiguration;
    private final FlagRegistry flagRegistry;
    private final Gson gson = new Gson();

    public PlotFlagDao(QueryConfiguration queryConfiguration, FlagRegistry flagRegistry) {
        this.queryConfiguration = queryConfiguration;
        this.flagRegistry = flagRegistry;
    }

    public List<PlotFlagWrapper<Object>> readAll() {
        @Language("mariadb")
        var querySting = "SELECT plot_id, flag_id, value FROM plot_flag WHERE plot_id = :plotId";
        return queryConfiguration.query(querySting)
                .single()
                .map(row -> {
                    var plotId = row.getString("plot_id");
                    var flagId = row.getString("flag_id");
                    var valueAsString = row.getString("value");

                    var flag = flagRegistry.getRegistered(flagId);

                    if (flag == null) {
                        return null;
                    }

                    return new PlotFlagWrapper<Object>(plotId, (PlotFlag<Object>) flag, flag.unmarshall(valueAsString));
                }).all();
    }

    public Boolean write(String plotId, String flagId, String value) {
        @Language("mariadb")
        var querySting = """
                REPLACE INTO plot_flag (plot_id, flag_id, value)
                VALUES (:plot_id, :flag_id, :value)
                """;
        return queryConfiguration.query(querySting)
                .single(call()
                        .bind("plot_id", plotId)
                        .bind("flag_id", flagId)
                        .bind("value", value)
                )
                .insert().changed();
    }
}