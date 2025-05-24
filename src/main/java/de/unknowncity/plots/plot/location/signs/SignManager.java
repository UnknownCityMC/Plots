package de.unknowncity.plots.plot.location.signs;

import com.destroystokyo.paper.MaterialTags;
import de.unknowncity.astralib.common.message.lang.Language;
import de.unknowncity.astralib.paper.api.message.PaperMessenger;
import de.unknowncity.plots.PlotsPlugin;
import de.unknowncity.plots.plot.Plot;
import de.unknowncity.plots.service.PlotService;
import net.kyori.adventure.text.Component;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.Rotatable;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.block.sign.Side;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Transformation;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.spongepowered.configurate.NodePath;

import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

public class SignManager {
    private final HashMap<UUID, SignEditSession> editSessions = new HashMap<>();
    private final PlotsPlugin plugin;
    public static final NamespacedKey SIGN_GLOW = new NamespacedKey("ucplots", "signglow");

    public SignManager(PlotsPlugin plugin) {
        this.plugin = plugin;
    }

    public SignEditSession openEditSession(Player player) {
        var editSession = new SignEditSession(plugin);
        editSessions.put(player.getUniqueId(), editSession);
        return editSession;
    }

    public Optional<SignEditSession> findOpenEditSession(Player player) {
        return editSessions.containsKey(player.getUniqueId()) ? Optional.of(editSessions.get(player.getUniqueId())) : Optional.empty();
    }

    public void closeEditSession(Player player) {
        findOpenEditSession(player).ifPresent(signEditSession -> {
            signEditSession.finish();
            editSessions.remove(player.getUniqueId());
        });
    }

    public void collectGarbage() {
        plugin.serviceRegistry().getRegistered(PlotService.class).plotCache().forEach((s, foundPlot) -> foundPlot.signs().forEach(plotSign -> {
            setOutline(foundPlot, plotSign, false);
        }));
    }

    public static void updateSings(Plot plot, PaperMessenger messenger) {
        plot.signs().forEach(relativePlotLocation -> {
            var loc = new Location(plot.world(), relativePlotLocation.x(), relativePlotLocation.y(), relativePlotLocation.z());
            var block = plot.world().getBlockAt(loc);

            if (!MaterialTags.SIGNS.isTagged(block)) {
                plot.signs().remove(relativePlotLocation);
                return;
            }

            var state = plot.state().name().toLowerCase();
            Sign sign = (Sign) block.getState();
            sign.getSide(Side.FRONT).line(0, messenger.component(Language.GERMAN, NodePath.path("sign", state, "line-1"), plot.tagResolvers(messenger)));
            sign.getSide(Side.FRONT).line(1, messenger.component(Language.GERMAN, NodePath.path("sign", state, "line-2"), plot.tagResolvers(messenger)));
            sign.getSide(Side.FRONT).line(2, messenger.component(Language.GERMAN, NodePath.path("sign", state, "line-3"), plot.tagResolvers(messenger)));
            sign.getSide(Side.FRONT).line(3, messenger.component(Language.GERMAN, NodePath.path("sign", state, "line-4"), plot.tagResolvers(messenger)));
            sign.update();
        });
    }

    public static void clearSign(Location location) {
        Sign sign = (Sign) location.getBlock().getState();
        sign.getSide(Side.FRONT).line(0, Component.empty());
        sign.getSide(Side.FRONT).line(1, Component.empty());
        sign.getSide(Side.FRONT).line(2, Component.empty());
        sign.getSide(Side.FRONT).line(3, Component.empty());
        sign.update();
    }

    public static void setOutline(Plot plot, PlotSign plotSign, boolean show) {
        var location = new Location(plot.world(), plotSign.x(), plotSign.y(), plotSign.z(), plotSign.yaw(), plotSign.pitch());
        if (show) {
            var block = location.getBlock();
            var entityLocation = block.getBoundingBox().getCenter().toLocation(plot.world());
            var signWidth = 0.09f;
            plot.world().spawn(entityLocation, BlockDisplay.class, blockDisplay -> {
                blockDisplay.setBlock(Material.RED_CONCRETE.createBlockData());
                blockDisplay.setRotation(yawRotation(block), location.getPitch());
                blockDisplay.setTransformation(new Transformation(
                        new Vector3f(-0.5025f, block.getBlockData() instanceof WallSign ? -0.3f : +0.05f, -signWidth / 2),
                        new Quaternionf(0.0, 0.0 ,0.0, 1.0),
                        new Vector3f(1.05f, 0.55f, signWidth),
                        new Quaternionf(0.0, 0.0 ,0.0, 1.0)
                ));
                blockDisplay.setGlowing(true);
                blockDisplay.setGlowColorOverride(Color.RED);
                blockDisplay.getPersistentDataContainer().set(SIGN_GLOW, PersistentDataType.BOOLEAN, true);
            });
            if (!(block.getBlockData() instanceof WallSign)) {
                plot.world().spawn(entityLocation, BlockDisplay.class, blockDisplay -> {
                    blockDisplay.setBlock(Material.RED_CONCRETE.createBlockData());
                    blockDisplay.setRotation(yawRotation(block), location.getPitch());
                    blockDisplay.setTransformation(new Transformation(
                            new Vector3f(-signWidth / 2, - 0.5f, -signWidth / 2),
                            new Quaternionf(0.0, 0.0 ,0.0, 1.0),
                            new Vector3f(signWidth, 0.6f, signWidth),
                            new Quaternionf(0.0, 0.0 ,0.0, 1.0)
                    ));
                    blockDisplay.setGlowing(true);
                    blockDisplay.setGlowColorOverride(Color.RED);
                    blockDisplay.getPersistentDataContainer().set(SIGN_GLOW, PersistentDataType.BOOLEAN, true);
                });
            }
        } else {
            location.getNearbyEntities(1, 1, 1).forEach(entity -> {
                if (entity.getPersistentDataContainer().has(SIGN_GLOW)) {
                    entity.remove();
                }
            });
        }
    }
    
    private static float yawRotation(Block block) {
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

    private static float blockFaceToYaw(BlockFace face) {
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