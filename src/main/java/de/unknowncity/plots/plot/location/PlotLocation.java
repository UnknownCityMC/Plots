package de.unknowncity.plots.plot.location;

import org.bukkit.Location;

public class PlotLocation extends PlotPosition {
    private final String name;
    private final boolean isPublic;

    public PlotLocation(String plotId, String name, boolean isPublic, double x, double y, double z, float yaw, float pitch) {
        super(plotId, x, y, z, yaw, pitch);
        this.name = name;
        this.isPublic = isPublic;
    }

    public PlotLocation(String plotId, String name, boolean isPublic, Location loc) {
        this(plotId, name, isPublic, loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
    }

    public static PlotLocation fromLocation(String plotId, String name, boolean isPublic, Location loc) {
        return new PlotLocation(plotId, name, isPublic, loc);
    }

    public String name() {
        return name;
    }

    public boolean isPublic() {
        return isPublic;
    }
}
