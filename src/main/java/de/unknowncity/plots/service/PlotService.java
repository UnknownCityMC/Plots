package de.unknowncity.plots.service;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import de.unknowncity.astralib.common.service.Service;
import de.unknowncity.plots.PlotsPlugin;
import de.unknowncity.plots.data.model.plot.Plot;
import de.unknowncity.plots.data.model.plot.PlotMember;
import de.unknowncity.plots.data.model.plot.PlotPaymentType;
import de.unknowncity.plots.data.model.plot.PlotState;
import de.unknowncity.plots.data.model.plot.flag.PlotFlag;
import de.unknowncity.plots.data.model.plot.group.PlotGroup;
import de.unknowncity.plots.data.repository.PlotGroupRepository;
import org.bukkit.World;

import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PlotService implements Service<PlotsPlugin> {
    private final Set<PlotGroup> plotGroupCache = new HashSet<>();
    private final PlotGroupRepository plotGroupRepositoryt;

    public PlotService(PlotGroupRepository plotGroupRepository) {
        this.plotGroupRepositoryt = plotGroupRepository;
    }

    @Override
    public void startup(PlotsPlugin plugin) {

    }

    @Override
    public void shutdown() {

    }

    public boolean existsPlot(ProtectedRegion region, World world) {
        return true;
    }

    public List<PlotGroup> plotGroupsInWorld(World world) {
        return plotGroupCache.stream().filter(plotGroup -> plotGroup.worldName().equals(world.getName())).toList();
    }

    public boolean createSellPlotFromExisting(ProtectedRegion region, World world, double price) {
        return true;
    }

    public boolean createRentPlotFromExisting(ProtectedRegion region, World world, double price, Duration rentInterval) {

        return true;
    }
}
