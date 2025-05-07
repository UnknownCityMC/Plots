package de.unknowncity.plots.plot.location;

import org.bukkit.Location;

public class PlotLocation extends PlotPosition {
    private final String name;
    private final boolean isPublic;

    public PlotLocation(String name, boolean isPublic, double x, double y, double z, float yaw, float pitch) {
        super(x, y, z, yaw, pitch);
        this.name = name;
        this.isPublic = isPublic;
    }

    public PlotLocation(String name, boolean isPublic, Location location) {
        super(location.x(), location.y(), location.z(), location.getYaw(), location.getPitch());
        this.name = name;
        this.isPublic = isPublic;
    }

    public String name() {
        return name;
    }

    public boolean isPublic() {
        return isPublic;
    }
}
