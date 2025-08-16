package de.unknowncity.plots.gui;

import de.unknowncity.astralib.paper.api.item.ItemBuilder;
import de.unknowncity.plots.PlotsPlugin;
import de.unknowncity.plots.gui.items.ManageMembersItem;
import de.unknowncity.plots.gui.util.PagedGUI;
import de.unknowncity.plots.plot.Plot;
import de.unknowncity.plots.service.PlotService;
import de.unknowncity.plots.util.SkullHelper;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.spongepowered.configurate.NodePath;
import xyz.xenondevs.invui.item.Item;

public class MembersGUI {

    public static void open(Player player, Plot plot, PlotsPlugin plugin) {
        var messenger = plugin.messenger();
        var plotService = plugin.serviceRegistry().getRegistered(PlotService.class);

        var title = messenger.component(player, NodePath.path("gui", "members", "title"));

        var backItem = Item.builder().setItemProvider(ItemBuilder.of(Material.BARRIER).name(
                        messenger.component(player, NodePath.path("gui", "members", "item", "back", "name"))
                ).item())
                .addClickHandler(click -> PlotMainGUI.open(player, plot, plugin))
                .build();

        var members = plot.members();

        var items = members.stream().map(member -> {

            var skull = SkullHelper.getSkull(member.memberID());

            var itemBuilder = new xyz.xenondevs.invui.item.ItemBuilder(skull)
                    .setName(messenger.component(player, NodePath.path("gui", "banned-players", "item", "member", "name"), Placeholder.parsed("name", member.name())))
                    .addLoreLines(messenger.component(player, NodePath.path("gui", "banned-players", "item", "member", "lore"), Placeholder.parsed("name", member.name())));

            return new ManageMembersItem(itemBuilder, plotService, plot, member);
        }).toList();

        var gui = PagedGUI.createAndOpenPagedGUI(messenger, title, backItem, items, player);
        gui.addCloseHandler((reason) -> plotService.savePlot(plot));
        gui.open();
    }
}
