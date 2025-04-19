package de.unknowncity.plots.plot.location;

public class PlotPosition {
    private final double x;
    private final double y;
    private final double z;
    private final float yaw;
    private final float pitch;

    public PlotPosition(double x, double y, double z, float yaw, float pitch) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public double x() {
        return x;
    }

    public double y() {
        return y;
    }

    public double z() {
        return z;
    }

    public float yaw() {
        return yaw;
    }

    public float pitch() {
        return pitch;
    }
}