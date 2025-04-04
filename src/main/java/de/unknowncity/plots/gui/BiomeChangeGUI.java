package de.unknowncity.plots.gui;

import de.unknowncity.astralib.paper.api.item.ItemBuilder;
import de.unknowncity.plots.PlotsPlugin;
import de.unknowncity.plots.plot.Plot;
import de.unknowncity.plots.gui.items.BiomeChangeItem;
import de.unknowncity.plots.gui.util.PagedGUI;
import de.unknowncity.plots.service.PlotService;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;
import org.spongepowered.configurate.NodePath;
import xyz.xenondevs.invui.item.Item;
import xyz.xenondevs.invui.item.impl.SimpleItem;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class BiomeChangeGUI {

    public static void open(Player player, Plot plot, PlotsPlugin plugin) {
        var messenger = plugin.messenger();

        var title = messenger.component(player, NodePath.path("gui", "biome", "title"));

        var backItem = new SimpleItem(ItemBuilder.of(Material.BARRIER).name(
                messenger.component(player, NodePath.path("gui", "biome", "item", "back", "name"))
        ).item(), click -> PlotMainGUI.open(player, plot, plugin));

        List<Item> items = Arrays.stream(Biome.values()).map(biome -> new BiomeChangeItem(new xyz.xenondevs.invui.item.builder.ItemBuilder(ItemBuilder.of(Material.GRASS_BLOCK).name(
                messenger.component(player, NodePath.path("gui", "biome", "item", "biome", "name"), Placeholder.parsed("biome", biome.name()))
        ).item()), plot, biome, plugin)).collect(Collectors.toList());

        var gui = PagedGUI.createAndOpenPagedGUI(messenger, title, backItem, items, player);
        gui.addCloseHandler(() -> plugin.serviceRegistry().getRegistered(PlotService.class).savePlot(plot));
        gui.open();
    }
}
