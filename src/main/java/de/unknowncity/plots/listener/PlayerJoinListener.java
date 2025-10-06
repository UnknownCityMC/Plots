package de.unknowncity.plots.listener;

import de.unknowncity.plots.PlotsPlugin;
import de.unknowncity.plots.service.PlotService;
import de.unknowncity.plots.service.plot.AccessService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.spongepowered.configurate.NodePath;

public class PlayerJoinListener implements Listener {
    private final PlotsPlugin plugin;

    public PlayerJoinListener(PlotsPlugin plotsPlugin) {
        this.plugin = plotsPlugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        var player = event.getPlayer();
        var location = player.getLocation();

        plugin.serviceRegistry().getRegistered(PlotService.class).findPlotAt(location).ifPresent(plot -> {
            var deniedPlayer = plot.findPlotBannedPlayer(player.getUniqueId());

           if (deniedPlayer.isPresent()) {
               plugin.serviceRegistry().getRegistered(AccessService.class).kickPlayer(plot, player);
               plugin.messenger().sendMessage(player, NodePath.path("event", "plot", "kick", "join"));
           }
        });
    }
}
