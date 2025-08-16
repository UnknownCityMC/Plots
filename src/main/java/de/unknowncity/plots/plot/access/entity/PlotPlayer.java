package de.unknowncity.plots.plot.access.entity;

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
}
