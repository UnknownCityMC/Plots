package de.unknowncity.plots.plot.flag.type.player;

import de.unknowncity.plots.plot.access.PlotAccessUtil;
import de.unknowncity.plots.plot.access.type.PlotAccessModifier;
import de.unknowncity.plots.plot.flag.PlotAccessModifierFlag;
import de.unknowncity.plots.service.PlotService;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.spongepowered.configurate.NodePath;

public class ItemPickupFlag extends PlotAccessModifierFlag implements Listener {

    public ItemPickupFlag(PlotService plotService) {
        super("item-pickup", PlotAccessModifier.EVERYBODY, Material.ENDER_EYE, plotService);
    }

    public void onItemDrop(EntityPickupItemEvent event) {
        if (!((event.getEntity() instanceof Player player))) {
            return;
        }

        if (player.hasPermission("ucplots.interact.bypass")) {
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
