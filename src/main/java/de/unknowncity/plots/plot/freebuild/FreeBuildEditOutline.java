package de.unknowncity.plots.plot.freebuild;

import de.unknowncity.plots.PlotsPlugin;
import de.unknowncity.plots.util.WireframeCube;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Transformation;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.HashSet;
import java.util.Set;

public class FreeBuildEditOutline {
    private final Player player;
    private WireframeCube regionOutline;
    private Set<Entity> entities = new HashSet<>();

    public FreeBuildEditOutline(Player player) {
        this.player = player;
    }

    public void updateOutline(PlotsPlugin plugin, Location loc1, Location loc2, boolean show, boolean canAfford) {

        // Hide outline (remove display entity)
        for (Entity entity : entities) {
            entity.remove();
        }

        if (regionOutline != null) {
            regionOutline.removeWireframe();
            System.out.println("frame not null");
        }

        if (!show) {
            return;
        }

        // Show outline (spawn new display entity)
        spawnEdge(plugin, loc1);
        spawnEdge(plugin, loc2);

        if (loc1 != null && loc2 != null) {
            var locCube1 = loc1.clone();
            locCube1.setY(-64);
            var locCube2 = loc2.clone();
            locCube2.setY(320);

            var material = canAfford ? Material.LIME_STAINED_GLASS : Material.RED_STAINED_GLASS;
            var color = canAfford ? Color.LIME : Color.RED;

            this.regionOutline = new WireframeCube(material, color);
            regionOutline.createWireframeCube(player.getWorld(), locCube1, locCube2);
            regionOutline.show(plugin, player);
        }
    }

    private void spawnEdge(PlotsPlugin plugin, Location location) {
        if (location != null) {
            var entity = location.getWorld().spawn(location, BlockDisplay.class, blockDisplay -> {
                blockDisplay.setTransformation(new Transformation(
                        new Vector3f(0, 0, 0),
                        new Quaternionf(0.0, 0.0, 0.0, 1.0),
                        new Vector3f(1.01f, 1.01f, 1.01f),
                        new Quaternionf(0.0, 0.0, 0.0, 1.0)
                ));

                blockDisplay.setGlowColorOverride(Color.BLUE);

                applyCommonData(blockDisplay);
                player.showEntity(plugin, blockDisplay);
            });
            entities.add(entity);
        }
    }

    private static void applyCommonData(BlockDisplay blockDisplay) {
        blockDisplay.setBlock(Material.LIGHT_BLUE_STAINED_GLASS.createBlockData());
        blockDisplay.setBrightness(new Display.Brightness(15, 15));
        blockDisplay.setGlowing(true);
        blockDisplay.setPersistent(false);
        blockDisplay.setVisibleByDefault(false);
    }
}