package de.unknowncity.plots.listener;

import de.unknowncity.plots.PlotsPlugin;
import de.unknowncity.plots.plot.model.Plot;
import de.unknowncity.plots.service.PlotService;
import io.papermc.paper.event.entity.EntityMoveEvent;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.vehicle.VehicleMoveEvent;

import java.util.Optional;

public class EntityMoveListener implements Listener {
    private final PlotsPlugin plugin;

    public EntityMoveListener(PlotsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onEntityMove(VehicleMoveEvent event) {
        handle(event.getVehicle(), event.getFrom(), event.getTo());
    }

    @EventHandler
    public void onEntityMove(EntityMoveEvent event) {
        handle(event.getEntity(), event.getFrom(), event.getTo());
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        handlePlayer(event.getPlayer(), event.getFrom(), event.getTo());
    }

    public void handle(Entity entity, Location from, Location to) {
        if (from.getBlockX() == to.getBlockX() && from.getBlockZ() == to.getBlockZ()) return;

        var plotService = plugin.serviceRegistry().getRegistered(PlotService.class);

        Optional<Plot> plotFrom = plotService.findPlotAt(from);
        Optional<Plot> plotTo = plotService.findPlotAt(to);

        if (plotFrom.isEmpty()) {
            return;
        }

        if (plotTo.isEmpty() || !plotFrom.get().id().equals(plotTo.get().id())) {
            entity.teleport(from);
            return;
        }
    }

    public void handlePlayer(Player player, Location from, Location to) {
        var vehicle = player.getVehicle();
        if (vehicle == null) return;

        handle(vehicle, from, to);
    }
}
