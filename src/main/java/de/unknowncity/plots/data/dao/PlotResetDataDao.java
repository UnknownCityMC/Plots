package de.unknowncity.plots.data.dao;

import de.chojo.sadu.queries.api.call.Call;
import de.chojo.sadu.queries.api.configuration.QueryConfiguration;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.block.Biome;
import org.intellij.lang.annotations.Language;

public class PlotResetDataDao {
    private final QueryConfiguration queryConfiguration;
    private final Registry<@org.jetbrains.annotations.NotNull Biome> biomeRegistry = RegistryAccess
            .registryAccess()
            .getRegistry(RegistryKey.BIOME);

    public PlotResetDataDao(QueryConfiguration queryConfiguration) {
        this.queryConfiguration = queryConfiguration;
    }

    public Biome getResetBiome(String plotId) {
        @Language("mariadb")
        var query = """
                SELECT biome FROM plot_reset_data WHERE plot_id = :plotId;
                """;

        return queryConfiguration.query(query)
                .single(Call.call().bind("plotId", plotId))
                .map(row -> biomeRegistry.get(NamespacedKey.fromString(row.getString("biome"))))
                .first().orElse(Biome.PLAINS);
    }

    public void setResetBiome(String plotId, Biome biome) {
        @Language("mariadb")
        var query = """
                INSERT INTO plot_reset_data (plot_id, biome) VALUES (:plotId, :biome) ON DUPLICATE KEY UPDATE biome = VALUES(biome);
                """;

        queryConfiguration.query(query)
                .single(Call.call()
                        .bind("plotId", plotId)
                        .bind("biome", biome.getKey().toString()))
                .insert().changed();
    }
}

