package de.unknowncity.plots.event;

import de.unknowncity.plots.plot.model.Plot;

public class PlotInfoUpdateEvent extends PlotEvent {

    public PlotInfoUpdateEvent(Plot plot) {
        super(plot);
    }
}
