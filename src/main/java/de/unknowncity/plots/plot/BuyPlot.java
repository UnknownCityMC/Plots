package de.unknowncity.plots.plot;

import de.unknowncity.plots.plot.access.PlotState;
import de.unknowncity.plots.plot.access.entity.PlotPlayer;

import java.time.LocalDateTime;
import java.util.UUID;

public class BuyPlot extends Plot {

    public BuyPlot(String plotId, PlotPlayer owner, String groupName, String regionId, double price, String worldName, PlotState state, LocalDateTime claimed
    ) {
        super(plotId, groupName, owner, regionId, price, worldName, state, claimed);
    }
}
