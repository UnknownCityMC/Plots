package de.unknowncity.plots.util;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.World;

public class PlotId {

    public static String generate(World world, ProtectedRegion region) {
        return world.getName() + "-" + region.getId();
    }
}
