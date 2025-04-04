package de.unknowncity.plots.plot.access;

import java.util.UUID;

public record BannedPlayer(
    UUID uuid,
    String name
){}
