package de.unknowncity.plots.util;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;

public class LocationUtil {

    public static Location findSuitablePlotLocation(World world, ProtectedRegion region) {
        var x = (region.getMaximumPoint().x() + region.getMinimumPoint().x()) / 2;
        var z = (region.getMaximumPoint().z() + region.getMinimumPoint().z()) / 2;

        var y = 320;

        while (true) {
            var possibleLocation = new Location(world, x, y, z);
            if (possibleLocation.getBlock().getType() == Material.AIR) {
                y = y / 2;
            } else {
                var possibleLocationOver1 = new Location(world, x, y + 1, z);
                var possibleLocationOver2 = new Location(world, x, y + 2, z);
                if (possibleLocationOver1.getBlock().getType() == Material.AIR &&
                        possibleLocationOver2.getBlock().getType() == Material.AIR) {
                    return possibleLocationOver1;
                } else {
                    y = y + y / 2;
                }
            }
        }
    }
}
