package de.unknowncity.plots.plot.location.signs;

import de.unknowncity.plots.plot.location.PlotPosition;

public class PlotSign extends PlotPosition {

    public PlotSign(double x, double y, double z, float yaw, float pitch) {
        super(x, y, z, yaw, pitch);
    }


    @Override
    public boolean equals(Object obj) {
        return obj instanceof PlotSign plotSign && plotSign.x() == this.x() && plotSign.y() == this.y() && plotSign.z() == this.z();
    }
}
