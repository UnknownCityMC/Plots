package de.unknowncity.plots.plot.flag.type.player;

import de.unknowncity.plots.plot.access.PlotAccessUtil;
import de.unknowncity.plots.plot.access.type.PlotAccessModifier;
import de.unknowncity.plots.plot.flag.PlotAccessModifierFlag;
import de.unknowncity.plots.service.PlotService;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.spongepowered.configurate.NodePath;

public class BlockBreakFlag extends PlotAccessModifierFlag implements Listener {

    public BlockBreakFlag(PlotService plotService) {
        super("block-break", PlotAccessModifier.MEMBER, Material.COARSE_DIRT, plotService);
    }

    @EventHandler
    public void onBlockPlace(BlockBreakEvent event) {
        var player = event.getPlayer();

        if (player.hasPermission("ucplots.interact.bypass")) {
            return;
        }


    }
}
