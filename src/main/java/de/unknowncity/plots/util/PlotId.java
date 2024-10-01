package de.unknowncity.plots.util;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import de.unknowncity.plots.data.model.plot.group.PlotGroup;

public class PlotId {

    public static String generate(PlotGroup plotGroup, ProtectedRegion region) {
        return plotGroup.name() + "-" + region.getId();
    }
}
