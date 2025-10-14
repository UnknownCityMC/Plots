package de.unknowncity.plots.plot.flag.type.vehicle;

import de.unknowncity.plots.Permissions;
import de.unknowncity.plots.plot.access.PlotAccessUtil;
import de.unknowncity.plots.plot.access.type.PlotAccessModifier;
import de.unknowncity.plots.plot.flag.PlotAccessModifierFlag;
import de.unknowncity.plots.service.PlotService;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.spongepowered.configurate.NodePath;

public class RideFlag extends PlotAccessModifierFlag implements Listener {

    public RideFlag(PlotService plotService) {
        super("ride", PlotAccessModifier.EVERYBODY, Material.SADDLE, plotService);
    }

    @EventHandler
    public void onPlayerInteractAtEntity(VehicleEnterEvent event) {
        if (!(event.getEntered() instanceof Player player)) {
            return;
        }

        if (player.hasPermission(Permissions.BYPASS_INTERACT)) {
            return;
        }

        plotService.findPlotAt(player.getLocation()).ifPresent(plot -> {
            if (PlotAccessUtil.hasAccess(player, plot.getFlag(this), plot)) {
                return;
            }

            event.setCancelled(true);
            plotService.plugin().messenger().sendMessage(player, NodePath.path("event", "plot", "deny", flagId));
        });
    }
}
