package de.unknowncity.plots.data.model.plot;

import java.util.UUID;

public record PlotMember(
        UUID memberID,
        String lastKnownName,
        PlotMemberRole plotMemberRole
) {
}
