package de.unknowncity.plots.plot.flag.type.entity;

import de.unknowncity.plots.plot.access.PlotAccessUtil;
import de.unknowncity.plots.plot.access.type.PlotAccessModifier;
import de.unknowncity.plots.plot.flag.PlotAccessModifierFlag;
import de.unknowncity.plots.service.PlotService;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.spongepowered.configurate.NodePath;

public class EntityTameableFlag extends PlotAccessModifierFlag implements Listener {

    public EntityTameableFlag(PlotService plotService) {
        super("entity-killable", PlotAccessModifier.MEMBER, Material.WHEAT_SEEDS, plotService);
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) {
            return;
        }

        plotService.findPlotAt(event.getEntity().getLocation()).ifPresent(plot -> {
            if (PlotAccessUtil.hasAccess(player, plot.getFlag(this), plot)) {
                return;
            }

            event.setCancelled(true);
            plotService.plugin().messenger().sendMessage(player, NodePath.path("event", "plot", "deny", flagId));
        });
    }
}
