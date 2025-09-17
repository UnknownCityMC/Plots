package de.unknowncity.plots.plot.model;

import de.unknowncity.plots.plot.access.PlotState;
import de.unknowncity.plots.plot.economy.PlotPaymentType;

import java.time.LocalDateTime;

public class BuyPlot extends Plot {

    public BuyPlot(String plotId, PlotPlayer owner, String groupName, String regionId, double price, String worldName, PlotState state, LocalDateTime claimed
    ) {
        super(plotId, groupName, owner, regionId, price, worldName, state, claimed);
    }

    @Override
    public LocalDateTime lastRentPayed() {
        return null;
    }

    @Override
    public long rentIntervalInMin() {
        return 0;
    }

    @Override
    public PlotPaymentType paymentType() {
        return PlotPaymentType.BUY;
    }
}
