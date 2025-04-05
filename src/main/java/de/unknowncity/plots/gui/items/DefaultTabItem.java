package de.unknowncity.plots.gui.items;

import org.bukkit.inventory.ItemStack;
import xyz.xenondevs.invui.gui.TabGui;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.item.impl.controlitem.TabItem;

public class DefaultTabItem extends TabItem {
    private final ItemProvider itemProvider;

    public DefaultTabItem(int tab, ItemStack itemStack) {
        super(tab);
        this.itemProvider = new ItemBuilder(itemStack);
    }

    @Override
    public ItemProvider getItemProvider(TabGui tabGui) {
        return itemProvider;
    }
}
