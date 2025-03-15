package de.unknowncity.plots.gui.items;

import de.unknowncity.astralib.paper.api.item.ItemBuilder;
import de.unknowncity.plots.PlotsPlugin;
import de.unknowncity.plots.data.model.plot.Plot;
import de.unknowncity.plots.gui.BiomeChangeGUI;
import de.unknowncity.plots.gui.FriendsGUI;
import de.unknowncity.plots.gui.InteractablesGUI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.spongepowered.configurate.NodePath;
import xyz.xenondevs.invui.item.Item;
import xyz.xenondevs.invui.item.impl.SimpleItem;

public class PreparedItems {

    public static Item biome(Player player, Plot plot, PlotsPlugin plugin) {
        var currentBiome = plot.biome();
        return new SimpleItem(ItemBuilder.of(Material.GRASS_BLOCK)
                .name(plugin.messenger().component(player, NodePath.path("gui", "main", "item", "biome", "name"),
                        Placeholder.component("current-biome", Component.translatable(currentBiome.translationKey())))
                ).lore(plugin.messenger().componentList(player, NodePath.path("gui", "main", "item", "biome", "lore"),
                                Placeholder.component("current-biome", Component.translatable(currentBiome.translationKey()))
                        )
                ).item(), click -> BiomeChangeGUI.open(player, plot, plugin));
    }

    public static Item settingsFriends(Player player, Plot plot, PlotsPlugin plugin) {
        return new SimpleItem(ItemBuilder.of(Material.PLAYER_HEAD).name(
                plugin.messenger().component(player, NodePath.path("gui", "main", "item", "members", "name"))
        ).item(), click -> FriendsGUI.open(player, plot, plugin));
    }

    public static Item settingsTempMembers(Player player, Plot plot, PlotsPlugin plugin) {
        return new SimpleItem(ItemBuilder.of(Material.CREEPER_HEAD).name(
                plugin.messenger().component(player, NodePath.path("gui", "main", "item", "tempmembers", "name"))
        ).item(), click -> {
        });
    }

    public static Item settingsBannedPlayers(Player player, Plot plot, PlotsPlugin plugin) {
        return new SimpleItem(ItemBuilder.of(Material.MACE).name(
                plugin.messenger().component(player, NodePath.path("gui", "main", "item", "banned", "name"))
        ).item(), click -> {
        });
    }

    public static Item flags(Player player, Plot plot, PlotsPlugin plugin) {

        return new SimpleItem(ItemBuilder.of(Material.WRITABLE_BOOK).name(
                plugin.messenger().component(player, NodePath.path("gui", "main", "item", "flags", "name"))
        ).item(), click -> {
        });
    }

    public static Item interactables(Player player, Plot plot, PlotsPlugin plugin) {
        return new SimpleItem(ItemBuilder.of(Material.CRAFTING_TABLE).name(
                plugin.messenger().component(player, NodePath.path("gui", "main", "item", "interactable", "name"))
        ).item(), click -> InteractablesGUI.open(player, plot, plugin));
    }
}
