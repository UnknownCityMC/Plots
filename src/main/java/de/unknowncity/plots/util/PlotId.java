package de.unknowncity.plots.util;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import de.unknowncity.plots.data.model.plot.group.PlotGroup;
import org.bukkit.World;

public class PlotId {

    public static String generate(World world, ProtectedRegion region) {
        return world.getName() + "-" + region.getId();
    }
}
