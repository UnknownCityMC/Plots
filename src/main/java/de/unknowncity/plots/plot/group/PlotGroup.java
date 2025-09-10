package de.unknowncity.plots.plot.group;

import de.unknowncity.plots.plot.Plot;

import java.util.HashMap;

public class PlotGroup {
    private static final String BASE_PERMISSION = "plots.limit.";

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

    /**
     * Builds a permission path for a given plot group name
     * Useful for finding the plot limit of a player for a specific plot group
     * @param groupName the name of the plot group
     * @return a permission path
     */
    public static String permission(String groupName) {
        return BASE_PERMISSION + groupName;
    }
}
