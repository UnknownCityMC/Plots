package de.unknowncity.plots.plot.flag;

import de.unknowncity.plots.plot.access.type.PlotMemberRole;
import de.unknowncity.plots.plot.access.type.PlotAccessModifier;
import org.bukkit.Material;
import org.bukkit.Tag;

import java.util.LinkedList;
import java.util.List;

public class PlotInteractable {
    private final Material blockType;
    private PlotAccessModifier accessModifier;

    private PlotInteractable(Material blockType, PlotAccessModifier accessModifier) {
        this.blockType = blockType;
        this.accessModifier = accessModifier;
    }

    public boolean hasAccess(PlotMemberRole role) {
        if (accessModifier == PlotAccessModifier.NOBODY) {
            return false;
        }
        if (accessModifier == PlotAccessModifier.EVERYBODY) {
            return true;
        }
        return role.ordinal() + 1 <= accessModifier.ordinal();
    }


    public static List<Material> allValidTypes() {
        var validTypes = new LinkedList<Material>();
        validTypes.add(Material.CAKE);
        validTypes.add(Material.JUKEBOX);
        validTypes.add(Material.NOTE_BLOCK);
        validTypes.add(Material.CAULDRON);
        validTypes.add(Material.FLOWER_POT);
        validTypes.add(Material.DECORATED_POT);
        validTypes.add(Material.ANVIL);
        validTypes.add(Material.CHIPPED_ANVIL);
        validTypes.add(Material.DAMAGED_ANVIL);
        validTypes.add(Material.CHEST);
        validTypes.add(Material.TRAPPED_CHEST);
        validTypes.add(Material.ENDER_CHEST);
        validTypes.add(Material.BARREL);
        validTypes.add(Material.CHISELED_BOOKSHELF);
        validTypes.add(Material.HOPPER);
        validTypes.add(Material.DROPPER);
        validTypes.add(Material.DISPENSER);
        validTypes.add(Material.FURNACE);
        validTypes.add(Material.BLAST_FURNACE);
        validTypes.add(Material.SMOKER);
        validTypes.add(Material.LOOM);
        validTypes.add(Material.CARTOGRAPHY_TABLE);
        validTypes.add(Material.GRINDSTONE);
        validTypes.add(Material.SMITHING_TABLE);
        validTypes.add(Material.LODESTONE);
        validTypes.add(Material.STONECUTTER);
        validTypes.add(Material.BELL);
        validTypes.add(Material.LECTERN);
        validTypes.add(Material.BEEHIVE);
        validTypes.add(Material.BEE_NEST);
        validTypes.add(Material.CRAFTING_TABLE);
        validTypes.add(Material.CRAFTER);
        validTypes.add(Material.BREWING_STAND);
        validTypes.add(Material.ENCHANTING_TABLE);
        validTypes.add(Material.BEACON);
        validTypes.add(Material.LEVER);
        validTypes.add(Material.REPEATER);
        validTypes.add(Material.COMPARATOR);
        validTypes.add(Material.DAYLIGHT_DETECTOR);
        validTypes.add(Material.CAMPFIRE);
        validTypes.add(Material.SOUL_CAMPFIRE);
        validTypes.add(Material.BIG_DRIPLEAF);
        validTypes.add(Material.RESPAWN_ANCHOR);
        validTypes.addAll(Tag.BUTTONS.getValues());
        validTypes.addAll(Tag.PRESSURE_PLATES.getValues());
        validTypes.addAll(Tag.TRAPDOORS.getValues());
        validTypes.addAll(Tag.DOORS.getValues());
        validTypes.addAll(Tag.FENCE_GATES.getValues());
        validTypes.addAll(Tag.CANDLES.getValues());
        return validTypes;
    }

    public static List<PlotInteractable> defaults() {
        return allValidTypes().stream().map(material -> PlotInteractable.create(material, PlotAccessModifier.TEMP_MEMBER)).toList();
    }

    public static PlotInteractable create(Material material, PlotAccessModifier accessModifier) {
        return new PlotInteractable(material, accessModifier);
    }

    public PlotAccessModifier accessModifier() {
        return accessModifier;
    }

    public void accessModifier(PlotAccessModifier accessModifier) {
        this.accessModifier = accessModifier;
    }

    public Material blockType() {
        return blockType;
    }
}
