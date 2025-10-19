package de.unknowncity.plots.listener;

import de.unknowncity.plots.PlotsPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.spongepowered.configurate.NodePath;

public class LandClaimDeactivateListener implements Listener {
    private final PlotsPlugin plugin;

    public LandClaimDeactivateListener(PlotsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event) {
        disableLandClaim(event);
    }

    @EventHandler
    public void onPlayerQuit(PlayerChangedWorldEvent event) {
        disableLandClaim(event);
    }

    private void disableLandClaim(PlayerChangedWorldEvent event) {
        var player = event.getPlayer();
        var openEditSessionOpt = plugin.landEditSessionHandler().findEditSession(player);
        if (openEditSessionOpt.isPresent()) {
            plugin.landEditSessionHandler().closeEditSession(player);
            plugin.messenger().sendMessage(player, NodePath.path("command", "land", "claim", "disable"));
        }
    }
}
