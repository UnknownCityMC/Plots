package de.unknowncity.plots.plot;

import de.unknowncity.plots.plot.access.PlotState;

import java.util.UUID;

public class BuyPlot extends Plot {

    public BuyPlot(String plotId, UUID owner, String groupName, String regionId, double price, String worldName,
                   PlotState state
    ) {
        super(plotId, groupName, owner, regionId, price, worldName, state);
    }
}
