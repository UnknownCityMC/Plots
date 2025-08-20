package de.unknowncity.plots.plot.flag.type.entity;

import de.unknowncity.plots.plot.access.PlotAccessUtil;
import de.unknowncity.plots.plot.access.type.PlotAccessModifier;
import de.unknowncity.plots.plot.flag.PlotAccessModifierFlag;
import de.unknowncity.plots.service.PlotService;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.inventory.CreativeCategory;
import org.spongepowered.configurate.NodePath;

import java.util.HashMap;

public class EntityFeedableFlag extends PlotAccessModifierFlag implements Listener {
    private final HashMap<EntityType, Material> feedableEntities = new HashMap<>();

    public EntityFeedableFlag(PlotService plotService) {
        super("entity-feedable", PlotAccessModifier.MEMBER, Material.WHEAT, plotService);

        feedableEntities.put(EntityType.PANDA, Material.BAMBOO);
        feedableEntities.put(EntityType.PARROT, Material.BEETROOT_SEEDS);
        feedableEntities.put(EntityType.PIG, Material.CARROT);

        feedableEntities.put(EntityType.SHEEP, Material.WHEAT);

    }

    @EventHandler
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
