package de.unknowncity.plots.plot.flag.type.player;

import de.unknowncity.plots.Permissions;
import de.unknowncity.plots.plot.access.PlotAccessUtil;
import de.unknowncity.plots.plot.access.type.PlotAccessModifier;
import de.unknowncity.plots.plot.flag.PlotAccessModifierFlag;
import de.unknowncity.plots.service.PlotService;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDropItemEvent;
import org.spongepowered.configurate.NodePath;

public class ItemDropFlag extends PlotAccessModifierFlag implements Listener {

    public ItemDropFlag(PlotService plotService) {
        super("item-drop", PlotAccessModifier.EVERYBODY, Material.ENDER_EYE, plotService);
    }

    @EventHandler
    public void onItemDrop(EntityDropItemEvent event) {
        if (!((event.getEntity() instanceof Player player))) {
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
