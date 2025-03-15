package de.unknowncity.plots.data.model.plot;

import java.util.UUID;

public record BannedPlayer(
    UUID uuid,
    String name
){}
