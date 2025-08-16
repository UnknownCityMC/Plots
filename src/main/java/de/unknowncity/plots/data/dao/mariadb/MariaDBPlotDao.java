package de.unknowncity.plots.data.dao.mariadb;

import de.chojo.sadu.mapper.reader.StandardReader;
import de.chojo.sadu.mapper.wrapper.Row;
import de.chojo.sadu.queries.api.configuration.QueryConfiguration;
import de.unknowncity.plots.data.dao.PlotDao;
import de.unknowncity.plots.plot.BuyPlot;
import de.unknowncity.plots.plot.Plot;
import de.unknowncity.plots.plot.RentPlot;
import de.unknowncity.plots.plot.access.PlotState;
import de.unknowncity.plots.plot.access.entity.PlotPlayer;
import de.unknowncity.plots.plot.economy.PlotPaymentType;
import org.bukkit.Bukkit;
import org.intellij.lang.annotations.Language;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static de.chojo.sadu.queries.api.call.Call.*;

public class MariaDBPlotDao implements PlotDao {
    private final QueryConfiguration queryConfiguration;
    
    public MariaDBPlotDao(QueryConfiguration queryConfiguration) {
        this.queryConfiguration = queryConfiguration;
    }

    @Override
    public CompletableFuture<Optional<? extends Plot>> read(String plotId) {
        @Language("mariadb")
        var queryString = """
                SELECT id, owner_id, region_id, group_name, world, state, payment_type, price, rent_interval, last_rent_paid
                FROM plot
                WHERE id = :plotId
                """;
        return CompletableFuture.supplyAsync(queryConfiguration.query(queryString)
                .single(call().bind("plotId", plotId))
                .map(row -> {
                    var paymentType = row.getEnum("payment_type", PlotPaymentType.class);

                    var ownerId = row.get("owner_id", StandardReader.UUID_FROM_STRING);
                    var owner = ownerId != null ? new PlotPlayer(
                            ownerId,
                            Bukkit.getOfflinePlayer(ownerId).getName()
                    ) : null;

                    if (paymentType == PlotPaymentType.BUY) {
                        return new BuyPlot(
                                plotId,
                                owner,
                                row.getString("group_name"),
                                row.getString("region_id"),
                                row.getDouble("price"),
                                row.getString("world"),
                                row.getEnum("state", PlotState.class),
                                row.get("claimed", StandardReader.LOCAL_DATE_TIME)
                        );
                    } else {
                        return new RentPlot(
                                plotId,
                                owner,
                                row.getString("group_name"),
                                row.getString("region_id"),
                                row.getDouble("price"),
                                row.getString("world"),
                                row.getEnum("state", PlotState.class),
                                row.get("claimed", StandardReader.LOCAL_DATE_TIME),
                                row.get("last_rent_paid", StandardReader.LOCAL_DATE_TIME),
                                row.getLong("rent_interval")
                        );
                    }
                })::first
        );
    }

    @Override
    public CompletableFuture<Boolean> write(Plot plot) {
        @Language("mariadb")
        var queryString = """
                REPLACE INTO plot (id, owner_id, region_id, group_name, world, state, payment_type, price, rent_interval, last_rent_paid)
                VALUES (:plotId, :ownerId, :regionId, :groupName, :world, :state, :paymentType, :price, :rentInterval, :lastRentPaid)
                """;
        return CompletableFuture.supplyAsync(queryConfiguration.query(queryString)
                .single(call()
                        .bind("plotId", plot.id())
                        .bind("ownerId", String.valueOf(plot.owner() == null ? "00000000-0000-0000-0000-000000000000" : plot.owner()))
                        .bind("regionId", plot.regionId())
                        .bind("groupName", plot.groupName())
                        .bind("world", plot.worldName())
                        .bind("state", plot.state())
                        .bind("paymentType", plot instanceof BuyPlot ? PlotPaymentType.BUY : PlotPaymentType.RENT)
                        .bind("price", plot.price())
                        .bind("rentInterval", plot instanceof RentPlot rentPlot ? rentPlot.rentIntervalInMin() : 0L)
                        .bind("lastRentPaid", plot instanceof RentPlot rentPlot ? rentPlot.lastRentPayed() : null)
                )
                .insert()::changed
        );
    }

