package de.unknowncity.plots.plot.flag;

import de.unknowncity.plots.plot.flag.type.block.*;
import de.unknowncity.plots.plot.flag.type.entity.EntityFeedableFlag;
import de.unknowncity.plots.plot.flag.type.entity.EntityKillableFlag;
import de.unknowncity.plots.plot.flag.type.entity.EntityTameableFlag;
import de.unknowncity.plots.plot.flag.type.player.BlockBreakFlag;
import de.unknowncity.plots.plot.flag.type.player.BlockPlaceFlag;
import de.unknowncity.plots.plot.flag.type.player.ItemDropFlag;
import de.unknowncity.plots.plot.flag.type.player.ItemPickupFlag;
import de.unknowncity.plots.plot.flag.type.vehicle.RideFlag;
import de.unknowncity.plots.service.PlotService;

public class PlotFlags {

    public static void registerAllFlags(PlotService plotService) {
        var flagRegistry = plotService.flagRegistry();

        // Block flags
        flagRegistry.registerToCat(PlotFlag.Category.BLOCK, new IceMeltFlag());
        flagRegistry.registerToCat(PlotFlag.Category.BLOCK, new IceFormFlag());
        flagRegistry.registerToCat(PlotFlag.Category.BLOCK, new LeafDecayFlag());
        flagRegistry.registerToCat(PlotFlag.Category.BLOCK, new SnowFallFlag());
        flagRegistry.registerToCat(PlotFlag.Category.BLOCK, new SnowMeltFlag());

        // Vehicle Flags
        flagRegistry.registerToCat(PlotFlag.Category.VEHICLE, new RideFlag(plotService));

        // Entity Flags
        flagRegistry.registerToCat(PlotFlag.Category.ENTITY, new EntityFeedableFlag(plotService));
        flagRegistry.registerToCat(PlotFlag.Category.ENTITY, new EntityTameableFlag(plotService));
        flagRegistry.registerToCat(PlotFlag.Category.ENTITY, new EntityKillableFlag(plotService));

        // Player flags (mostly PlotAccessModifier flags)
        flagRegistry.registerToCat(PlotFlag.Category.PLAYER, new BlockPlaceFlag(plotService));
        flagRegistry.registerToCat(PlotFlag.Category.PLAYER, new BlockBreakFlag(plotService));
        flagRegistry.registerToCat(PlotFlag.Category.PLAYER, new ItemDropFlag(plotService));
        flagRegistry.registerToCat(PlotFlag.Category.PLAYER, new ItemPickupFlag(plotService));
    }
}
