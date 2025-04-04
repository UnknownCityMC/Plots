package de.unknowncity.plots.gui.items;

import de.unknowncity.astralib.paper.api.item.ItemBuilder;
import de.unknowncity.plots.PlotsPlugin;
import de.unknowncity.plots.plot.access.PlotAccessModifier;
import de.unknowncity.plots.plot.flag.PlotInteractable;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.NodePath;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.impl.AbstractItem;

import java.util.ArrayList;
import java.util.Arrays;

public class InteractablesItem extends AbstractItem {
    private final Player player;
    private final PlotsPlugin plugin;
    private final PlotInteractable interactable;

    public InteractablesItem(Player player, PlotInteractable plotInteractable, PlotsPlugin plugin) {
        this.player = player;
        this.interactable = plotInteractable;
        this.plugin = plugin;
    }

    @Override
    public ItemProvider getItemProvider() {
        var itemBuilder = ItemBuilder.of(interactable.blockType());
        itemBuilder.name(plugin.messenger().component(player, NodePath.path("gui", "interactables", "item", "slot", "name"),
                Placeholder.component("item-name", Component.translatable(interactable.blockType().translationKey())))
        );
        var lore = new ArrayList<>(plugin.messenger().componentList(player, NodePath.path("gui", "interactables", "item", "slot", "lore")));


        lore.addAll(Arrays.stream(PlotAccessModifier.values()).map(accessModifier -> {
            var accessModifierName = plugin.messenger().component(player, NodePath.path("gui", "interactables", "item", "slot", "access-modifier", accessModifier.name()));

            if (interactable.accessModifier() == accessModifier) {
                return plugin.messenger().component(player, NodePath.path("gui", "access-modifier-format", "active"),
                        Placeholder.component("access-modifier", accessModifierName));
            } else {
                return plugin.messenger().component(player, NodePath.path("gui", "access-modifier-format", "inactive"),
                        Placeholder.component("access-modifier", accessModifierName));
            }
        }).toList());
        itemBuilder.lore(lore);
        return new xyz.xenondevs.invui.item.builder.ItemBuilder(itemBuilder.item());
    }

    @Override
    public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent event) {
       if (clickType == ClickType.LEFT) {
           interactable.accessModifier(getNextInteractable(interactable.accessModifier(), false));
       }
       if (clickType == ClickType.RIGHT) {
           interactable.accessModifier(getNextInteractable(interactable.accessModifier(), true));
       }
       notifyWindows();
    }


    private PlotAccessModifier getNextInteractable(PlotAccessModifier accessModifier, boolean reverse) {
        var values = PlotAccessModifier.values();
        int index = reverse ? ((accessModifier.ordinal() == 0) ? values.length - 1 : accessModifier.ordinal() - 1) :
                ((accessModifier.ordinal() == values.length - 1) ? 0 : accessModifier.ordinal() + 1);
        return values[index];
    }
}
