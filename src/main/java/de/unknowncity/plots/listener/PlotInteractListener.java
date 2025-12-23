package de.unknowncity.plots.listener;

import de.unknowncity.plots.Permissions;
import de.unknowncity.plots.PlotsPlugin;
import de.unknowncity.plots.plot.access.PlotAccessUtil;
import de.unknowncity.plots.service.PlotService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.spongepowered.configurate.NodePath;

public class PlotInteractListener implements Listener {

    private final PlotsPlugin plugin;
    private final PlotService plotService;

    public PlotInteractListener(PlotsPlugin plotsPlugin) {
        this.plugin = plotsPlugin;
        plotService = plugin.serviceRegistry().getRegistered(PlotService.class);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        var player = event.getPlayer();

        if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
            return;
        }

        if (event.getClickedBlock() == null) {
            return;
        }

        if (player.hasPermission(Permissions.BYPASS_INTERACT)) {
            return;
        }

        plotService.findPlotAt(event.getClickedBlock().getLocation()).ifPresent(plot -> {
            var interactables = plot.interactables().stream().filter(plotInteractable -> plotInteractable.blockType() == event.getClickedBlock().getType()).toList();
            if (!interactables.isEmpty() && PlotAccessUtil.hasAccess(player, interactables.getFirst().accessModifier(), plot)) {
                return;
            }

            if (interactables.isEmpty() && (plot.isMember(player.getUniqueId()) || plot.isOwner(player.getUniqueId()))) {
                return;
            }

            event.setCancelled(true);
            plugin.messenger().sendMessage(event.getPlayer(), NodePath.path("event", "plot", "deny", "interact"));
        });
    }
}