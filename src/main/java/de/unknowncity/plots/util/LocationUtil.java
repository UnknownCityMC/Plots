package de.unknowncity.plots.util;

import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;

import java.util.Optional;

public class LocationUtil {

    public static Optional<Location> findSuitablePlotLocation(World world, ProtectedRegion region) {
        BlockVector3 min = region.getMinimumPoint();
        BlockVector3 max = region.getMaximumPoint();

        double x = (min.x() + max.x()) / 2.0 + 0.5;
        double z = (min.z() + max.z()) / 2.0 + 0.5;

        int y = world.getHighestBlockYAt((int) x, (int) z);
        Location loc = new Location(world, x + 0.5, y + 1, z + 0.5);

        if (isSafeSpot(loc)) {
            return Optional.of(loc);
        }

        return Optional.empty();
    }

    public static boolean isSafeSpot(Location loc) {
        Material below = loc.clone().subtract(0, 1, 0).getBlock().getType();
        Material feet = loc.clone().add(0, 0, 0).getBlock().getType();
        Material head = loc.clone().add(0, 1, 0).getBlock().getType();

        return below.isSolid() && !feet.isSolid() && !head.isSolid();
    }
}
