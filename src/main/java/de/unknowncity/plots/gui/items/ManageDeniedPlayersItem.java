package de.unknowncity.plots.gui.items;

import de.unknowncity.astralib.paper.api.item.ItemBuilder;
import de.unknowncity.astralib.paper.api.message.PaperMessenger;
import de.unknowncity.plots.PlotsPlugin;
import de.unknowncity.plots.plot.model.Plot;
import de.unknowncity.plots.plot.model.PlotPlayer;
import de.unknowncity.plots.service.plot.AccessService;
import de.unknowncity.plots.util.SkullHelper;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.NodePath;
import xyz.xenondevs.invui.Click;
import xyz.xenondevs.invui.gui.PagedGui;
import xyz.xenondevs.invui.item.AbstractPagedGuiBoundItem;
import xyz.xenondevs.invui.item.Item;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.ItemWrapper;

public class ManageDeniedPlayersItem extends AbstractPagedGuiBoundItem {
    private final PaperMessenger messenger;
    private final Plot plot;
    private final PlotPlayer bannedPlayer;
    private final PlotsPlugin plugin;

    public ManageDeniedPlayersItem(Plot plot, PlotPlayer bannedPlayer, PaperMessenger messenger, PlotsPlugin plugin) {
        this.messenger = messenger;
        this.plot = plot;
        this.bannedPlayer = bannedPlayer;
        this.plugin = plugin;
    }

    @Override
    public @NotNull ItemProvider getItemProvider(@NotNull Player player) {
        var skull = SkullHelper.getSkull(bannedPlayer.uuid());

        var name = messenger.component(player, NodePath.path("gui", "banned-players", "item", "banned-player", "name"),
                Placeholder.parsed("name", bannedPlayer.name()));

        var lore = messenger.componentList(player, NodePath.path("gui", "banned-players", "item", "banned-player", "lore"),
                Placeholder.parsed("name", bannedPlayer.name()));

        var builder = ItemBuilder.of(skull)
                .name(name)
                .lore(lore)
                .item();

        return new ItemWrapper(builder);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull Click click) {
        if (clickType == ClickType.SHIFT_LEFT) {
            plugin.serviceRegistry().getRegistered(AccessService.class).unDenyPlayer(plot, bannedPlayer.uuid());

            var gui = (PagedGui<Item>) getGui();
            gui.setContent(gui.getContent().stream().filter(item -> !item.equals(this)).toList());
            player.playSound(player.getLocation(), "entity.item.break", 1, 1);

            notifyWindows();
        }
    }
}
