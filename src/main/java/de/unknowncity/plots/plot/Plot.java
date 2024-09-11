package de.unknowncity.plots.plot;

import de.unknowncity.plots.plot.flag.PlotFlag;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

public record Plot(
        String plotID,
        String worldName,
        UUID ownerID,
        PlotState plotState,
        PlotPaymentType plotSellType,
        double price,
        LocalDateTime lastRentPayed,
        long rentIntervalInMin,
        Set<PlotMember> plotMembers,
        Set<PlotFlag> plotFlags
) {
}
