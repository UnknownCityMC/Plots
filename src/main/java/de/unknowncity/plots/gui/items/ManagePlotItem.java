package de.unknowncity.plots.gui.items;

import de.unknowncity.astralib.paper.api.item.ItemBuilder;
import de.unknowncity.plots.PlotsPlugin;
import de.unknowncity.plots.gui.PlotMainGUI;
import de.unknowncity.plots.plot.group.PlotGroup;
import de.unknowncity.plots.plot.model.Plot;
import de.unknowncity.plots.service.PlotService;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.NodePath;
import xyz.xenondevs.invui.Click;
import xyz.xenondevs.invui.item.AbstractPagedGuiBoundItem;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.ItemWrapper;

import java.util.ArrayList;

public class ManagePlotItem extends AbstractPagedGuiBoundItem {
    private final Plot plot;
    private final PlotsPlugin plugin;

    public ManagePlotItem(PlotsPlugin plugin, Plot plot) {
        this.plugin = plugin;
        this.plot = plot;
    }

    @Override
    public @NotNull ItemProvider getItemProvider(@NotNull Player player) {
        var groupOptional = plugin.serviceRegistry().getRegistered(PlotService.class).getGroup(plot.groupName());
        var displayItem = PlotGroup.DEFAULT_DISPLAY_ITEM;
        if (groupOptional.isPresent()) {
            displayItem = groupOptional.get().displayItem();
        }

        var nameKey = NodePath.path("gui", "viewplots", "item", plot.isOwner(player.getUniqueId()) ? "plot-owned" : "plot-member", "name");
        var loreKey = NodePath.path("gui", "viewplots", "item", plot.isOwner(player.getUniqueId()) ? "plot-owned" : "plot-member", "lore");

        var lore = new ArrayList<>(plugin.messenger().componentList(player, loreKey, plot.tagResolvers(player, plugin.messenger())));
        var name = plugin.messenger().component(player, nameKey, plot.tagResolvers(player, plugin.messenger()));

        var builder = ItemBuilder.of(displayItem)
                .name(name)
                .lore(lore)
                .item();

        return new ItemWrapper(builder);
    }

    @Override
    public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull Click click) {
        if (clickType == ClickType.LEFT) {
            player.teleport(plot.plotHome().getLocation(plot.world()));
            plugin.messenger().sendMessage(player, NodePath.path("command", "plot", "plot-tp", "success"));
            return;
        }
        if (clickType == ClickType.SHIFT_LEFT) {
            if (!plot.isOwner(player.getUniqueId())) {
                return;
            }

            PlotMainGUI.open(player, plot, plugin);
        }
    }
}
