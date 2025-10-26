package de.unknowncity.plots.listener;

import de.unknowncity.plots.PlotsPlugin;
import de.unknowncity.plots.plot.model.Plot;
import de.unknowncity.plots.service.PlotService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleMoveEvent;

import java.util.Optional;

public class EntityMoveListener implements Listener {
    private final PlotsPlugin plugin;

    public EntityMoveListener(PlotsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onEntityMove(VehicleMoveEvent event) {
        var from = event.getFrom();
        var to = event.getTo();

        if (from.getBlockX() == to.getBlockX() && from.getBlockZ() == to.getBlockZ()) return;

        var plotService = plugin.serviceRegistry().getRegistered(PlotService.class);

        Optional<Plot> plotFrom = plotService.findPlotAt(from);
        Optional<Plot> plotTo = plotService.findPlotAt(to);

        if (plotFrom.isEmpty()) {
            return;
        }

        if (plotTo.isEmpty() || !plotFrom.get().id().equals(plotTo.get().id())) {
            event.getVehicle().teleport(from);
        }
    }
}
