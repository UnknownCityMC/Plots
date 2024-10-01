package de.unknowncity.plots.data.model.plot.group;

import de.unknowncity.plots.data.model.plot.Plot;

import java.util.ArrayList;
import java.util.List;

public class PlotGroup {
    private final String name;
    private final String worldName;
    private List<Plot> plotsInGroup;

    public PlotGroup(String name, String worldName) {
        this.name = name;
        this.worldName = worldName;
        this.plotsInGroup = new ArrayList<>();
    }

    public void plotsInGroup(List<Plot> plots) {
        plotsInGroup = plots;
    }

    public List<Plot> plotsInGroup() {
        return plotsInGroup;
    }

    public String name() {
        return name;
    }

    public String worldName() {
        return worldName;
    }

}
