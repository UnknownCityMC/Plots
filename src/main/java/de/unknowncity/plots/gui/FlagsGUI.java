package de.unknowncity.plots.gui;

import de.unknowncity.astralib.paper.api.item.ItemBuilder;
import de.unknowncity.plots.PlotsPlugin;
import de.unknowncity.plots.plot.Plot;
import de.unknowncity.plots.gui.items.FlagItem;
import de.unknowncity.plots.gui.items.NextPageItem;
import de.unknowncity.plots.gui.items.PrevPageItem;
import de.unknowncity.plots.plot.flag.FlagCategory;
import de.unknowncity.plots.plot.flag.PlotFlagCategories;
import de.unknowncity.plots.service.PlotService;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.spongepowered.configurate.NodePath;
import xyz.xenondevs.inventoryaccess.component.AdventureComponentWrapper;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.gui.PagedGui;
import xyz.xenondevs.invui.gui.TabGui;
import xyz.xenondevs.invui.gui.structure.Markers;
import xyz.xenondevs.invui.gui.structure.Structure;
import xyz.xenondevs.invui.item.Item;
import xyz.xenondevs.invui.item.impl.SimpleItem;
import xyz.xenondevs.invui.window.Window;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class FlagsGUI {

    public static void open(Player player, Plot plot, PlotsPlugin plugin) {
        var messenger = plugin.messenger();
        var plotService = plugin.serviceRegistry().getRegistered(PlotService.class);

        var title = messenger.component(player, NodePath.path("gui", "flags", "title"));

        var backItem = new SimpleItem(ItemBuilder.of(Material.BARRIER).name(
                messenger.component(player, NodePath.path("gui", "flags", "item", "back", "name"))
        ).item(), click -> PlotMainGUI.open(player, plot, plugin));

        List<Item> blockFlags = PlotFlagCategories.block().stream().map(plotFlagClass -> {
            var flag = plugin.serviceRegistry().getRegistered(PlotService.class).flagRegistry().getRegistered(plotFlagClass);

            return new FlagItem<>(player, flag, plot, plugin);
        }).collect(Collectors.toList());

        var guis = new ArrayList<Gui>();
        var innerStructure = new Structure(
                ". . . . . .",
                ". . . . . .",
                ". . . . . .",
                ". . . . . .",
                "# < # # > #"
        )
                .addIngredient('<', new PrevPageItem(ItemStack.of(Material.PAPER), messenger, player))
                .addIngredient('>', new NextPageItem(ItemStack.of(Material.PAPER), messenger, player))
                .addIngredient('#', PlotMainGUI.BORDER_ITEM)
                .addIngredient('.', Markers.CONTENT_LIST_SLOT_HORIZONTAL);

        //guis.add(PagedGui.ofItems(innerStructure, playerFlags));
        //guis.add(PagedGui.ofItems(innerStructure, entityFlags));
        //guis.add(PagedGui.ofItems(innerStructure, vehicleFlags));
        guis.add(PagedGui.ofItems(innerStructure, blockFlags));

        var playerTab = new SimpleItem(
                ItemBuilder.of(Material.PLAYER_HEAD).name(
                                messenger.component(player, NodePath.path("gui", "flags", "item", "player-tab", "name"))
                        )
                        .lore(
                                messenger.componentList(player, NodePath.path("gui", "flags", "item", "player-tab", "lore"))
                        ).item()
        );

        var entityTab = new SimpleItem(
                ItemBuilder.of(Material.ZOMBIE_SPAWN_EGG).name(
                                messenger.component(player, NodePath.path("gui", "flags", "item", "entity-tab", "name"))
                        )
                        .lore(
                                messenger.componentList(player, NodePath.path("gui", "flags", "item", "entity-tab", "lore"))
                        ).item()
        );

        var vehicleTab = new SimpleItem(
                ItemBuilder.of(Material.MINECART).name(
                                messenger.component(player, NodePath.path("gui", "flags", "item", "vehicle-tab", "name"))
                        )
                        .lore(
                                messenger.componentList(player, NodePath.path("gui", "flags", "item", "vehicle-tab", "lore"))
                        ).item()
        );

        var blockTab = new SimpleItem(
                ItemBuilder.of(Material.BLUE_ICE).name(
                                messenger.component(player, NodePath.path("gui", "flags", "item", "block-tab", "name"))
                        )
                        .lore(
                                messenger.componentList(player, NodePath.path("gui", "flags", "item", "block-tab", "lore"))
                        ).item()
        );

        var tabbedGUI = TabGui.normal()
                .setStructure(
                        "# # # # # # # # #",
                        "P # . . . . . . #",
                        "E # . . . . . . #",
                        "V # . . . . . . #",
                        "B # . . . . . . #",
                        "# # . . . . . . X"
                )
                .addIngredient('#', PlotMainGUI.BORDER_ITEM)
                .addIngredient('X', backItem)
                .addIngredient('P', playerTab)
                .addIngredient('E', entityTab)
                .addIngredient('V', vehicleTab)
                .addIngredient('B', blockTab)
                .addIngredient('.', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
                .setTabs(guis)
                .build();


        Window.single().setGui(tabbedGUI).setTitle(new AdventureComponentWrapper(title)).addCloseHandler(() -> {
            plotService.savePlot(plot);
        }).open(player);
    }
}