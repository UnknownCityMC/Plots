package de.unknowncity.plots.listener;

import de.unknowncity.plots.PlotsPlugin;
import de.unknowncity.plots.data.model.plot.PlotLocations;
import de.unknowncity.plots.service.RegionService;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.spongepowered.configurate.NodePath;

public class PlotCreateListener implements Listener {

    private final PlotsPlugin plugin;
    private final RegionService regionService;

    public PlotCreateListener(PlotsPlugin plotsPlugin) {
        this.plugin = plotsPlugin;
        regionService = plugin.serviceRegistry().getRegistered(RegionService.class);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {

        if ((event.getAction() != Action.LEFT_CLICK_BLOCK) && (event.getAction() != Action.RIGHT_CLICK_BLOCK)) {
            return;
        }

        var player = event.getPlayer();

        if (plugin.createPlotPlayers.isEmpty() || !plugin.createPlotPlayers.containsKey(player.getUniqueId())) {
            return;
        }

        event.setCancelled(true);

        if (event.getClickedBlock() == null || event.getClickedBlock().isEmpty()) {
            plugin.messenger().sendMessage(player, NodePath.path("command", "plotmod", "create", "no-block"));
        }

        var location = event.getClickedBlock().getLocation();

        if (regionService.getSuitableRegion(location).isPresent()) {
            plugin.messenger().sendMessage(player, NodePath.path("command", "plotmod", "create", "already-exists"));
            return;
        }

        var plotLocations = plugin.createPlotPlayers.get(player.getUniqueId());
        if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
            plotLocations = new PlotLocations(location, plotLocations.loc2());
            plugin.messenger().sendMessage(player, NodePath.path("command", "plotmod", "create", "loc1-success"),
                    Placeholder.parsed("x", String.valueOf(location.x())),
                    Placeholder.parsed("y", String.valueOf(location.y())),
                    Placeholder.parsed("z", String.valueOf(location.z())));
        } else {
            plotLocations = new PlotLocations(plotLocations.loc1(), location);
            plugin.messenger().sendMessage(player, NodePath.path("command", "plotmod", "create", "loc2-success"),
                    Placeholder.parsed("x", String.valueOf(location.x())),
                    Placeholder.parsed("y", String.valueOf(location.y())),
                    Placeholder.parsed("z", String.valueOf(location.z())));
        }

        plugin.createPlotPlayers.put(player.getUniqueId(), plotLocations);
    }
}
