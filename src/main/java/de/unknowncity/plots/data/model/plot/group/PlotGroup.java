package de.unknowncity.plots.data.model.plot.group;

import de.unknowncity.plots.data.model.plot.Plot;

import java.util.HashMap;

public class PlotGroup {
    private final String name;
    private HashMap<String, Plot> plotsInGroup;

    public PlotGroup(String name) {
        this.name = name;
        this.plotsInGroup = new HashMap<>();
    }

    public void plotsInGroup(HashMap<String, Plot> plots) {
        plotsInGroup = plots;
    }

    public HashMap<String, Plot> plotsInGroup() {
        return plotsInGroup;
    }

    public String name() {
        return name;
    }
}
