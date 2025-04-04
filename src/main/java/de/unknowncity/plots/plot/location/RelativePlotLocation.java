package de.unknowncity.plots.plot.location;

public record RelativePlotLocation(
        PlotLocationType type,
        double x,
        double y,
        double z,
        float yaw,
        float pitch
) {
}
