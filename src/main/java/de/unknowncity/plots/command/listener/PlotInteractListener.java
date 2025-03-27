package de.unknowncity.plots.command.listener;

import de.unknowncity.plots.PlotsPlugin;
import de.unknowncity.plots.data.model.plot.flag.PlotFlagAccessModifier;
import de.unknowncity.plots.service.PlotService;
import de.unknowncity.plots.service.RegionService;
import de.unknowncity.plots.util.PlotId;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.spongepowered.configurate.NodePath;

public class PlotInteractListener implements Listener {

    private final PlotsPlugin plugin;
    private final RegionService regionService;
    private final PlotService plotService;

    public PlotInteractListener(PlotsPlugin plotsPlugin) {
        this.plugin = plotsPlugin;
        regionService = plugin.serviceRegistry().getRegistered(RegionService.class);
        plotService = plugin.serviceRegistry().getRegistered(PlotService.class);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        var player = event.getPlayer();

        if (player.hasPermission("ucplots.interact.bypass")) {
            return;
        }

        var possibleRegion = regionService.getSuitableRegion(player.getLocation());

        if (possibleRegion.isEmpty()) {
            return;
        }

        var plotId = PlotId.generate(player.getWorld(), possibleRegion.get());

        if (!plotService.existsPlot(plotId)) {
            return;
        }

        var plot = plotService.getPlot(plotId);

        if (event.getClickedBlock() == null) {
            return;
        }

        if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
            return;
        }

        plot.interactables().stream().filter(plotInteractable -> plotInteractable.blockType() == event.getClickedBlock().getType()).forEach(plotInteractable -> {
            if (plot.owner().equals(event.getPlayer().getUniqueId())) {
                if (plotInteractable.accessModifier() == PlotFlagAccessModifier.NOBODY) {
                    event.setCancelled(true);
                    plugin.messenger().sendMessage(event.getPlayer(), NodePath.path("event", "plot", "interact", "deny"));
                }
                return;
            }

            var memberOpt = plot.members().stream().filter(plotMember -> plotMember.memberID().equals(event.getPlayer().getUniqueId())).findFirst();
            if (memberOpt.isEmpty()) {
                event.setCancelled(true);
                plugin.messenger().sendMessage(event.getPlayer(), NodePath.path("event", "plot", "interact", "deny"));
                return;
            }

            if (plotInteractable.hasAccess(memberOpt.get().plotMemberRole())) {
                return;
            }

            event.setCancelled(true);
            plugin.messenger().sendMessage(event.getPlayer(), NodePath.path("event", "plot", "interact", "deny"));
        });
    }

    public void cancelEvent(PlayerInteractEvent event) {
        event.setCancelled(true);
    }
}
