package de.unknowncity.plots.plot.model;

import de.unknowncity.plots.plot.access.PlotState;
import de.unknowncity.plots.plot.economy.PlotPaymentType;

import java.time.LocalDateTime;

public class RentPlot extends Plot {
    private LocalDateTime lastRentPayed;
    private long rentIntervalInMin;

    public RentPlot(String plotId, PlotPlayer owner, String groupName, String regionId, double price, String worldName,
                    PlotState state, LocalDateTime claimed, LocalDateTime lastRentPayed, long rentIntervalInMin
    ) {
        super(plotId, groupName, owner, regionId, price, worldName, state, claimed);
        this.lastRentPayed = lastRentPayed;
        this.rentIntervalInMin = rentIntervalInMin;
    }

    public void lastRentPayed(LocalDateTime lastRentPayed) {
        this.lastRentPayed = lastRentPayed;
    }

    public void rentIntervalInMin(long rentIntervalInMin) {
        this.rentIntervalInMin = rentIntervalInMin;
    }

    @Override
    public LocalDateTime lastRentPayed() {
        return lastRentPayed;
    }

    @Override
    public long rentIntervalInMin() {
        return rentIntervalInMin;
    }

    @Override
    public PlotPaymentType paymentType() {
        return PlotPaymentType.RENT;
    }
}