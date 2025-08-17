package de.unknowncity.plots.gui.items;

import de.unknowncity.astralib.paper.api.message.PaperMessenger;
import de.unknowncity.plots.gui.GuiPlaceholders;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.NodePath;
import xyz.xenondevs.invui.Click;
import xyz.xenondevs.invui.item.AbstractPagedGuiBoundItem;
import xyz.xenondevs.invui.item.ItemBuilder;
import xyz.xenondevs.invui.item.ItemProvider;

public class PrevPageItem extends AbstractPagedGuiBoundItem {
    private final ItemStack displayItem;
    private final PaperMessenger messenger;

    public PrevPageItem(ItemStack displayItem, PaperMessenger messenger) {
        this.displayItem = displayItem;
        this.messenger = messenger;
    }

    @Override
    public @NotNull ItemProvider getItemProvider(@NotNull Player player) {
        boolean hasPrevPage = getGui().getPage() > 1;
        var builder = new ItemBuilder(displayItem);

        var lore = messenger.componentList(
                player,
                NodePath.path("gui", "item", "prev-page", "lore", hasPrevPage ? "has-prev" : "no-prev"),
                GuiPlaceholders.paged(getGui())
        ).toArray(Component[]::new);

        var name = messenger.component(
                player,
                NodePath.path("gui", "item", "prev-page", "display-name", hasPrevPage ? "has-prev" : "no-prev"),
                GuiPlaceholders.paged(getGui())
        );

        builder.setName(name).addLoreLines(lore);

        return builder;
    }

    @Override
    public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull Click click) {
        getGui().setPage(getGui().getPage() - 1);
        player.playSound(player.getLocation(), "item.book.page_turn", 1, 1);
    }
}