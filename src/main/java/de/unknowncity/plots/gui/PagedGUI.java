package de.unknowncity.plots.gui;

import de.unknowncity.astralib.paper.api.message.PaperMessenger;
import de.unknowncity.plots.gui.items.NextPageItem;
import de.unknowncity.plots.gui.items.PrevPageItem;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import xyz.xenondevs.inventoryaccess.component.AdventureComponentWrapper;
import xyz.xenondevs.invui.gui.PagedGui;
import xyz.xenondevs.invui.gui.structure.Markers;
import xyz.xenondevs.invui.item.Item;
import xyz.xenondevs.invui.item.impl.SimpleItem;
import xyz.xenondevs.invui.window.Window;

import java.util.List;

public class PagedGUI {

    public static void createAndOpenPagedGUI(PaperMessenger messenger, Component title, SimpleItem backItem, List<Item> items, Player player) {
        var gui = PagedGui.items()
                .setStructure(
                        "# # # # # # # # #",
                        "# . . . . . . . #",
                        "# . . . . . . . #",
                        "# . . . . . . . #",
                        "# . . . . . . . #",
                        "B # < # # # > # #"
                )
                .addIngredient('#', PlotMainGUI.BORDER_ITEM)
                .addIngredient('B', backItem)
                .addIngredient('<', new PrevPageItem(ItemStack.of(Material.PAPER), messenger, player))
                .addIngredient('>', new NextPageItem(ItemStack.of(Material.PAPER), messenger, player))
                .addIngredient('.', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
                .setContent(items)
                .build();


        Window.single().setGui(gui).setTitle(new AdventureComponentWrapper(title)).open(player);
    }
}
