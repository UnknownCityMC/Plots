package de.unknowncity.plots.plot.flag;

import de.unknowncity.plots.plot.flag.type.block.IceFormFlag;
import de.unknowncity.plots.plot.flag.type.block.IceMeltFlag;
import de.unknowncity.plots.plot.flag.type.block.LeafDecayFlag;
import de.unknowncity.plots.plot.flag.type.player.BlockBreakFlag;
import de.unknowncity.plots.plot.flag.type.player.BlockPlaceFlag;
import de.unknowncity.plots.plot.flag.type.player.ItemDropFlag;
import de.unknowncity.plots.service.PlotService;

public class PlotFlags {

    public static void registerAllFlags(PlotService plotService) {
        var flagRegistry = plotService.flagRegistry();

        // Block flags
        flagRegistry.registerToCat(PlotFlag.Category.BLOCK, new IceMeltFlag());
        flagRegistry.registerToCat(PlotFlag.Category.BLOCK, new IceFormFlag());
        flagRegistry.registerToCat(PlotFlag.Category.BLOCK, new LeafDecayFlag());

        // Player flags (mostly PlotAccessModifier flags)
        flagRegistry.registerToCat(PlotFlag.Category.PLAYER, new BlockPlaceFlag(plotService));
        flagRegistry.registerToCat(PlotFlag.Category.PLAYER, new BlockBreakFlag(plotService));
        flagRegistry.registerToCat(PlotFlag.Category.PLAYER, new ItemDropFlag(plotService));
    }
}
