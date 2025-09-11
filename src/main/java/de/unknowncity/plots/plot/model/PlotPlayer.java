package de.unknowncity.plots.plot.model;

import de.chojo.sadu.mapper.annotation.MappingProvider;
import de.chojo.sadu.mapper.reader.StandardReader;
import de.chojo.sadu.mapper.rowmapper.RowMapping;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Bukkit;

import java.util.UUID;

public class PlotPlayer {
    private final String plotId;
    private final UUID uuid;
    private final String name;

    public PlotPlayer(
            String plotId,
            UUID uuid,
            String name
    ) {

        this.plotId = plotId;
        this.uuid = uuid;
        this.name = name;
    }

    public UUID uuid() {
        return uuid;
    }

    public String name() {
        return name;
    }

    public String plotId() {
        return plotId;
    }

    public TagResolver[] tagResolvers() {
        return new TagResolver[]{
                Placeholder.unparsed("name", name),
                Placeholder.unparsed("uuid", uuid.toString())
        };
    }

    @MappingProvider({"plot_id", "player_id"})
    public static RowMapping<PlotPlayer> map() {
        return row -> {
            var plotId = row.getString("plot_id");
            var playerId = row.get("player_id", StandardReader.UUID_FROM_STRING);
            var name = Bukkit.getOfflinePlayer(playerId).getName();

            return new PlotPlayer(
                    plotId,
                    playerId,
                    name
            );
        };
    }
}
