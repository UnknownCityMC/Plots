package de.unknowncity.plots.plot;

import de.unknowncity.plots.plot.access.PlotState;
import de.unknowncity.plots.plot.access.entity.PlotPlayer;

import java.time.LocalDateTime;
import java.util.UUID;

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

    public LocalDateTime lastRentPayed() {
        return lastRentPayed;
    }

    public long rentIntervalInMin() {
        return rentIntervalInMin;
    }

    public void lastRentPayed(LocalDateTime lastRentPayed) {
        this.lastRentPayed = lastRentPayed;
    }

    public void rentIntervalInMin(long rentIntervalInMin) {
        this.rentIntervalInMin = rentIntervalInMin;
    }
}