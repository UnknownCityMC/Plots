package de.unknowncity.plots.listener;

import com.destroystokyo.paper.MaterialTags;
import de.unknowncity.plots.PlotsPlugin;
import de.unknowncity.plots.plot.location.signs.SignManager;
import de.unknowncity.plots.service.PlotService;
import de.unknowncity.plots.service.RegionService;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.spongepowered.configurate.NodePath;

public class PlotSignLinkListener implements Listener {

    private final PlotsPlugin plugin;
    private final RegionService regionService;
    private final PlotService plotService;
    private final SignManager signManager;

    public PlotSignLinkListener(PlotsPlugin plotsPlugin) {
        this.plugin = plotsPlugin;
        this.regionService = plugin.serviceRegistry().getRegistered(RegionService.class);
        this.plotService = plugin.serviceRegistry().getRegistered(PlotService.class);
        this.signManager = plotService.signManager();
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {

        if ((event.getAction() != Action.LEFT_CLICK_BLOCK) && (event.getAction() != Action.RIGHT_CLICK_BLOCK)) {
            return;
        }

        var player = event.getPlayer();

        var possibleEditSession = signManager.findOpenEditSession(player);

        if (possibleEditSession.isEmpty()) {
            return;
        }

        var editSession = possibleEditSession.get();

        event.setCancelled(true);

        if (event.getClickedBlock() == null || event.getClickedBlock().isEmpty()) {
            plugin.messenger().sendMessage(player, NodePath.path("command", "plotadmin", "sign", "no-block"));
        }

        var block = event.getClickedBlock();
        var location = block.getLocation();
        var world = block.getWorld();

        if (!MaterialTags.SIGNS.isTagged(block)) {
            var region = regionService.getSuitableRegion(location);
            region.ifPresentOrElse(protectedRegion -> {
                if (!plotService.existsPlot(protectedRegion, world)) {
                    plugin.messenger().sendMessage(player, NodePath.path("command", "plotadmin", "sign", "no-region"));
                    return;
                }

                var plot = plotService.getPlot(world, protectedRegion);
                editSession.setPlot(plot);
                plugin.messenger().sendMessage(player, NodePath.path("command", "plotadmin", "sign", "set-region"), plot.tagResolvers(player, plugin.messenger()));
            }, () -> plugin.messenger().sendMessage(player, NodePath.path("command", "plotadmin", "no-suitable-region")));
            return;
        }

        if (!editSession.hasPlot()) {
            plugin.messenger().sendMessage(player, NodePath.path("command", "plotadmin", "sign", "no-region-set"));
            return;
        }

        if (player.isSneaking()) {
            if (!editSession.removeSign(location)) {
                plugin.messenger().sendMessage(player, NodePath.path("command", "plotadmin", "sign", "not-exists"));
                return;
            }
            plugin.messenger().sendMessage(player, NodePath.path("command", "plotadmin", "sign", "remove-success"));
            return;
        }

        if(!editSession.addSign(location)) {
            plugin.messenger().sendMessage(player, NodePath.path("command", "plotadmin", "sign", "already-exists"));
            return;
        }

        plugin.messenger().sendMessage(player, NodePath.path("command", "plotadmin", "sign", "add-success"));
    }
}
