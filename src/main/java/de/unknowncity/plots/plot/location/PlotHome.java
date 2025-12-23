package de.unknowncity.plots.plot.location;

import org.bukkit.Location;

public class PlotHome extends PlotPosition {
    private final String name;
    private final boolean isPublic;

    public PlotHome(String plotId, String name, boolean isPublic, double x, double y, double z, float yaw, float pitch) {
        super(plotId, x, y, z, yaw, pitch);
        this.name = name;
        this.isPublic = isPublic;
    }

    public PlotHome(String plotId, String name, boolean isPublic, Location loc) {
        this(plotId, name, isPublic, loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
    }

    public static PlotHome fromLocation(String plotId, String name, boolean isPublic, Location loc) {
        return new PlotHome(plotId, name, isPublic, loc);
    }

    public String name() {
        return name;
    }

    public boolean isPublic() {
        return isPublic;
    }
}
