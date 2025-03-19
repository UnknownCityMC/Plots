package de.unknowncity.plots.service;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.managers.storage.StorageException;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import de.unknowncity.astralib.common.service.Service;
import de.unknowncity.plots.PlotsPlugin;
import de.unknowncity.plots.data.model.plot.PlotExpandDirection;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class RegionService implements Service<PlotsPlugin> {
    private final WorldGuard worldGuard = WorldGuard.getInstance();

    @Override
    public void shutdown() {

    }

    /**
     * Returns a region suitable for mariadb creation at the location
     *
     * @param location the player
     * @return a ProtectedRegion suitable for mariadb creation
     */
    public Optional<ProtectedRegion> getSuitableRegion(Location location) {
        var container = worldGuard.getPlatform().getRegionContainer();
        var regions = container.get(BukkitAdapter.adapt(location.getWorld()));

        if (regions == null) {
            return Optional.empty();
        }

        var possibleRegions = regions.getApplicableRegions(BukkitAdapter.asBlockVector(location));
        return possibleRegions.getRegions().stream().max(Comparator.comparingInt(ProtectedRegion::getPriority));
    }

    public HashMap<String, ProtectedRegion> getAllRegions() {
        var regionManagers = WorldGuard.getInstance().getPlatform().getRegionContainer();
        HashMap<String, ProtectedRegion> getAllRegions = new HashMap<>();

        for (RegionManager regionManager : regionManagers.getLoaded()) {
            getAllRegions.putAll(regionManager.getRegions());
        }

        return getAllRegions;
    }

    public Map<String, ProtectedRegion> getRegions(World world) {
        var regions = WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(world));

        if (regions == null) {
            return new HashMap<>();
        }

        return regions.getRegions();
    }

    public ProtectedRegion getRegion(String id, World world) {
        var regions = WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(world));

        if (regions == null) {
            return null;
        }

        return regions.getRegions().get(id);
    }

    public boolean doesRegionExistBetweenLocations(Location loc1, Location loc2) {
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager regionManager = container.get(BukkitAdapter.adapt(loc1.getWorld()));

        if (regionManager == null) {
            return false;
        }

        BlockVector3 bv1 = new BlockVector3(loc1.getBlockX(), loc1.getBlockY(), loc1.getBlockZ());
        BlockVector3 bv2 = new BlockVector3(loc2.getBlockX(), loc2.getBlockY(), loc2.getBlockZ());

        for (ProtectedRegion region : regionManager.getRegions().values()) {
            if (isRegionBetweenLocations(region, bv1, bv2)) {
                return true;
            }
        }

        return false;
    }

    public boolean doesRegionExistBetweenLocations(World world, BlockVector3 loc1, BlockVector3 loc2, String ignoreId) {
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager regionManager = container.get(BukkitAdapter.adapt(world));

        if (regionManager == null) {
            return false;
        }

        for (ProtectedRegion region : regionManager.getRegions().values()) {
            if (!region.getId().equals(ignoreId) && isRegionBetweenLocations(region, loc1, loc2)) {
                return true;
            }
        }

        return false;
    }

    private boolean isRegionBetweenLocations(ProtectedRegion region, BlockVector3 loc1, BlockVector3 loc2) {
        double minX = region.getMinimumPoint().x();
        double minZ = region.getMinimumPoint().z();

        double maxX = region.getMaximumPoint().x();
        double maxZ = region.getMaximumPoint().z();

        return (minX >= Math.min(loc1.x(), loc2.x()) || maxX <= Math.max(loc1.x(), loc2.x())) &&
                (minZ >= Math.min(loc1.z(), loc2.z()) || maxZ <= Math.max(loc1.z(), loc2.z()));
    }

    public ProtectedRegion createRegionFromLocations(Location loc1, Location loc2, String regionName) {
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager regionManager = container.get(BukkitAdapter.adapt(loc1.getWorld()));

        BlockVector3 bv1 = new BlockVector3(loc1.getBlockX(), loc1.getBlockY(), loc1.getBlockZ());
        BlockVector3 bv2 = new BlockVector3(loc2.getBlockX(), loc2.getBlockY(), loc2.getBlockZ());

        ProtectedRegion region = new ProtectedCuboidRegion(regionName, bv1, bv2);

        regionManager.addRegion(region);
        try {
            regionManager.save();
        } catch (StorageException e) {
            throw new RuntimeException(e);
        }

        return region;
    }

    public boolean expandRegionInDirection(ProtectedRegion region, PlotExpandDirection direction, int blocks, World world) {
        BlockVector3 newMin = region.getMinimumPoint();
        BlockVector3 newMax = region.getMaximumPoint();

        switch (direction) {
            case NORTH:
                newMin = newMin.withZ(newMin.z() - blocks);
                break;
            case SOUTH:
                newMax = newMax.withZ(newMax.z() + blocks);
                break;
            case WEST:
                newMin = newMin.withX(newMin.x() - blocks);
                break;
            case EAST:
                newMax = newMax.withX(newMax.x() + blocks);
                break;
        }

        if (doesRegionExistBetweenLocations(world, newMin, newMax, region.getId())) {
            return false;
        }

        ProtectedRegion newRegion = new ProtectedCuboidRegion(region.getId(), newMin, newMax);
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager regionManager = container.get(BukkitAdapter.adapt(world));
        regionManager.addRegion(newRegion);

        return true;
    }

    public int expandRegionInDirectionBlockCount(ProtectedRegion region, PlotExpandDirection direction, int blocks, World world) {
        BlockVector3 newMin = region.getMinimumPoint();
        BlockVector3 newMax = region.getMaximumPoint();

        switch (direction) {
            case NORTH:
                newMin = newMin.withZ(newMin.z() - blocks);
                break;
            case SOUTH:
                newMax = newMax.withZ(newMax.z() + blocks);
                break;
            case WEST:
                newMin = newMin.withX(newMin.x() - blocks);
                break;
            case EAST:
                newMax = newMax.withX(newMax.x() + blocks);
                break;
        }

        ProtectedRegion newRegion = new ProtectedCuboidRegion(region.getId(), newMin, newMax);

        return calculateAreaSquareMeters(newRegion);
    }

    public int calculateAreaSquareMeters(ProtectedRegion region) {
        return calculateAreaSquareMeters(region.getMinimumPoint(), region.getMaximumPoint());
    }

    private int calculateAreaSquareMeters(BlockVector3 min, BlockVector3 max) {
        int lengthX = Math.abs(max.x()) - Math.abs(min.x()) + 1;
        int lengthZ = Math.abs(max.z()) - Math.abs(min.z()) + 1;

        return lengthX * lengthZ;
    }
}
