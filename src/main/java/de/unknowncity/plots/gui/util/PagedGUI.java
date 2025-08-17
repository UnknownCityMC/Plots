package de.unknowncity.plots.gui.util;

import de.unknowncity.astralib.paper.api.message.PaperMessenger;
import de.unknowncity.plots.gui.PlotMainGUI;
import de.unknowncity.plots.gui.items.NextPageItem;
import de.unknowncity.plots.gui.items.PrevPageItem;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import xyz.xenondevs.invui.gui.Markers;
import xyz.xenondevs.invui.gui.PagedGui;
import xyz.xenondevs.invui.gui.Structure;
import xyz.xenondevs.invui.item.Item;
import xyz.xenondevs.invui.window.Window;

import java.util.List;

public class PagedGUI {

    public static Window createAndOpenPagedGUI(PaperMessenger messenger, Component title, Item backItem, List<? extends Item> items, Player player) {
        var gui = PagedGui.ofItems(
                        new Structure(
                                "# # # # # # # # #",
                                "# . . . . . . . #",
                                "# . . . . . . . #",
                                "# . . . . . . . #",
                                "# . . . . . . . #",
                                "# # < # # # > # B"
                        ).addIngredient('#', PlotMainGUI.BORDER_ITEM)
                                .addIngredient('B', backItem)
                                .addIngredient('<', new PrevPageItem(ItemStack.of(Material.PAPER), messenger))
                                .addIngredient('>', new NextPageItem(ItemStack.of(Material.PAPER), messenger))
                                .addIngredient('.', Markers.CONTENT_LIST_SLOT_HORIZONTAL),
                        items
                );

        return Window.builder().setUpperGui(gui).setTitle(title).build(player);
    }
}
