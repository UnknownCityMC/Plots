package de.unknowncity.plots.gui;

import de.unknowncity.astralib.paper.api.item.ItemBuilder;
import de.unknowncity.plots.PlotsPlugin;
import de.unknowncity.plots.gui.items.PreparedItems;
import de.unknowncity.plots.plot.model.Plot;
import de.unknowncity.plots.service.PlotService;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.spongepowered.configurate.NodePath;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.gui.Structure;
import xyz.xenondevs.invui.item.Item;
import xyz.xenondevs.invui.window.Window;

import java.util.concurrent.CompletableFuture;

public class PlotMainGUI {

    public static final Item BORDER_ITEM = Item.simple(ItemBuilder.of(Material.BLACK_STAINED_GLASS_PANE).name(Component.empty()).item());
    public static final Item FILLER_ITEM = Item.simple(ItemBuilder.of(Material.GRAY_STAINED_GLASS_PANE).name(Component.empty()).item());

    public static void open(Player player, Plot plot, PlotsPlugin plugin) {
        var title = plugin.messenger().component(player, NodePath.path("gui", "main", "title"));

        var gui = Gui.of(
                new Structure(
                        "# # # # # # # # #",
                        "# . . . P . . . #",
                        "# B . . . . . W #",
                        "# . M D . I F . #",
                        "# # # # # # # # #"
                )
                        .addIngredient('P', PreparedItems.plotInfo(player, plot, plugin))
                        .addIngredient('W', PreparedItems.warp(player, plot, plugin))
                        .addIngredient('B', PreparedItems.biome(player, plot, plugin))
                        .addIngredient('M', PreparedItems.members(player, plot, plugin))
                        .addIngredient('I', PreparedItems.interactables(player, plot, plugin))
                        .addIngredient('F', PreparedItems.flags(player, plot, plugin))
                        .addIngredient('D', PreparedItems.bannedPlayers(player, plot, plugin))
        );

        var window = Window.builder().setUpperGui(gui).setTitle(title).addCloseHandler(
                reason -> CompletableFuture.runAsync(() -> plugin.serviceRegistry().getRegistered(PlotService.class).savePlot(plot))
        );
        window.open(player);
    }
}
