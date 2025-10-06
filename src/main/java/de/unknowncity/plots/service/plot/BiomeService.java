package de.unknowncity.plots.service.plot;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.function.biome.BiomeReplace;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.function.visitor.RegionVisitor;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.world.biome.BiomeType;
import de.unknowncity.astralib.common.service.Service;
import de.unknowncity.plots.PlotsPlugin;
import de.unknowncity.plots.event.PlotInfoUpdateEvent;
import de.unknowncity.plots.plot.model.Plot;

import java.util.logging.Logger;

public class BiomeService extends Service<PlotsPlugin> {
    private final Logger logger;

    public BiomeService(Logger logger) {
        this.logger = logger;
    }

    public void setBiome(Plot plot, BiomeType biome) {
        var world = plot.world();
        final var biomeExtend = 3;

        try (EditSession editSession = WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(world))) {
            var region = new CuboidRegion(plot.protectedRegion().getMinimumPoint(), plot.protectedRegion().getMaximumPoint());
            region.expand(BlockVector3.at(biomeExtend, 0, 0));
            region.expand(BlockVector3.at(-biomeExtend, 0, 0));
            region.expand(BlockVector3.at(0, 0, biomeExtend));
            region.expand(BlockVector3.at(0, 0, -biomeExtend));

            var replace = new BiomeReplace(editSession, biome);
            var visitor = new RegionVisitor(region, replace);
            Operations.complete(visitor);
        } catch (WorldEditException e) {
            logger.warning("Failed to change biome for plot " + plot.id());
        }

        new PlotInfoUpdateEvent(plot).callEvent();
    }
}
