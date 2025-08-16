package de.unknowncity.plots.gui;

import de.unknowncity.plots.PlotsPlugin;
import de.unknowncity.plots.gui.items.ManageBannedMember;
import de.unknowncity.plots.gui.util.PagedGUI;
import de.unknowncity.plots.plot.Plot;
import de.unknowncity.plots.service.PlotService;
import de.unknowncity.plots.util.SkullHelper;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.spongepowered.configurate.NodePath;
import xyz.xenondevs.invui.item.Item;
import xyz.xenondevs.invui.item.ItemBuilder;

public class BannedPlayersGUI {

    public static void open(Player player, Plot plot, PlotsPlugin plugin) {
        var messenger = plugin.messenger();
        var plotService = plugin.serviceRegistry().getRegistered(PlotService.class);

        var title = messenger.component(player, NodePath.path("gui", "banned-players", "title"));

        var backItem = Item.builder().setItemProvider(new ItemBuilder(Material.BARRIER).setName(
                        messenger.component(player, NodePath.path("gui", "banned-players", "item", "back", "name"))
                )).addClickHandler(click -> PlotMainGUI.open(player, plot, plugin))
                .build();

        var bannedPlayers = plot.bannedPlayers();

        var items = bannedPlayers.stream().map(bannedPlayer -> {

            var skull = SkullHelper.getSkull(bannedPlayer.uuid());

            var itemBuilder = new ItemBuilder(skull)
                    .setName(messenger.component(player, NodePath.path("gui", "banned-players", "item", "member", "name"), Placeholder.parsed("name", bannedPlayer.name())))
                    .addLoreLines(messenger.component(player, NodePath.path("gui", "banned-players", "item", "member", "lore"), Placeholder.parsed("name", bannedPlayer.name())));

            return new ManageBannedMember(itemBuilder, plotService, plot, bannedPlayer);
        }).toList();

        var gui = PagedGUI.createAndOpenPagedGUI(messenger, title, backItem, items, player);
        gui.addCloseHandler((reason -> plotService.savePlot(plot)));
        gui.open();
    }
}
