package de.unknowncity.plots.plot.location;

import org.bukkit.Location;
import org.bukkit.World;

public class PlotPosition implements Cloneable {
    private final String plotId;
    private final double x;
    private final double y;
    private final double z;
    private final float yaw;
    private final float pitch;

    public PlotPosition(String plotId, double x, double y, double z, float yaw, float pitch) {
        this.plotId = plotId;
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

    public String plotId() {
        return plotId;
    }

    public static PlotPosition fromLocation(String plotId, Location loc) {
        return new PlotPosition(plotId, loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
    }

    public Location getLocation(World world) {
        return new Location(world, x, y, z, yaw, pitch);
    }
}