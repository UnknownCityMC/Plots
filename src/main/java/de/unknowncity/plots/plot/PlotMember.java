package de.unknowncity.plots.plot;

import de.unknowncity.plots.member.PlotMemberRole;

import java.util.UUID;

public record PlotMember(
        UUID memberID,
        PlotMemberRole plotMemberRole
) {
}
