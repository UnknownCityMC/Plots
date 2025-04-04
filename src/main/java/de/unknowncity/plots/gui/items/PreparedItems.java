package de.unknowncity.plots.gui.items;

import de.unknowncity.astralib.paper.api.item.ItemBuilder;
import de.unknowncity.plots.PlotsPlugin;
import de.unknowncity.plots.plot.Plot;
import de.unknowncity.plots.gui.*;
import de.unknowncity.plots.util.SkullHelper;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.spongepowered.configurate.NodePath;
import xyz.xenondevs.invui.item.Item;
import xyz.xenondevs.invui.item.impl.SimpleItem;

public class PreparedItems {
    private static final ItemStack PLOT_INFO_SKULL = SkullHelper.getSkull("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjU0ODUwMzFiMzdmMGQ4YTRmM2I3ODE2ZWI3MTdmMDNkZTg5YTg3ZjZhNDA2MDJhZWY1MjIyMWNkZmFmNzQ4OCJ9fX0=");
    private static final ItemStack WARP_SKULL = SkullHelper.getSkull("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjBiZmMyNTc3ZjZlMjZjNmM2ZjczNjVjMmM0MDc2YmNjZWU2NTMxMjQ5ODkzODJjZTkzYmNhNGZjOWUzOWIifX19");

    public static Item plotInfo(Player player, Plot plot, PlotsPlugin plugin) {
        return new SimpleItem(
                ItemBuilder.of(PLOT_INFO_SKULL)
                        .name(plugin.messenger().component(player, NodePath.path("gui", "main", "item", "plot-info", "name"), plot.tagResolvers(player, plugin.messenger())))
                        .lore(plugin.messenger().componentList(player, NodePath.path("gui", "main", "item", "plot-info", "lore"), plot.tagResolvers(player, plugin.messenger())))
                        .item()
        );
    }

    public static Item warp(Player player, Plot plot, PlotsPlugin plugin) {
        return new SimpleItem(
                ItemBuilder.of(WARP_SKULL)
                        .name(plugin.messenger().component(player, NodePath.path("gui", "main", "item", "warp", "name"), plot.tagResolvers(player, plugin.messenger())))
                        .lore(plugin.messenger().componentList(player, NodePath.path("gui", "main", "item", "warp", "lore"), plot.tagResolvers(player, plugin.messenger())))
                        .item()
        );
    }

    public static Item biome(Player player, Plot plot, PlotsPlugin plugin) {
        var currentBiome = plot.biome();
        return new SimpleItem(
                ItemBuilder.of(Material.CHERRY_SAPLING)
                        .name(plugin.messenger().component(player, NodePath.path("gui", "main", "item", "biome", "name"),
                                Placeholder.component("current-biome", Component.translatable(currentBiome.translationKey())))
                        ).lore(plugin.messenger().componentList(player, NodePath.path("gui", "main", "item", "biome", "lore"),
                                        Placeholder.component("current-biome", Component.translatable(currentBiome.translationKey()))
                                )
                        ).item(), click -> BiomeChangeGUI.open(player, plot, plugin)
        );
    }

    public static Item friends(Player player, Plot plot, PlotsPlugin plugin) {
        return new SimpleItem(
                ItemBuilder.of(Material.SKELETON_SKULL).name(
                        plugin.messenger().component(player, NodePath.path("gui", "main", "item", "members", "name"))
                ).item(), click -> MembersGUI.open(player, plot, plugin)
        );
    }

    public static Item bannedPlayers(Player player, Plot plot, PlotsPlugin plugin) {
        return new SimpleItem(
                ItemBuilder.of(Material.WITHER_SKELETON_SKULL).name(
                        plugin.messenger().component(player, NodePath.path("gui", "main", "item", "banned", "name"))
                ).item(), click -> BannedPlayersGUI.open(player, plot, plugin));
    }

    public static Item flags(Player player, Plot plot, PlotsPlugin plugin) {

        return new SimpleItem(
                ItemBuilder.of(Material.WRITABLE_BOOK).name(
                        plugin.messenger().component(player, NodePath.path("gui", "main", "item", "flags", "name"))
                ).item(), click -> FlagsGUI.open(player, plot, plugin));
    }

    public static Item interactables(Player player, Plot plot, PlotsPlugin plugin) {
        return new SimpleItem(
                ItemBuilder.of(Material.SMITHING_TABLE).name(
                        plugin.messenger().component(player, NodePath.path("gui", "main", "item", "interactable", "name"))
                ).item(), click -> InteractablesGUI.open(player, plot, plugin)
        );
    }
}
