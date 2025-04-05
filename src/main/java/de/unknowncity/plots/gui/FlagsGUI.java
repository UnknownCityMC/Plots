package de.unknowncity.plots.gui;

import de.unknowncity.astralib.paper.api.item.ItemBuilder;
import de.unknowncity.plots.PlotsPlugin;
import de.unknowncity.plots.gui.items.DefaultTabItem;
import de.unknowncity.plots.plot.Plot;
import de.unknowncity.plots.gui.items.FlagItem;
import de.unknowncity.plots.gui.items.NextPageItem;
import de.unknowncity.plots.gui.items.PrevPageItem;
import de.unknowncity.plots.plot.flag.PlotFlag;
import de.unknowncity.plots.plot.flag.PlotFlags;
import de.unknowncity.plots.service.PlotService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
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


        List<Item> playerFlags = plotService.flagRegistry().flagCategories().get(PlotFlag.Category.PLAYER).stream().map(plotFlag -> {
            return new FlagItem<>(player, plotFlag, plot, plugin);
        }).collect(Collectors.toList());

        List<Item> entityFlags = plotService.flagRegistry().flagCategories().get(PlotFlag.Category.PLAYER).stream().map(plotFlag -> {
            return new FlagItem<>(player, plotFlag, plot, plugin);
        }).collect(Collectors.toList());

        List<Item> vehicleFlags = plotService.flagRegistry().flagCategories().get(PlotFlag.Category.PLAYER).stream().map(plotFlag -> {
            return new FlagItem<>(player, plotFlag, plot, plugin);
        }).collect(Collectors.toList());

        List<Item> blockFlags = plotService.flagRegistry().flagCategories().get(PlotFlag.Category.BLOCK).stream().map(plotFlag -> {
            return new FlagItem<>(player, plotFlag, plot, plugin);
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

        guis.add(PagedGui.ofItems(innerStructure, playerFlags));
        guis.add(PagedGui.ofItems(innerStructure, entityFlags));
        guis.add(PagedGui.ofItems(innerStructure, vehicleFlags));
        guis.add(PagedGui.ofItems(innerStructure, blockFlags));

        var playerTab = new DefaultTabItem(0,
                ItemBuilder.of(Material.PLAYER_HEAD).name(
                                messenger.component(player, NodePath.path("gui", "flags", "item", "player-tab", "name"),
                                        Placeholder.parsed("flag-amount", String.valueOf(playerFlags.size())))
                        )
                        .lore(
                                messenger.componentList(player, NodePath.path("gui", "flags", "item", "player-tab", "lore"),
                                        Placeholder.parsed("flag-amount", String.valueOf(playerFlags.size())),
                                        Placeholder.parsed("flag-description", String.valueOf(playerFlags.size())))
                        ).item()
        );

        var entityTab = new DefaultTabItem(1,
                ItemBuilder.of(Material.ZOMBIE_SPAWN_EGG).name(
                                messenger.component(player, NodePath.path("gui", "flags", "item", "entity-tab", "name"),
                                        Placeholder.parsed("flag-amount", String.valueOf(entityFlags.size())))
                        )
                        .lore(
                                messenger.componentList(player, NodePath.path("gui", "flags", "item", "entity-tab", "lore"),
                                        Placeholder.parsed("flag-amount", String.valueOf(playerFlags.size())))
                        ).item()
        );

        var vehicleTab = new DefaultTabItem(2,
                ItemBuilder.of(Material.MINECART).name(
                                messenger.component(player, NodePath.path("gui", "flags", "item", "vehicle-tab", "name"),
                                        Placeholder.parsed("flag-amount", String.valueOf(vehicleFlags.size())))
                        )
                        .lore(
                                messenger.componentList(player, NodePath.path("gui", "flags", "item", "vehicle-tab", "lore"),
                                        Placeholder.parsed("flag-amount", String.valueOf(playerFlags.size())))
                        ).item()
        );

        var blockTab = new DefaultTabItem(3,
                ItemBuilder.of(Material.BLUE_ICE).name(
                                messenger.component(player, NodePath.path("gui", "flags", "item", "block-tab", "name"),
                                        Placeholder.parsed("flag-amount", String.valueOf(blockFlags.size())))
                        )
                        .lore(
                                messenger.componentList(player, NodePath.path("gui", "flags", "item", "block-tab", "lore"),
                                        Placeholder.parsed("flag-amount", String.valueOf(playerFlags.size())))
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


    public List<Component> flagCategoryLore(List<PlotFlag<?>> flags, PlotsPlugin plugin, Player player) {
        var lore = new ArrayList<Component>();

        flags.forEach(plotFlag -> {
            lore.add(plugin.messenger().component(player, NodePath.path("flags", "description", plotFlag.flagId())));
        });

        return lore;
    }
}