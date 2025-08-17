package de.unknowncity.plots.gui;

import de.unknowncity.plots.PlotsPlugin;
import de.unknowncity.plots.gui.items.ManageBannedMember;
import de.unknowncity.plots.gui.items.NextPageItem;
import de.unknowncity.plots.gui.items.PreparedItems;
import de.unknowncity.plots.gui.items.PrevPageItem;
import de.unknowncity.plots.plot.Plot;
import de.unknowncity.plots.service.PlotService;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.spongepowered.configurate.NodePath;
import xyz.xenondevs.invui.gui.Markers;
import xyz.xenondevs.invui.gui.PagedGui;
import xyz.xenondevs.invui.gui.Structure;
import xyz.xenondevs.invui.window.Window;

public class BannedPlayersGUI {

    public static void open(Player player, Plot plot, PlotsPlugin plugin) {
        var messenger = plugin.messenger();
        var plotService = plugin.serviceRegistry().getRegistered(PlotService.class);

        var title = messenger.component(player, NodePath.path("gui", "banned-players", "title"));

        var bannedPlayers = plot.deniedPlayers();

        var items = bannedPlayers.stream().map(bannedPlayer -> new ManageBannedMember(plot, bannedPlayer, messenger)).toList();

        var gui = PagedGui.ofItems(
                new Structure(
                        "# # # # # # # # #",
                        "# . . . . . . . #",
                        "# . . . . . . . #",
                        "# . . . . . . . #",
                        "# . . . . . . . #",
                        "# # < # + # > # B"
                ).addIngredient('#', PlotMainGUI.BORDER_ITEM)
                        .addIngredient('B', PreparedItems.back(player, "banned-players", plugin, () -> PlotMainGUI.open(player, plot, plugin)))
                        .addIngredient('<', new PrevPageItem(ItemStack.of(Material.PAPER), messenger))
                        .addIngredient('>', new NextPageItem(ItemStack.of(Material.PAPER), messenger))
                        .addIngredient('+', PreparedItems.addBannedPlayer(player, plot, plugin))
                        .addIngredient('.', Markers.CONTENT_LIST_SLOT_HORIZONTAL),
                items
        );

        gui.setBackground(PlotMainGUI.FILLER_ITEM.getItemProvider(player));

        var window = Window.builder().setUpperGui(gui).setTitle(title).build(player);
        window.addCloseHandler((reason -> plotService.savePlot(plot)));
        window.open();
    }
}
