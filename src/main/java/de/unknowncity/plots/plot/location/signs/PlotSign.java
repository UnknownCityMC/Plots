package de.unknowncity.plots.plot.location.signs;

public record PlotSign(int x, int y, int z) {

    @Override
    public boolean equals(Object obj) {
        return obj instanceof PlotSign plotSign && plotSign.x() == this.x() && plotSign.y() == this.y() && plotSign.z() == this.z();
    }
}
