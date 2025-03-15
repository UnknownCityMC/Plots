package de.unknowncity.plots.gui;

import de.unknowncity.astralib.paper.api.item.ItemBuilder;
import de.unknowncity.plots.PlotsPlugin;
import de.unknowncity.plots.data.model.plot.Plot;
import de.unknowncity.plots.gui.items.ManageFriendItem;
import de.unknowncity.plots.service.PlotService;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.spongepowered.configurate.NodePath;
import xyz.xenondevs.inventoryaccess.component.AdventureComponentWrapper;
import xyz.xenondevs.invui.item.Item;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.builder.SkullBuilder;
import xyz.xenondevs.invui.item.impl.SimpleItem;
import xyz.xenondevs.invui.util.MojangApiUtils;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class FriendsGUI {

    public static void open(Player player, Plot plot, PlotsPlugin plugin) {
        var messenger = plugin.messenger();
        var plotService = plugin.serviceRegistry().getRegistered(PlotService.class);

        var title = messenger.component(player, NodePath.path("gui", "friends", "title"));

        var backItem = new SimpleItem(ItemBuilder.of(Material.BARRIER).name(
                messenger.component(player, NodePath.path("gui", "friends", "item", "back", "name"))
        ).item(), click -> PlotMainGUI.open(player, plot, plugin));

        var friends = plot.members();

        List<Item> items = friends.stream().map(friend -> {
            ItemProvider skull;
            try {
                skull = new SkullBuilder(friend.memberID()).setDisplayName(new AdventureComponentWrapper(
                        messenger.component(player, NodePath.path("gui", "friends", "item", "member", "name"), Placeholder.parsed("name", friend.memberID().toString())
                        )));
            } catch (MojangApiUtils.MojangApiException | IOException e) {
                skull = new xyz.xenondevs.invui.item.builder.ItemBuilder((Material.PLAYER_HEAD)).setDisplayName(
                        new AdventureComponentWrapper(
                                messenger.component(player, NodePath.path("gui", "friends", "item", "member", "name"), Placeholder.parsed("name", friend.memberID().toString())
                                ))
                );
            }
            return new ManageFriendItem(skull, plotService, plot, friend);
        }).collect(Collectors.toList());

        PagedGUI.createAndOpenPagedGUI(messenger, title, backItem, items, player);
    }
}
