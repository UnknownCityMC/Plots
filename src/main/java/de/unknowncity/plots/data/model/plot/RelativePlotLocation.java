package de.unknowncity.plots.data.model.plot;

public record RelativePlotLocation(
        PlotLocationType type,
        double x,
        double y,
        double z,
        double yaw,
        double pitch
) {
}
