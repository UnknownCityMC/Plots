package de.unknowncity.plots.plot.location;

public record RelativePlotLocation(
        String name,
        double x,
        double y,
        double z,
        float yaw,
        float pitch
) {
}
