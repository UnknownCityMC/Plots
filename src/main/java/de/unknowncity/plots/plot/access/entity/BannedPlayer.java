package de.unknowncity.plots.plot.access.entity;

import java.util.UUID;

public record BannedPlayer(
    UUID uuid,
    String name
){}
