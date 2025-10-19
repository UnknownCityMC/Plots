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
import de.unknowncity.plots.data.dao.PlotResetDataDao;
import de.unknowncity.plots.event.PlotInfoUpdateEvent;
import de.unknowncity.plots.plot.model.Plot;
import org.bukkit.Bukkit;

import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public class BiomeService extends Service<PlotsPlugin> {
    private final PlotsPlugin plugin;
    private final PlotResetDataDao resetDataDao;

    public BiomeService(PlotsPlugin plugin, PlotResetDataDao resetDataDao) {
        this.plugin = plugin;
        this.resetDataDao = resetDataDao;
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
            plugin.getLogger().warning("Failed to change biome for plot " + plot.id());
        }

        new PlotInfoUpdateEvent(plot).callEvent();
    }

    public void resetBiome(Plot plot) {
        CompletableFuture.supplyAsync(() -> resetDataDao.getResetBiome(plot.id())).whenComplete((biomeType, throwable) -> {
            Bukkit.getScheduler().runTask(plugin, () -> setBiome(plot, BukkitAdapter.adapt(biomeType)));
        });
    }

    public void saveResetBiome(Plot plot) {
        var biome = plot.world().getBiome(plot.plotHome().getLocation(plot.world()));
        CompletableFuture.runAsync(() -> resetDataDao.setResetBiome(plot.id(), biome))
                .exceptionally(throwable -> {
                    plugin.getLogger().log(Level.WARNING, "Failed to save reset data biome", throwable);
                    return null;
                });
    }
}
