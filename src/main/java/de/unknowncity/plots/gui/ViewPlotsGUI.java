package de.unknowncity.plots.gui;

import de.unknowncity.plots.PlotsPlugin;
import de.unknowncity.plots.gui.items.*;
import de.unknowncity.plots.plot.flag.PlotInteractable;
import de.unknowncity.plots.service.PlotService;
import de.unknowncity.plots.service.plot.InteractablesService;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.spongepowered.configurate.NodePath;
import xyz.xenondevs.invui.gui.Markers;
import xyz.xenondevs.invui.gui.PagedGui;
import xyz.xenondevs.invui.gui.Structure;
import xyz.xenondevs.invui.item.Item;
import xyz.xenondevs.invui.window.Window;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ViewPlotsGUI {

    public static void open(Player player, PlotsPlugin plugin) {
        var plotService = plugin.serviceRegistry().getRegistered(PlotService.class);

        var title = plugin.messenger().component(player, NodePath.path("gui", "viewplots", "title"));

        var plotsOwned = plotService.findPlotsByOwnerUUID(player.getUniqueId());
        var plotsMemberOf = plotService.findPlotsByMember(player.getUniqueId());

        var items = Stream.concat(plotsOwned.stream(), plotsMemberOf.stream())
                .map(plot -> new ManagePlotItem(plugin, plot))
                .toList();

        var gui = PagedGui.ofItems(
                new Structure(
                        "# # # # # # # # #",
                        "# . . . . . . . #",
                        "# . . . . . . . #",
                        "# . . . . . . . #",
                        "# . . . . . . . #",
                        "# # < # # # > # #"
                )
                        .addIngredient('<', new PrevPageItem(ItemStack.of(Material.PAPER), plugin.messenger(), plugin.configuration().gui()))
                        .addIngredient('>', new NextPageItem(ItemStack.of(Material.PAPER), plugin.messenger(), plugin.configuration().gui()))
                        .addIngredient('.', Markers.CONTENT_LIST_SLOT_HORIZONTAL),

                items
        );

        var window = Window.builder().setUpperGui(gui).setTitle(title).build(player);
        window.open();
    }
}