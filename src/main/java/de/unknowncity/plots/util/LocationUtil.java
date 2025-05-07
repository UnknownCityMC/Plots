package de.unknowncity.plots.util;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;

public class LocationUtil {

    public static Location findSaveTeleportLocation(World world, ProtectedRegion region) {
        var x = (region.getMaximumPoint().x() + region.getMinimumPoint().x()) / 2;
        var z = (region.getMaximumPoint().z() + region.getMinimumPoint().z()) / 2;

        var y = 320;

        while (true) {
            var searchLocation = new Location(world, x, y, z);

            if (searchLocation.getBlock().getType() == Material.AIR) {
                var possibleLocation = searchLocation.subtract(0, 1, 0);
                if (possibleLocation.getBlock().getType().isSolid()) {
                    return possibleLocation;
                }
                y = y / 2;
            } else {
                y = y + y / 2;
            }
        }
    }
}
