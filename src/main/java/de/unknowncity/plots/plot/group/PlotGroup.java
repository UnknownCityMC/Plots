package de.unknowncity.plots.plot.group;

public record PlotGroup(String name) {
    private static final String BASE_PERMISSION = "plots.limit.";

    /**
     * Builds a permission path for a given plot group name
     * Useful for finding the plot limit of a player for a specific plot group
     *
     * @param groupName the name of the plot group
     * @return a permission path
     */
    public static String permission(String groupName) {
        return BASE_PERMISSION + groupName;
    }
}
