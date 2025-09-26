package de.unknowncity.plots.data.dao;

import com.google.gson.Gson;
import de.chojo.sadu.queries.api.configuration.ConnectedQueryConfiguration;
import de.chojo.sadu.queries.api.configuration.QueryConfiguration;
import de.chojo.sadu.queries.configuration.ConnectedQueryConfigurationImpl;
import de.unknowncity.plots.data.model.PlotFlagWrapper;
import de.unknowncity.plots.plot.flag.FlagRegistry;
import de.unknowncity.plots.plot.flag.PlotFlag;
import org.intellij.lang.annotations.Language;

import java.sql.Connection;
import java.util.List;
import java.util.Map;
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
        var querySting = "SELECT plot_id, flag_id, value FROM plot_flag";
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

    public Boolean write(ConnectedQueryConfiguration connection, String plotId, Map<PlotFlag<?>, ?> plotFlags) {
        @Language("mariadb")
        var querySting = """
                INSERT INTO plot_flag (
                    plot_id,
                    flag_id,
                    value
                )
                VALUES (
                    :plotId,
                    :flagId,
                    :value
                )
                ON DUPLICATE KEY UPDATE
                    value = VALUES(value);
                """;
        return connection.query(querySting)
                .batch(plotFlags.keySet().stream().map(plotFlag ->
                                call().bind("plotId", plotId)
                                        .bind("flagId", plotFlag.flagId())
                                        .bind("value", plotFlag.marshall(plotFlags.get(plotFlag)))
                        )
                )
                .insert().changed();
    }
}