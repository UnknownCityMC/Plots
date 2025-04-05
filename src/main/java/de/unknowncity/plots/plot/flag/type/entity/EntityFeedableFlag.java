package de.unknowncity.plots.plot.flag.type.entity;

import de.unknowncity.plots.plot.access.PlotAccessUtil;
import de.unknowncity.plots.plot.access.type.PlotAccessModifier;
import de.unknowncity.plots.plot.flag.PlotAccessModifierFlag;
import de.unknowncity.plots.service.PlotService;
import org.bukkit.Material;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.inventory.CreativeCategory;
import org.spongepowered.configurate.NodePath;

public class EntityFeedableFlag extends PlotAccessModifierFlag {

    public EntityFeedableFlag(PlotService plotService) {
        super("entity-feedable", PlotAccessModifier.MEMBER, Material.IRON_SWORD, plotService);
    }

    public void onEntityDamage(PlayerInteractAtEntityEvent event) {
        var player = event.getPlayer();

        var itemType = player.getActiveItem().getType();

        if (itemType.getCreativeCategory() != CreativeCategory.FOOD) {
            return;
        }

        plotService.findPlotAt(event.getRightClicked().getLocation()).ifPresent(plot -> {
            if (PlotAccessUtil.hasAccess(player, plot.getFlag(this), plot)) {
                return;
            }

            event.setCancelled(true);
            plotService.plugin().messenger().sendMessage(player, NodePath.path("event", "plot", "deny", flagId));
        });
    }
}
