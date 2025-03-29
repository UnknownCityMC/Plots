package de.unknowncity.plots.listener;

import de.unknowncity.plots.PlotsPlugin;
import de.unknowncity.plots.service.PlotService;
import de.unknowncity.plots.service.RegionService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.spongepowered.configurate.NodePath;

public class PlotSignLinkListener implements Listener {

    private final PlotsPlugin plugin;
    private final RegionService regionService;
    private final PlotService plotService;

    public PlotSignLinkListener(PlotsPlugin plotsPlugin) {
        this.plugin = plotsPlugin;
        regionService = plugin.serviceRegistry().getRegistered(RegionService.class);
        plotService = plugin.serviceRegistry().getRegistered(PlotService.class);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {

        if ((event.getAction() != Action.LEFT_CLICK_BLOCK) && (event.getAction() != Action.RIGHT_CLICK_BLOCK)) {
            return;
        }

        var player = event.getPlayer();

        if (plugin.signLinkPlayers.isEmpty() || !plugin.signLinkPlayers.containsKey(player.getUniqueId())) {
            return;
        }

        event.setCancelled(true);

        if (event.getClickedBlock() == null || event.getClickedBlock().isEmpty()) {
            plugin.messenger().sendMessage(player, NodePath.path("command", "plotadmin", "sign", "no-block"));
        }

        var block = event.getClickedBlock();
        var location = block.getLocation();
        var world = block.getWorld();

        if (!block.getType().toString().contains("SIGN")) {
            var region = regionService.getSuitableRegion(location);
            region.ifPresentOrElse(protectedRegion -> {
                if (!plotService.existsPlot(protectedRegion, world)) {
                    plugin.messenger().sendMessage(player, NodePath.path("command", "plotadmin", "sign", "no-region"));
                    return;
                }

                var plot = plotService.getPlot(world, protectedRegion);
                plugin.signLinkPlayers.put(player.getUniqueId(), plot);
                plugin.messenger().sendMessage(player, NodePath.path("command", "plotadmin", "sign", "set-region"), plot.tagResolvers(player, plugin.messenger()));
            }, () -> plugin.messenger().sendMessage(player, NodePath.path("command", "plotadmin", "no-suitable-region")));
            return;
        }

        var plot = plugin.signLinkPlayers.get(player.getUniqueId());
        if (plot == null) {
            plugin.messenger().sendMessage(player, NodePath.path("command", "plotadmin", "sign", "no-region-set"));
            return;
        }

        if(!plotService.addSign(plot, location)) {
            plugin.messenger().sendMessage(player, NodePath.path("command", "plotadmin", "sign", "already-exists"));
            return;
        }

        plugin.messenger().sendMessage(player, NodePath.path("command", "plotadmin", "sign", "success"));
    }
}
