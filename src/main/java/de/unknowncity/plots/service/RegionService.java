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
import de.unknowncity.plots.plot.PlotExpandDirection;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.*;

public class RegionService extends Service<PlotsPlugin> {
    private final WorldGuard worldGuard = WorldGuard.getInstance();

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

    public boolean doesRegionExistBetweenLocations(World world, BlockVector3 loc1, BlockVector3 loc2) {

        ProtectedRegion newRegion = new ProtectedCuboidRegion(UUID.randomUUID().toString(), loc1, loc2);
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager regionManager = container.get(BukkitAdapter.adapt(world));

        if (regionManager == null) {
            return false;
        }

        return !newRegion.getIntersectingRegions(regionManager.getRegions().values()).isEmpty();
    }

    public boolean doesRegionExistBetweenLocations(World world, BlockVector3 loc1, BlockVector3 loc2, ProtectedRegion ignoreRegion) {

        ProtectedRegion newRegion = new ProtectedCuboidRegion(UUID.randomUUID().toString(), loc1, loc2);
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager regionManager = container.get(BukkitAdapter.adapt(world));

        if (regionManager == null) {
            return false;
        }

        var regionList = newRegion.getIntersectingRegions(regionManager.getRegions().values());
        regionList.remove(ignoreRegion);

        return !regionList.isEmpty();
    }

    public ProtectedRegion createRegionFromLocations(World world, BlockVector3 loc1, BlockVector3 loc2, String regionName) {
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager regionManager = container.get(BukkitAdapter.adapt(world));

        ProtectedRegion region = new ProtectedCuboidRegion(regionName, loc1, loc2);

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

        if (doesRegionExistBetweenLocations(world, newMin, newMax, region)) {
            return false;
        }


        ProtectedRegion newRegion = new ProtectedCuboidRegion(region.getId(), newMin, newMax);
        newRegion.setFlags(region.getFlags());
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager regionManager = container.get(BukkitAdapter.adapt(world));
        regionManager.addRegion(newRegion);
        return true;
    }

    public int calculateExpandArea(ProtectedRegion region, PlotExpandDirection direction, int blocks, World world) {
        var newMin = region.getMinimumPoint();
        var newMax = region.getMaximumPoint();

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

        return calculateAreaSquareMeters(newMin, newMax);
    }

    public int calculateAreaSquareMeters(ProtectedRegion region) {
        return calculateAreaSquareMeters(region.getMinimumPoint(), region.getMaximumPoint());
    }

    public int calculateAreaSquareMeters(BlockVector3 min, BlockVector3 max) {
        int lengthX = Math.abs(max.x() - min.x());
        int lengthZ = Math.abs(max.z() - min.z());

        return lengthX * lengthZ;
    }

    public int calculateAreaSquareMeters(Location min, Location max) {
        int lengthX = Math.abs(max.getBlockX() - min.getBlockX());
        int lengthZ = Math.abs(max.getBlockZ() - min.getBlockZ());

        return lengthX * lengthZ;
    }
}