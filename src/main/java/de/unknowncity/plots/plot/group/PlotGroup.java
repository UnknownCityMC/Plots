package de.unknowncity.plots.plot.group;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class PlotGroup {
    public static final ItemStack DEFAULT_DISPLAY_ITEM = new ItemStack(Material.PAPER);
    private final String name;
    private ItemStack displayItem;

    public PlotGroup(String name) {
        this.name = name;
        this.displayItem = DEFAULT_DISPLAY_ITEM;
    }

    public PlotGroup(String name, ItemStack displayItem) {
        this.name = name;
        this.displayItem = displayItem;
    }

    public PlotGroup(String name, byte[] displayItem) {
        this.name = name;
        this.displayItem = ItemStack.deserializeBytes(displayItem);
    }

    public String name() {
        return name;
    }

    public ItemStack displayItem() {
        return displayItem;
    }

    public void resetDisplayItem() {
        this.displayItem = DEFAULT_DISPLAY_ITEM;
    }

    private static final String BASE_PERMISSION = "plots.limit.";

    /**
     * Builds a permission path for a given plot group name
     * Useful for finding the plot limit of a player for a specific plot group
     *
     * @param groupName the name of the plot group
     * @return a permission path
     */
    public static String permission(String groupName) {
        return BASE_PERMISSION + groupName;
    }

    public void displayItem(@NotNull ItemStack itemStack) {
        this.displayItem = itemStack;
    }
}
