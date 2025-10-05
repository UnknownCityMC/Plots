package de.unknowncity.plots.plot.location.signs;

import de.unknowncity.plots.plot.model.Plot;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.Rotatable;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Transformation;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.*;
import java.util.stream.Collectors;

public class SignOutline {
    private final Player player;
    private final Plugin plugin;
    private Map<PlotSign, List<Entity>> outlineEntities = new HashMap<>();
    private static final NamespacedKey SIGN_GLOW = new NamespacedKey("ucplots", "");

    public SignOutline(Player player, Plugin plugin) {
        this.player = player;
        this.plugin = plugin;
    }

    public void hideOutline() {
        outlineEntities.forEach((plotSign, entities) -> entities.forEach(Entity::remove));
        outlineEntities.clear();
    }

    public void hideOutline(PlotSign plotSign) {
        for (PlotSign sign : outlineEntities.keySet().stream().filter(plotSign::equals).collect(Collectors.toSet())) {
            outlineEntities.get(sign).forEach(Entity::remove);
            outlineEntities.remove(sign);
        }
    }

    public void showOutline(Plot plot, PlotSign plotSign) {
        outlineEntities.putIfAbsent(plotSign, new ArrayList<>());
        var location = new Location(plot.world(), plotSign.x(), plotSign.y(), plotSign.z());

        // Show outline (spawn new display entity)
        var block = location.getBlock();
        var entityLocation = block.getBoundingBox().getCenter().toLocation(plot.world());
        var signWidth = 0.09f;

        plot.world().spawn(entityLocation, BlockDisplay.class, blockDisplay -> {
            blockDisplay.setTransformation(new Transformation(
                    new Vector3f(-0.5025f, block.getBlockData() instanceof WallSign ? -0.3f : +0.05f, -signWidth / 2),
                    new Quaternionf(0.0, 0.0, 0.0, 1.0),
                    new Vector3f(1.05f, 0.55f, signWidth),
                    new Quaternionf(0.0, 0.0, 0.0, 1.0)
            ));

            applyCommonData(block, blockDisplay, location);
            outlineEntities.get(plotSign).add(blockDisplay);
        });

        // Add an outline to the signpost if the sign has one
        if (!(block.getBlockData() instanceof WallSign)) {
            plot.world().spawn(entityLocation, BlockDisplay.class, blockDisplay -> {

                blockDisplay.setTransformation(new Transformation(
                        new Vector3f(-signWidth / 2, -0.5f, -signWidth / 2),
                        new Quaternionf(0.0, 0.0, 0.0, 1.0),
                        new Vector3f(signWidth, 0.6f, signWidth),
                        new Quaternionf(0.0, 0.0, 0.0, 1.0)
                ));

                applyCommonData(block, blockDisplay, location);
                outlineEntities.get(plotSign).add(blockDisplay);
            });
        }

        outlineEntities.forEach((plotSigns, entities) -> entities.forEach(entity -> {player.showEntity(plugin, entity);}));
    }

    private void applyCommonData(Block block, BlockDisplay blockDisplay, Location location) {
        blockDisplay.setBlock(Material.RED_CONCRETE.createBlockData());
        blockDisplay.setRotation(yawRotation(block), location.getPitch());
        blockDisplay.setVisibleByDefault(false);
        blockDisplay.setGlowing(true);
        blockDisplay.setGlowColorOverride(Color.RED);
        blockDisplay.getPersistentDataContainer().set(SIGN_GLOW, PersistentDataType.BOOLEAN, true);
        blockDisplay.setPersistent(false);
    }

    private float yawRotation(Block block) {
        var data = block.getBlockData();

        if (data instanceof Rotatable rotatable) {
            var facing = rotatable.getRotation();
            return blockFaceToYaw(facing);
        }

        if (data instanceof Directional directional) {
            var facing = directional.getFacing();
            return blockFaceToYaw(facing);
        }

        return 0;
    }

    private float blockFaceToYaw(BlockFace face) {
        return switch (face) {
            case SOUTH_SOUTH_WEST -> 22.5f;
            case SOUTH_WEST -> 45f;
            case WEST_SOUTH_WEST -> 67.5f;
            case WEST -> 90f;
            case WEST_NORTH_WEST -> 112.5f;
            case NORTH_WEST -> 135f;
            case NORTH_NORTH_WEST -> 157.5f;
            case NORTH -> 180f;
            case NORTH_NORTH_EAST -> -157.5f;
            case NORTH_EAST -> -135f;
            case EAST_NORTH_EAST -> -112.5f;
            case EAST -> -90f;
            case EAST_SOUTH_EAST -> -67.5f;
            case SOUTH_EAST -> -45f;
            case SOUTH_SOUTH_EAST -> -22.5f;
            default -> 0f;
        };
    }
}