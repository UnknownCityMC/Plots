package de.unknowncity.plots.gui.items;

import de.unknowncity.astralib.paper.api.item.ItemBuilder;
import de.unknowncity.plots.PlotsPlugin;
import de.unknowncity.plots.plot.model.Plot;
import de.unknowncity.plots.plot.flag.PlotFlag;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.NodePath;
import xyz.xenondevs.invui.Click;
import xyz.xenondevs.invui.item.AbstractItem;
import xyz.xenondevs.invui.item.Item;
import xyz.xenondevs.invui.item.ItemProvider;

import java.util.ArrayList;

public class FlagItem<T> extends AbstractItem {
    private final PlotFlag<T> plotFlag;
    private final PlotsPlugin plugin;
    private final Plot plot;
    private T value;

    public FlagItem(Player player, PlotFlag<T> plotFlag, Plot plot, PlotsPlugin plugin) {
        this.plotFlag = plotFlag;
        this.plugin = plugin;
        this.plot = plot;
        this.value = plot.getFlag(plotFlag);
    }

    @Override
    public @NotNull ItemProvider getItemProvider(@NotNull Player player) {
        var itemBuilder = ItemBuilder.of(plotFlag.displayMaterial());
        itemBuilder.name(plugin.messenger().component(
                player, NodePath.path("gui", "flags", "item", "slot", "name"),
                Placeholder.component("flag-name", plugin.messenger().component(player, NodePath.path("flags", "name", plotFlag.flagId())))));

        var flagDescription = plugin.messenger().component(player, NodePath.path("flags", "description", plotFlag.flagId()));

        var lore = new ArrayList<>(plugin.messenger().componentList(player, NodePath.path("gui", "flags", "item", "slot", "lore"),
                Placeholder.component("flag-description", flagDescription)
        ));

        lore.addAll(plotFlag.possibleValues().stream().map(possibleValue -> {
            var valueName = plugin.messenger().component(player, NodePath.path("flags", "value", possibleValue.toString()));

            if (plot.getFlag(plotFlag) == possibleValue) {
                return plugin.messenger().component(player, NodePath.path("gui", "flags", "format", "active"),
                        Placeholder.component("value", valueName));
            } else {
                return plugin.messenger().component(player, NodePath.path("gui", "flags", "format", "inactive"),
                        Placeholder.component("value", valueName));
            }
        }).toList());
        itemBuilder.lore(lore);
        return Item.simple(itemBuilder.item()).getItemProvider(player);
    }

    @Override
    public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull Click click) {
        if (clickType == ClickType.LEFT) {
            value = getNextValue(value, false);
            plot.setFlag(plotFlag, value);
        }
        if (clickType == ClickType.RIGHT) {
            value = getNextValue(value, true);
            plot.setFlag(plotFlag, value);
        }

        player.playSound(player.getLocation(), "ui.button.click", 1, 1);
        notifyWindows();
    }

    private T getNextValue(T value, boolean reverse) {
        var values = plotFlag.possibleValues();
        var currentIndex = values.indexOf(value);
        int index = reverse ? ((currentIndex == 0) ? values.size() - 1 : currentIndex - 1) :
                ((currentIndex == values.size() - 1) ? 0 : currentIndex + 1);
        return values.get(index);
    }
}
