package de.unknowncity.plots.service;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import de.unknowncity.astralib.common.service.Service;
import de.unknowncity.plots.PlotsPlugin;
import org.bukkit.Location;

import java.util.Comparator;
import java.util.Optional;

public class RegionService implements Service<PlotsPlugin> {
    private WorldGuard worldGuard;

    @Override
    public void startup(PlotsPlugin plugin) {
        this.worldGuard = WorldGuard.getInstance();
    }


    @Override
    public void shutdown() {

    }


    /**
     * Returns a region suitable for plot creation at the location
     * @param location the player
     * @return a ProtectedRegion suitable for plot creation
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
}
