package de.unknowncity.plots.plot;

import com.fastasyncworldedit.core.registry.state.PropertyKey;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.BuiltInClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardWriter;
import com.sk89q.worldedit.function.mask.BlockMask;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.block.BlockTypes;
import de.unknowncity.astralib.common.structure.KeyValue;
import de.unknowncity.plots.PlotsPlugin;
import de.unknowncity.plots.plot.model.Plot;
import org.bukkit.Tag;
import org.bukkit.block.data.type.Leaves;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

public class SchematicManager {
    private final PlotsPlugin plugin;
    private static final Path SCHEMATIC_PATH_BACKUP = Path.of("schematics", "backups" );
    private static final Path SCHEMATIC_PATH_PRE_SALE = Path.of("schematics", "presale" );

    public SchematicManager(PlotsPlugin plugin) {
        this.plugin = plugin;
    }

    public void makeDirectories() {
        try {
            Files.createDirectories(plugin.getDataPath().resolve(SCHEMATIC_PATH_BACKUP));
            Files.createDirectories(plugin.getDataPath().resolve(SCHEMATIC_PATH_PRE_SALE));
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, e.getMessage());
        }
    }

    public void createPreSaleSchematic(Plot plot) {
        createWorldEditSchematic(plot, getPreSaleSchematicPath(plot));
    }


    public boolean createSchematicBackup(Plot plot, UUID owner) {
        return createWorldEditSchematic(plot, getBackupOwnedSchematicPath(plot, owner));
    }

    public boolean createSchematicBackup(Plot plot) {
        return createWorldEditSchematic(plot, getBackupGenericSchematicPath(plot));
    }

    private boolean createWorldEditSchematic(Plot plot, Path path) {
        CuboidRegion region = new CuboidRegion(plot.protectedRegion().getMinimumPoint(), plot.protectedRegion().getMaximumPoint());
        BlockArrayClipboard clipboard = new BlockArrayClipboard(region);

        ForwardExtentCopy forwardExtentCopy = new ForwardExtentCopy(
                BukkitAdapter.adapt(plot.world()), region, clipboard, region.getMinimumPoint()
        );

        try {
            Operations.complete(forwardExtentCopy);
        } catch (WorldEditException e) {
            plugin.getLogger().log(Level.SEVERE, e.getMessage());
            return false;
        }

        var file = plugin.getDataPath().resolve(path).toFile();

        if (!file.exists()) {
            try {
                file.getParentFile().mkdirs();
                file.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, e.getMessage());
            }
        }

        try (ClipboardWriter writer = BuiltInClipboardFormat.SPONGE_V3_SCHEMATIC.getWriter(new FileOutputStream(file))) {
            writer.write(clipboard);
            return true;
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, e.getMessage());
            return false;
        }
    }

    public void pastePresaleSchematic(Plot plot) {
        var possibleOperation = loadWorldEditSchematic(plot, getPreSaleSchematicPath(plot));
        possibleOperation.ifPresent(operation -> {
            try {
                Operations.complete(operation.value());
                operation.key().close();
            } catch (WorldEditException e) {
                plugin.getLogger().log(Level.SEVERE, e.getMessage());
            }
        });
    }

    public void pasteOwnedBackupSchematic(Plot plot, UUID owner) {
        var possibleOperation = loadWorldEditSchematic(plot, getBackupOwnedSchematicPath(plot, owner));
        possibleOperation.ifPresent(operation -> {
            try {
                Operations.complete(operation.value());
                operation.key().close();
            } catch (WorldEditException e) {
                plugin.getLogger().log(Level.SEVERE, e.getMessage());
            }
        });
    }

    public Optional<KeyValue<EditSession, Operation>> loadWorldEditSchematic(Plot plot, Path path) {
        var file = plugin.getDataPath().resolve(path).toFile();
        var format = BuiltInClipboardFormat.SPONGE_V3_SCHEMATIC;

        if (!file.exists()) {
            plugin.getLogger().log(Level.INFO, "Schematic file " + file + " does not exist! Skipping...");
            return Optional.empty();
        }

        Clipboard clipboard;
        try (ClipboardReader reader = format.getReader(new FileInputStream(file))) {
            clipboard = reader.read();
            var editSession = WorldEdit.getInstance().newEditSession((BukkitAdapter.adapt(plot.world())));
            return Optional.of(KeyValue.of(
                    editSession,
                    new ClipboardHolder(clipboard)
                            .createPaste(editSession)
                            .to(clipboard.getOrigin())
                            .ignoreAirBlocks(false)
                            .build()
            ));
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, e.getMessage());
            return Optional.empty();
        }
    }

    public void replaceLeavesWithOnesThatDecay(Plot plot) {
        var world = plot.world();
        var region = plot.protectedRegion();
        var min = region.getMinimumPoint().toVector3();
        var max = region.getMaximumPoint().toVector3();

        for (int x = min.getBlockX(); x <= max.getBlockX(); x++) {
            for (int y = min.getBlockY(); y <= max.getBlockY(); y++) {
                for (int z = min.getBlockZ(); z <= max.getBlockZ(); z++) {
                    var block = world.getBlockAt(x, y, z);
                    var data = block.getBlockData();
                    if (data instanceof Leaves leaves) {
                        leaves.setPersistent(false);
                        block.setBlockData(leaves, false);
                    }
                }
            }
        }
    }

    public Path getBackupOwnedSchematicPath(Plot plot, UUID owner) {
        return SCHEMATIC_PATH_BACKUP.resolve(plot.id()).resolve(owner + ".schem");
    }

    public Path getBackupGenericSchematicPath(Plot plot) {
        return SCHEMATIC_PATH_BACKUP.resolve(plot.id()).resolve(plot.id() + ".schem");
    }

    public Path getPreSaleSchematicPath(Plot plot) {
        return SCHEMATIC_PATH_PRE_SALE.resolve(plot.id() + ".schem");
    }
}
