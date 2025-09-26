package de.unknowncity.plots.data.dao;

import de.chojo.sadu.queries.api.configuration.ConnectedQueryConfiguration;
import de.chojo.sadu.queries.api.configuration.QueryConfiguration;
import de.chojo.sadu.queries.call.adapter.UUIDAdapter;
import de.unknowncity.plots.plot.model.Plot;
import org.intellij.lang.annotations.Language;

import java.util.List;

import static de.chojo.sadu.queries.api.call.Call.call;

public class PlotDao {
    private final QueryConfiguration queryConfiguration;

    public PlotDao(QueryConfiguration queryConfiguration) {
        this.queryConfiguration = queryConfiguration;
    }

    public Boolean write(ConnectedQueryConfiguration connection, Plot plot) {
        @Language("mariadb")
        var queryString = """
                INSERT INTO plot (
                    id,
                    owner_id,
                    region_id,
                    group_name,
                    world,
                    state,
                    payment_type,
                    price,
                    rent_interval,
                    last_rent_paid
                )
                VALUES (
                    :plotId,
                    :ownerId,
                    :regionId,
                    :groupName,
                    :world,
                    :state,
                    :paymentType,
                    :price,
                    :rentInterval,
                    :lastRentPaid
                )
                ON DUPLICATE KEY UPDATE
                    owner_id      = VALUES(owner_id),
                    region_id     = VALUES(region_id),
                    group_name    = VALUES(group_name),
                    world         = VALUES(world),
                    state         = VALUES(state),
                    payment_type  = VALUES(payment_type),
                    price         = VALUES(price),
                    rent_interval = VALUES(rent_interval),
                    last_rent_paid= VALUES(last_rent_paid);
                """;

        return connection.query(queryString)
                .single(call()
                        .bind("plotId", plot.id())
                        .bind("ownerId", plot.owner() == null ? null : plot.owner().uuid(), UUIDAdapter.AS_STRING)
                        .bind("regionId", plot.regionId())
                        .bind("groupName", plot.groupName())
                        .bind("world", plot.worldName())
                        .bind("state", plot.state())
                        .bind("paymentType", plot.paymentType())
                        .bind("price", plot.price())
                        .bind("rentInterval", plot.rentIntervalInMin())
                        .bind("lastRentPaid", plot.lastRentPayed())
                )
                .insert().changed();
    }

    public List<? extends Plot> readAll() {
        @Language("mariadb")
        var queryString = """
                SELECT id, owner_id, region_id, group_name, world, state, payment_type, price, rent_interval, last_rent_paid, claimed
                FROM plot
                """;

        return queryConfiguration.query(queryString)
                .single()
                .mapAs(Plot.class)
                .all();
    }

    public Boolean delete(String plotId) {
        @Language("mariadb")
        var queryString = """
                DELETE FROM plot WHERE id = :plotId
                """;

        return queryConfiguration.query(queryString)
                .single(call().bind("plotId", plotId))
                .delete().changed();
    }
}