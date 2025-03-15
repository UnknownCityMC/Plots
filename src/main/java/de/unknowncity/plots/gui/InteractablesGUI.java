package de.unknowncity.plots.gui;

import de.unknowncity.astralib.paper.api.item.ItemBuilder;
import de.unknowncity.plots.PlotsPlugin;
import de.unknowncity.plots.data.model.plot.Plot;
import de.unknowncity.plots.data.model.plot.flag.PlotInteractable;
import de.unknowncity.plots.gui.items.InteractablesItem;
import de.unknowncity.plots.service.PlotService;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.spongepowered.configurate.NodePath;
import xyz.xenondevs.invui.item.Item;
import xyz.xenondevs.invui.item.impl.SimpleItem;

import java.util.List;
import java.util.stream.Collectors;

public class InteractablesGUI {

    public static void open(Player player, Plot plot, PlotsPlugin plugin) {
        var messenger = plugin.messenger();
        var plotService = plugin.serviceRegistry().getRegistered(PlotService.class);

        var title = messenger.component(player, NodePath.path("gui", "interactables", "title"));

        var backItem = new SimpleItem(ItemBuilder.of(Material.BARRIER).name(
                messenger.component(player, NodePath.path("gui", "interactables", "item", "back", "name"))
        ).item(), click -> PlotMainGUI.open(player, plot, plugin));

        List<Item> items = PlotInteractable.allValidTypes().stream()
                .map(material -> {
                    var plotInteractable = plot.getInteractable(material);
                    return new InteractablesItem(player, plotInteractable, plugin);
                }).collect(Collectors.toList());

        PagedGUI.createAndOpenPagedGUI(messenger, title, backItem, items, player);
    }
}