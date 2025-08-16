package de.unknowncity.plots.gui;

import de.unknowncity.astralib.paper.api.item.ItemBuilder;
import de.unknowncity.plots.PlotsPlugin;
import de.unknowncity.plots.gui.items.DefaultTabItem;
import de.unknowncity.plots.gui.items.FlagItem;
import de.unknowncity.plots.gui.items.NextPageItem;
import de.unknowncity.plots.gui.items.PrevPageItem;
import de.unknowncity.plots.plot.Plot;
import de.unknowncity.plots.plot.flag.PlotFlag;
import de.unknowncity.plots.service.PlotService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.spongepowered.configurate.NodePath;
import xyz.xenondevs.invui.gui.*;
import xyz.xenondevs.invui.item.AbstractItem;
import xyz.xenondevs.invui.item.Item;
import xyz.xenondevs.invui.window.Window;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class FlagsGUI {

    public static void open(Player player, Plot plot, PlotsPlugin plugin) {
        var messenger = plugin.messenger();
        var plotService = plugin.serviceRegistry().getRegistered(PlotService.class);

        var title = messenger.component(player, NodePath.path("gui", "flags", "title"));

        var backItem = Item.builder().setItemProvider(ItemBuilder.of(Material.BARRIER).name(
                        messenger.component(player, NodePath.path("gui", "flags", "item", "back", "name"))
                ).item()).addClickHandler(click -> PlotMainGUI.open(player, plot, plugin))
                .build();

        var flagCategories = plotService.flagRegistry().flagCategories();

        var playerFlags = flagCategories.get(PlotFlag.Category.PLAYER);
        var playerFLagItems = playerFlags.stream()
                .map(plotFlag -> new FlagItem<>(player, plotFlag, plot, plugin))
                .toList();

        var entityFlags = flagCategories.get(PlotFlag.Category.ENTITY);
        var entityFlagItems = entityFlags.stream()
                .map(plotFlag -> new FlagItem<>(player, plotFlag, plot, plugin))
                .toList();

        var vehicleFlags = flagCategories.get(PlotFlag.Category.VEHICLE);
        var vehicleFlagItems = vehicleFlags.stream()
                .map(plotFlag -> new FlagItem<>(player, plotFlag, plot, plugin))
                .toList();

        var blockFlags = flagCategories.get(PlotFlag.Category.BLOCK);
        var blockFlagItems = blockFlags.stream()
                .map(plotFlag -> new FlagItem<>(player, plotFlag, plot, plugin))
                .toList();

        var guis = new ArrayList<Gui>();
        var innerStructure = new Structure(
                ". . . . . .",
                ". . . . . .",
                ". . . . . .",
                ". . . . . .",
                "# < # # > #"
        )
                .addIngredient('<', new PrevPageItem(ItemStack.of(Material.PAPER), messenger))
                .addIngredient('>', new NextPageItem(ItemStack.of(Material.PAPER), messenger))
                .addIngredient('#', PlotMainGUI.BORDER_ITEM)
                .addIngredient('.', Markers.CONTENT_LIST_SLOT_HORIZONTAL);

        guis.add(PagedGui.ofItems(innerStructure, playerFLagItems));
        guis.add(PagedGui.ofItems(innerStructure, entityFlagItems));
        guis.add(PagedGui.ofItems(innerStructure, vehicleFlagItems));
        guis.add(PagedGui.ofItems(innerStructure, blockFlagItems));

        // Player Flag Tab Item
        var playerTabLore = new ArrayList<Component>();
        playerTabLore.addAll(messenger.componentList(player, NodePath.path("gui", "flags", "item", "player-tab", "lore"),
                Placeholder.parsed("flag-amount", String.valueOf(playerFlags.size())))
        );
        playerTabLore.addAll(flagCategoryLore(playerFlags, plugin, player));
        var playerTab = new DefaultTabItem(0, ItemBuilder.of(Material.PLAYER_HEAD).name(
                messenger.component(player, NodePath.path("gui", "flags", "item", "player-tab", "name"),
                        Placeholder.parsed("flag-amount", String.valueOf(playerFlags.size())))
        ).lore(playerTabLore).item());

        // Entity Flag Tab Item
        var entityTabLore = new ArrayList<Component>();
        entityTabLore.addAll(messenger.componentList(player, NodePath.path("gui", "flags", "item", "entity-tab", "lore"),
                Placeholder.parsed("flag-amount", String.valueOf(playerFlags.size())))
        );
        entityTabLore.addAll(flagCategoryLore(entityFlags, plugin, player));
        var entityTab = new DefaultTabItem(1, ItemBuilder.of(Material.ZOMBIE_SPAWN_EGG).name(
                messenger.component(player, NodePath.path("gui", "flags", "item", "entity-tab", "name"),
                        Placeholder.parsed("flag-amount", String.valueOf(entityFlags.size())))
        ).lore(entityTabLore).item());

        // Vehicle Flag Tab Item
        var vehicleTabLore = new ArrayList<Component>();
        vehicleTabLore.addAll(messenger.componentList(player, NodePath.path("gui", "flags", "item", "vehicle-tab", "lore"),
                Placeholder.parsed("flag-amount", String.valueOf(playerFlags.size())))
        );
        vehicleTabLore.addAll(flagCategoryLore(vehicleFlags, plugin, player));
        var vehicleTab = new DefaultTabItem(2, ItemBuilder.of(Material.MINECART).name(
                messenger.component(player, NodePath.path("gui", "flags", "item", "vehicle-tab", "name"),
                        Placeholder.parsed("flag-amount", String.valueOf(vehicleFlags.size())))
        ).lore(vehicleTabLore).item());

        // Block Flag Tab Item
        var blockTabLore = new ArrayList<Component>();
        blockTabLore.addAll(messenger.componentList(player, NodePath.path("gui", "flags", "item", "block-tab", "lore"),
                Placeholder.parsed("flag-amount", String.valueOf(blockFlags.size())))
        );
        blockTabLore.addAll(flagCategoryLore(blockFlags, plugin, player));
        var blockTab = new DefaultTabItem(3, ItemBuilder.of(Material.BLUE_ICE).name(
                messenger.component(player, NodePath.path("gui", "flags", "item", "block-tab", "name"),
                        Placeholder.parsed("flag-amount", String.valueOf(blockFlags.size())))
        ).lore(blockTabLore).item());

        var tabbedGUI = TabGui.builder()
                .setStructure(new Structure(
                        "# # # # # # # # #",
                        "P # . . . . . . #",
                        "E # . . . . . . #",
                        "V # . . . . . . #",
                        "B # . . . . . . #",
                        "# # . . . . . . X")
                        .addIngredient('#', PlotMainGUI.BORDER_ITEM)
                        .addIngredient('X', backItem)
                        .addIngredient('P', playerTab)
                        .addIngredient('E', entityTab)
                        .addIngredient('V', vehicleTab)
                        .addIngredient('B', blockTab)
                        .addIngredient('.', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
                ).setTabs(guis)
                .build();


        Window.builder()
                .setUpperGui(tabbedGUI)
                .setTitle(title)
                .addCloseHandler((reason) -> plotService.savePlot(plot))
                .open(player);
    }


    private static List<Component> flagCategoryLore(List<PlotFlag<?>> flags, PlotsPlugin plugin, Player player) {
        var lore = new ArrayList<Component>();

        flags.forEach(plotFlag -> {
            var flagInfo = plugin.messenger().component(player, NodePath.path("flags", "info", plotFlag.flagId()));
            var flagName = plugin.messenger().component(player, NodePath.path("flags", "name", plotFlag.flagId()));
            var flagDescription = plugin.messenger().component(player, NodePath.path("flags", "description", plotFlag.flagId()));

            var loreLine = plugin.messenger().component(player, NodePath.path("gui", "flags", "item", "tab", "lore", "slot"),
                    Placeholder.component("flag-info", flagInfo),
                    Placeholder.component("flag-description", flagDescription),
                    Placeholder.component("flag-name", flagName)
            );

            lore.add(loreLine);
        });

        return lore;
    }
}