    @Override
    public CompletableFuture<List<? extends Plot>> readAll() {
        @Language("mariadb")
        var queryString = """
                SELECT id, owner_id, region_id, group_name, world, state, payment_type, price, rent_interval, last_rent_paid, claimed
                FROM plot
                """;
        return CompletableFuture.supplyAsync(queryConfiguration.query(queryString)
                .single()
                .map(row -> {
                    var paymentType = row.getEnum("payment_type", PlotPaymentType.class);
                    var plotId = row.getString("id");

                    var ownerId = row.get("owner_id", StandardReader.UUID_FROM_STRING);
                    var owner = ownerId != null ? new PlotPlayer(
                            ownerId,
                            Bukkit.getOfflinePlayer(ownerId).getName()
                    ) : null;

                    if (paymentType == PlotPaymentType.BUY) {
                        return new BuyPlot(
                                plotId,
                                owner,
                                row.getString("group_name"),
                                row.getString("region_id"),
                                row.getDouble("price"),
                                row.getString("world"),
                                row.getEnum("state", PlotState.class),
                                row.get("claimed", StandardReader.LOCAL_DATE_TIME)
                        );
                    } else {
                        return new RentPlot(
                                plotId,
                                owner,
                                row.getString("group_name"),
                                row.getString("region_id"),
                                row.getDouble("price"),
                                row.getString("world"),
                                row.getEnum("state", PlotState.class),
                                row.get("claimed", StandardReader.LOCAL_DATE_TIME),
                                row.get("last_rent_paid", StandardReader.LOCAL_DATE_TIME),
                                row.getLong("rent_interval")
                        );
                    }
                })::all
        );
    }

    @Override
    public CompletableFuture<List<? extends Plot>> readAllFromGroup(String groupName) {
        @Language("mariadb")
        var queryString = """
                SELECT id, owner_id, region_id, group_name, world, state, payment_type, price, rent_interval, last_rent_paid, claimed
                FROM plot
                WHERE group_name = :groupName
                """;
        return CompletableFuture.supplyAsync(queryConfiguration.query(queryString)
                .single(call().bind("groupName", groupName))
                .map(row -> {
                    var paymentType = row.getEnum("payment_type", PlotPaymentType.class);
                    var plotId = row.getString("id");

                    var ownerId = row.get("owner_id", StandardReader.UUID_FROM_STRING);
                    var owner = ownerId != null ? new PlotPlayer(
                            ownerId,
                            Bukkit.getOfflinePlayer(ownerId).getName()
                    ) : null;

                    if (paymentType == PlotPaymentType.BUY) {
                        return new BuyPlot(
                                plotId,
                                owner,
                                row.getString("group_name"),
                                row.getString("region_id"),
                                row.getDouble("price"),
                                row.getString("world"),
                                row.getEnum("state", PlotState.class),
                                row.get("claimed", StandardReader.LOCAL_DATE_TIME)
                        );
                    } else {
                        return new RentPlot(
                                plotId,
                                owner,
                                row.getString("group_name"),
                                row.getString("region_id"),
                                row.getDouble("price"),
                                row.getString("world"),
                                row.getEnum("state", PlotState.class),
                                row.get("claimed", StandardReader.LOCAL_DATE_TIME),
                                row.get("last_rent_paid", StandardReader.LOCAL_DATE_TIME),
                                row.getLong("rent_interval")
                        );
                    }
                })::all
        );
    }

    @Override
    public CompletableFuture<Boolean> delete(String plotId) {
        @Language("mariadb")
        var queryString = """
                DELETE FROM plot WHERE id = :plotId
                """;
        return CompletableFuture.supplyAsync(queryConfiguration.query(queryString)
                .single(call().bind("plotId", plotId))
                .delete()::changed
        );
    }
}