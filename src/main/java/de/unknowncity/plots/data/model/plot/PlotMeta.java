package de.unknowncity.plots.data.model.plot;

import java.time.LocalDateTime;

public record PlotMeta(
        PlotState plotState,
        PlotPaymentType plotPaymentType,
        double price,
        LocalDateTime lastRentPayed,
        long rentIntervalInMin
) {

}
