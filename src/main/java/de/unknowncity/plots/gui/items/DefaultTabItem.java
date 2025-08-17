package de.unknowncity.plots.gui.items;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.Click;
import xyz.xenondevs.invui.item.AbstractTabGuiBoundItem;
import xyz.xenondevs.invui.item.ItemBuilder;
import xyz.xenondevs.invui.item.ItemProvider;

public class DefaultTabItem extends AbstractTabGuiBoundItem {
    private final int tabIndex;
    private final ItemProvider itemProvider;

    public DefaultTabItem(int tabIndex, ItemStack itemStack) {
        this.tabIndex = tabIndex;
        this.itemProvider = new ItemBuilder(itemStack);
    }

    @Override
    public @NotNull ItemProvider getItemProvider(@NotNull Player viewer) {
        return itemProvider;
    }

    @Override
    public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull Click click) {
        this.getGui().setTab(tabIndex);
        player.playSound(player.getLocation(), "ui.button.click", 1, 1);
    }
}
