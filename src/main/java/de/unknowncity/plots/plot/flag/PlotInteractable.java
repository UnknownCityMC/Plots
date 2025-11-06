package de.unknowncity.plots.plot.flag;

import de.unknowncity.plots.plot.access.type.PlotMemberRole;
import de.unknowncity.plots.plot.access.type.PlotAccessModifier;
import org.bukkit.Material;
import org.bukkit.Tag;

import javax.annotation.Nullable;
import java.util.LinkedList;
import java.util.List;

public class PlotInteractable {
    private @Nullable String plotId;
    private final Material blockType;
    private PlotAccessModifier accessModifier;
    public static final LinkedList<Material> VALID_TYPES = new LinkedList<>();

    public PlotInteractable(String plotId, Material blockType, PlotAccessModifier accessModifier) {
        this.plotId = plotId;
        this.blockType = blockType;
        this.accessModifier = accessModifier;
    }

    private PlotInteractable(Material blockType, PlotAccessModifier accessModifier) {
        this.blockType = blockType;
        this.accessModifier = accessModifier;
    }

    /**
     * Checks if the role of a plot member is either on the same level or higher than the required access modifier for
     * this interactable block
     * @param role the role to check for access
     * @return if the role should be able to interact with this block
     */
    public boolean hasAccess(PlotMemberRole role) {
        // As nobody is allowed to interact, the role is irrelevant
        if (accessModifier == PlotAccessModifier.NOBODY) {
            return false;
        }

        // As everybody is allowed to interact, the role is irrelevant
        if (accessModifier == PlotAccessModifier.EVERYBODY) {
            return true;
        }

        // Check if the plot member role is higher or equal to the access modifier
        return role.ordinal() + 1 <= accessModifier.ordinal();
    }

    static {
        VALID_TYPES.add(Material.CAKE);
        VALID_TYPES.add(Material.JUKEBOX);
        VALID_TYPES.add(Material.NOTE_BLOCK);
        VALID_TYPES.add(Material.CAULDRON);
        VALID_TYPES.add(Material.FLOWER_POT);
        VALID_TYPES.add(Material.DECORATED_POT);
        VALID_TYPES.add(Material.ANVIL);
        VALID_TYPES.add(Material.CHIPPED_ANVIL);
        VALID_TYPES.add(Material.DAMAGED_ANVIL);
        VALID_TYPES.add(Material.CHEST);
        VALID_TYPES.addAll(Tag.COPPER_CHESTS.getValues());
        VALID_TYPES.add(Material.TRAPPED_CHEST);
        VALID_TYPES.add(Material.ENDER_CHEST);
        VALID_TYPES.add(Material.BARREL);
        VALID_TYPES.add(Material.CHISELED_BOOKSHELF);
        VALID_TYPES.add(Material.HOPPER);
        VALID_TYPES.add(Material.DROPPER);
        VALID_TYPES.add(Material.DISPENSER);
        VALID_TYPES.add(Material.FURNACE);
        VALID_TYPES.add(Material.BLAST_FURNACE);
        VALID_TYPES.add(Material.SMOKER);
        VALID_TYPES.add(Material.LOOM);
        VALID_TYPES.add(Material.CARTOGRAPHY_TABLE);
        VALID_TYPES.add(Material.GRINDSTONE);
        VALID_TYPES.add(Material.SMITHING_TABLE);
        VALID_TYPES.add(Material.LODESTONE);
        VALID_TYPES.add(Material.STONECUTTER);
        VALID_TYPES.add(Material.BELL);
        VALID_TYPES.add(Material.LECTERN);
        VALID_TYPES.add(Material.BEEHIVE);
        VALID_TYPES.add(Material.BEE_NEST);
        VALID_TYPES.add(Material.CRAFTING_TABLE);
        VALID_TYPES.add(Material.CRAFTER);
        VALID_TYPES.add(Material.BREWING_STAND);
        VALID_TYPES.add(Material.ENCHANTING_TABLE);
        VALID_TYPES.add(Material.BEACON);
        VALID_TYPES.add(Material.LEVER);
        VALID_TYPES.add(Material.REPEATER);
        VALID_TYPES.add(Material.COMPARATOR);
        VALID_TYPES.add(Material.DAYLIGHT_DETECTOR);
        VALID_TYPES.add(Material.CAMPFIRE);
        VALID_TYPES.add(Material.SOUL_CAMPFIRE);
        VALID_TYPES.add(Material.BIG_DRIPLEAF);
        VALID_TYPES.add(Material.RESPAWN_ANCHOR);
        VALID_TYPES.addAll(Tag.BUTTONS.getValues());
        VALID_TYPES.addAll(Tag.PRESSURE_PLATES.getValues());
        VALID_TYPES.addAll(Tag.TRAPDOORS.getValues());
        VALID_TYPES.addAll(Tag.DOORS.getValues());
        VALID_TYPES.addAll(Tag.FENCE_GATES.getValues());
        VALID_TYPES.addAll(Tag.CANDLES.getValues());
    }

    public static List<PlotInteractable> defaults() {
        return VALID_TYPES.stream().map(material -> new PlotInteractable(material, PlotAccessModifier.TEMP_MEMBER)).toList();
    }

    public static PlotInteractable create(String plotId, Material material, PlotAccessModifier accessModifier) {
        return new PlotInteractable(plotId, material, accessModifier);
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

    @Nullable
    public String plotId() {
        return plotId;
    }
}
