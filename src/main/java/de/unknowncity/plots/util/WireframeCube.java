package de.unknowncity.plots.util;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Display;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Transformation;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

import java.util.HashSet;
import java.util.Set;

public class WireframeCube {

    private final Set<BlockDisplay> wireframeEntities = new HashSet<>();
    private final Material material;
    private final Color glowOverrideColor;

    public WireframeCube(Material material, Color glowOverrideColor) {
        this.material = material;
        this.glowOverrideColor = glowOverrideColor;
    }

    /**
     * Create a wireframe cube between two positions
     * @param world the world to create the cube in
     * @param pos1 the first position
     * @param pos2 the second position
     */
    public void createWireframeCube(World world, Location pos1, Location pos2) {

        double minX = Math.min(pos1.getBlockX(), pos2.getBlockX());
        double maxX = Math.max(pos1.getBlockX(), pos2.getBlockX()) + 1;
        double minY = Math.min(pos1.getBlockY(), pos2.getBlockY());
        double maxY = Math.max(pos1.getBlockY(), pos2.getBlockY()) + 1;
        double minZ = Math.min(pos1.getBlockZ(), pos2.getBlockZ());
        double maxZ = Math.max(pos1.getBlockZ(), pos2.getBlockZ()) + 1;

        double width = maxX - minX;
        double height = maxY - minY;
        double depth = maxZ - minZ;

        float wireThickness = 0.05f;

        // bottom
        createWireframeLine(world, minX, minY, minZ, width, 0, 0, wireThickness);
        createWireframeLine(world, minX, minY, maxZ, width, 0, 0, wireThickness);
        createWireframeLine(world, minX, minY, minZ, 0, 0, depth, wireThickness);
        createWireframeLine(world, maxX, minY, minZ, 0, 0, depth, wireThickness);

        // top
        createWireframeLine(world, minX, maxY, minZ, width, 0, 0, wireThickness);
        createWireframeLine(world, minX, maxY, maxZ, width, 0, 0, wireThickness);
        createWireframeLine(world, minX, maxY, minZ, 0, 0, depth, wireThickness);
        createWireframeLine(world, maxX, maxY, minZ, 0, 0, depth, wireThickness);

        // sides
        createWireframeLine(world, minX, minY, minZ, 0, height, 0, wireThickness);
        createWireframeLine(world, maxX, minY, minZ, 0, height, 0, wireThickness);
        createWireframeLine(world, minX, minY, maxZ, 0, height, 0, wireThickness);
        createWireframeLine(world, maxX, minY, maxZ, 0, height, 0, wireThickness);
    }

    private void createWireframeLine(World world, double x, double y, double z,
                                     double lengthX, double lengthY, double lengthZ,
                                     float thickness) {

        if (lengthX == 0 && lengthY == 0 && lengthZ == 0) return;

        var startLocation = new Location(world, x, y, z);
        var display = world.spawn(startLocation, BlockDisplay.class);
        display.setBlock(material.createBlockData());

        Vector3f translation = new Vector3f(0, 0, 0);
        AxisAngle4f leftRotation = new AxisAngle4f();
        Vector3f scale = new Vector3f();
        AxisAngle4f rightRotation = new AxisAngle4f();

        if (Math.abs(lengthX) > 0) {
            // x
            scale.set((float) Math.abs(lengthX), thickness, thickness);
        } else if (Math.abs(lengthY) > 0) {
            // y
            scale.set(thickness, (float) Math.abs(lengthY), thickness);
        } else if (Math.abs(lengthZ) > 0) {
            // z
            scale.set(thickness, thickness, (float )Math.abs(lengthZ));
        }

        Transformation transformation = new Transformation(translation, leftRotation, scale, rightRotation);
        display.setTransformation(transformation);

        display.setBrightness(new Display.Brightness(15, 15));
        display.setViewRange(500.0f);
        display.setGlowing(true);
        display.setGlowColorOverride(glowOverrideColor);
        display.setPersistent(false);

        wireframeEntities.add(display);
    }

    /**
     * Remove wireframe
     */
    public void removeWireframe() {
        for (BlockDisplay entity : wireframeEntities) {
            entity.remove();
        }
        wireframeEntities.clear();
    }

    /**
     * Show wireframe to player
     * @param plugin the plugin
     * @param player the player to show the wireframe for
     */
    public void show(Plugin plugin, Player player) {
        for (BlockDisplay wireframeEntity : wireframeEntities) {
            player.showEntity(plugin, wireframeEntity);
        }
    }
}