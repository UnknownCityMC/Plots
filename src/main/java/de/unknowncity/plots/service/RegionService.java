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

        int minX = (int) Math.min(loc1.getX(), loc2.getX());
        int minY = (int) Math.min(loc1.getY(), loc2.getY());
        int minZ = (int) Math.min(loc1.getZ(), loc2.getZ());
        int maxX = (int) Math.max(loc1.getX(), loc2.getX());
        int maxY = (int) Math.max(loc1.getY(), loc2.getY());
        int maxZ = (int) Math.max(loc1.getZ(), loc2.getZ());

        for (ProtectedRegion region : regionManager.getRegions().values()) {
            if (region.contains(minX, minY, minZ) && region.contains(maxX, maxY, maxZ)) {
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

        int minX = Math.min(loc1.x(), loc2.x());
        int minY = Math.min(loc1.y(), loc2.y());
        int minZ = Math.min(loc1.z(), loc2.z());
        int maxX = Math.max(loc1.x(), loc2.x());
        int maxY = Math.max(loc1.y(), loc2.y());
        int maxZ = Math.max(loc1.z(), loc2.z());

        for (ProtectedRegion region : regionManager.getRegions().values()) {
            if (region.contains(minX, minY, minZ) && region.contains(maxX, maxY, maxZ)) {
                if (region.getId().equals(ignoreId)) {
                    continue;
                }
                return true;
            }
        }

        return false;
    }

    public ProtectedRegion createRegionFromLocations(Location loc1, Location loc2, String regionName) {
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager regionManager = container.get(BukkitAdapter.adapt(loc1.getWorld()));

        int minX = (int) Math.min(loc1.getX(), loc2.getX());
        int minY = (int) Math.min(loc1.getY(), loc2.getY());
        int minZ = (int) Math.min(loc1.getZ(), loc2.getZ());
        int maxX = (int) Math.max(loc1.getX(), loc2.getX());
        int maxY = (int) Math.max(loc1.getY(), loc2.getY());
        int maxZ = (int) Math.max(loc1.getZ(), loc2.getZ());

        BlockVector3 min = new BlockVector3(minX, minY, minZ);
        BlockVector3 max = new BlockVector3(maxX, maxY, maxZ);

        ProtectedRegion region = new ProtectedCuboidRegion(regionName, min, max);

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
}
