package de.unknowncity.plots.data.dao.mariadb;

import de.chojo.sadu.mapper.reader.StandardReader;
import de.unknowncity.plots.data.dao.PlotMetaDao;
import de.unknowncity.plots.data.model.plot.Plot;
import de.unknowncity.plots.data.model.plot.PlotMeta;
import de.unknowncity.plots.data.model.plot.PlotPaymentType;
import de.unknowncity.plots.data.model.plot.PlotState;
import org.intellij.lang.annotations.Language;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static de.chojo.sadu.queries.api.call.Call.call;
import static de.chojo.sadu.queries.api.query.Query.query;

public class MariaDBPlotMetaDao implements PlotMetaDao {
    @Override
    public CompletableFuture<Optional<PlotMeta>> read(String plotId) {
        @Language("mariadb")
        var queryString = """
                SELECT state, price, payment_type, rent_interval, last_rent_paid
                FROM plot_meta
                WHERE plot_id = :plotId
                """;
        return CompletableFuture.supplyAsync(query(queryString)
                .single(call().bind("plotId", plotId))
                .map(row -> new PlotMeta(
                        row.getEnum("state", PlotState.class),
                        row.getEnum("payment_type", PlotPaymentType.class),
                        row.getDouble("price"),
                        row.get("last_rent_paid", StandardReader.LOCAL_DATE_TIME),
                        row.getLong("rent_interval")
                ))::first
        );
    }

    @Override
    public CompletableFuture<Boolean> write(PlotMeta plotMeta, String plotId) {
        @Language("mariadb")
        var queryString = """
                REPLACE INTO plot_meta (plot_id, state, price, payment_type, rent_interval, last_rent_paid)
                VALUES (:plotId, :state, :price, :paymentType, :rentInterval, :lastRentPaid)
                """;
        return CompletableFuture.supplyAsync(query(queryString)
                .single(call()
                        .bind("plotId", plotId)
                        .bind("state", plotId)
                        .bind("price", plotId)
                        .bind("paymentType", plotId)
                        .bind("rentInterval", plotId)
                        .bind("lastPayedRent", plotId)
                )
                .insert()::changed
        );
    }
}