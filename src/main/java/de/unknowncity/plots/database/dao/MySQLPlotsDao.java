package de.unknowncity.plots.database.dao;

import de.unknowncity.plots.plot.Plot;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class MySQLPlotsDao implements PlotsDao {


    @Override
    public CompletableFuture<Optional<Plot>> read(String plotID, String world) {
        /*CompletableFuture.supplyAsync(

                Query.query("""
                        SELECT p.*, f.*, m.*
                        FROM plots AS p JOIN plot_members AS m JOIN plot_flags AS f
                        ON p.id = f.plot_id = m.plot_id
                        WHERE p.id = :plot_id AND p.world = :world
                        """)

        );
         */
        return null;
    }

    @Override
    public CompletableFuture<Boolean> write(Plot plot) {
        return null;
    }

    @Override
    public CompletableFuture<Boolean> update(Plot plot) {
        return null;
    }

    @Override
    public CompletableFuture<Boolean> delete(String plotID, String world) {
        return null;
    }
}
