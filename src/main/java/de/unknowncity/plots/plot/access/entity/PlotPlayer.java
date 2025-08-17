package de.unknowncity.plots.plot.access.entity;

import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

import java.util.UUID;

public class PlotPlayer {
    private final UUID uuid;
    private final String name;

    public PlotPlayer(
            UUID uuid,
            String name
    ) {
        this.uuid = uuid;
        this.name = name;
    }

    public UUID uuid() {
        return uuid;
    }

    public String name() {
        return name;
    }

    public TagResolver[] tagResolvers() {
        return new TagResolver[]{
                Placeholder.unparsed("name", name),
                Placeholder.unparsed("uuid", uuid.toString())
        };
    }
}
