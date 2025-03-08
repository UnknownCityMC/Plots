package de.unknowncity.plots.data.model.plot;

import java.time.LocalDateTime;

public class RentPlot extends Plot {
    private LocalDateTime lastRentPayed;
    private long rentIntervalInMin;

    public RentPlot(String plotId, String groupName, String regionId, double price, String worldName, LocalDateTime lastRentPayed, long rentIntervalInMin) {
        super(plotId, groupName, regionId, price, worldName);
        this.lastRentPayed = lastRentPayed;
        this.rentIntervalInMin = rentIntervalInMin;
    }


    @Override
    public PlotPaymentType plotPayMentType() {
        return PlotPaymentType.RENT;
    }

    public LocalDateTime lastRentPayed() {
        return lastRentPayed;
    }

    public long rentIntervalInMin() {
        return rentIntervalInMin;
    }
}