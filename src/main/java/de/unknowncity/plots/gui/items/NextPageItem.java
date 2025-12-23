package de.unknowncity.plots.gui.items;

import de.unknowncity.astralib.paper.api.item.ItemBuilder;
import de.unknowncity.astralib.paper.api.message.PaperMessenger;
import de.unknowncity.plots.configuration.GuiSettings;
import de.unknowncity.plots.gui.GuiPlaceholders;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.NodePath;
import xyz.xenondevs.invui.Click;
import xyz.xenondevs.invui.item.AbstractPagedGuiBoundItem;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.ItemWrapper;

public class NextPageItem extends AbstractPagedGuiBoundItem {
    private final ItemStack displayItem;
    private final PaperMessenger messenger;
    private final GuiSettings guiSettings;

    public NextPageItem(ItemStack displayItem, PaperMessenger messenger, GuiSettings guiSettings) {
        this.displayItem = displayItem;
        this.messenger = messenger;
        this.guiSettings = guiSettings;
    }

    @Override
    public @NotNull ItemProvider getItemProvider(@NotNull Player player) {
        boolean hasNextPage = getGui().getPage() < getGui().getPageCount() - 1;

        var lore = messenger.componentList(
                player,
                NodePath.path("gui", "item", "next-page", "lore", hasNextPage ? "has-next" : "no-next"),
                GuiPlaceholders.paged(getGui())
        );

        var name = messenger.component(
                player,
                NodePath.path("gui", "item", "next-page", "display-name", hasNextPage ? "has-next" : "no-next"),
                GuiPlaceholders.paged(getGui())
        );

        var builder = ItemBuilder.of(displayItem)
                .itemModel(NamespacedKey.fromString(guiSettings.buttonModelNext()))
                .name(name)
                .lore(lore);

        return new ItemWrapper(builder.item());
    }

    @Override
    public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull Click click) {
        getGui().setPage(getGui().getPage() + 1);
        player.playSound(player.getLocation(), "item.book.page_turn", 1, 1);
    }
}
