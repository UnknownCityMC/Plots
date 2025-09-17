package de.unknowncity.plots.listener;

import de.unknowncity.plots.PlotsPlugin;
import de.unknowncity.plots.service.RegionService;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.spongepowered.configurate.NodePath;

public class LandEditListener implements Listener {

    private final PlotsPlugin plugin;
    private final RegionService regionService;

    public LandEditListener(PlotsPlugin plotsPlugin) {
        this.plugin = plotsPlugin;
        regionService = plugin.serviceRegistry().getRegistered(RegionService.class);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {

        if ((event.getAction() != Action.LEFT_CLICK_BLOCK) && (event.getAction() != Action.RIGHT_CLICK_BLOCK)) {
            return;
        }

        var player = event.getPlayer();

        var editSessionOpt = plugin.landEditSessionHandler().findEditSession(player);

        editSessionOpt.ifPresent(landEditSession -> {
            event.setCancelled(true);

            if (event.getClickedBlock() == null) {
                return;
            }

            var location = event.getClickedBlock().getLocation();

            if (regionService.getSuitableRegion(location).isPresent()) {
                plugin.messenger().sendMessage(player, NodePath.path("command", "land", "claim", "already-exists"));
                return;
            }

            if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
                landEditSession.setPos1(location);
            } else {
                landEditSession.setPos2(location);
            }
        });
    }
}
