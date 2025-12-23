package de.unknowncity.plots.plot.location.signs;

import de.chojo.sadu.mapper.annotation.MappingProvider;
import de.chojo.sadu.mapper.rowmapper.RowMapping;

public record PlotSign(String plotId, long x, long y, long z) {

    @Override
    public boolean equals(Object obj) {
        return obj instanceof PlotSign plotSign && plotSign.x() == this.x() && plotSign.y() == this.y() && plotSign.z() == this.z();
    }

    @MappingProvider({"plot_id", "x", "y", "z"})
    public static RowMapping<PlotSign> map() {
        return row -> {
            var plotId = row.getString("plot_id");
            var x = row.getLong("x");
            var y = row.getLong("y");
            var z = row.getLong("z");

            return new PlotSign(plotId, x, y, z);
        };
    }

    public boolean isAt(org.bukkit.Location location) {
        return location.getBlockX() == x() && location.getBlockY() == y() && location.getBlockZ() == z();
    }
}
