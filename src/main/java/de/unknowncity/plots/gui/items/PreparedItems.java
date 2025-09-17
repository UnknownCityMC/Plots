package de.unknowncity.plots.gui.items;

import de.unknowncity.astralib.paper.api.item.ItemBuilder;
import de.unknowncity.plots.PlotsPlugin;
import de.unknowncity.plots.gui.*;
import de.unknowncity.plots.plot.model.Plot;
import de.unknowncity.plots.util.SkullHelper;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.spongepowered.configurate.NodePath;
import xyz.xenondevs.invui.item.Item;

public class PreparedItems {
    private static final ItemStack PLOT_INFO_SKULL = SkullHelper.getSkull("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjU0ODUwMzFiMzdmMGQ4YTRmM2I3ODE2ZWI3MTdmMDNkZTg5YTg3ZjZhNDA2MDJhZWY1MjIyMWNkZmFmNzQ4OCJ9fX0=");
    private static final ItemStack WARP_SKULL = SkullHelper.getSkull("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjBiZmMyNTc3ZjZlMjZjNmM2ZjczNjVjMmM0MDc2YmNjZWU2NTMxMjQ5ODkzODJjZTkzYmNhNGZjOWUzOWIifX19");

    public static Item back(Player player, PlotsPlugin plugin, Runnable action) {
        var name = plugin.messenger().component(player, NodePath.path("gui", "item", "back", "name"));
        var lore = plugin.messenger().componentList(player, NodePath.path("gui", "item", "back", "lore"));

        var item = ItemBuilder.of(Material.WITHER_SKELETON_SKULL)
                .name(name)
                .lore(lore)
                .itemModel(NamespacedKey.fromString(plugin.configuration().gui().buttonModelReturn()))
                .item();

        return Item.builder().setItemProvider(item).addClickHandler(click -> {
            player.playSound(player.getLocation(), "ui.button.click", 1, 1);
            action.run();
        }).build();
    }

    public static Item plotInfo(Player player, Plot plot, PlotsPlugin plugin) {
        var name = plugin.messenger().component(player, NodePath.path("gui", "main", "item", "info", "name"),
                plot.tagResolvers(player, plugin.messenger()));
        var lore = plugin.messenger().componentList(player, NodePath.path("gui", "main", "item", "info", "lore"),
                plot.tagResolvers(player, plugin.messenger()));

        var item = ItemBuilder.of(Material.WITHER_SKELETON_SKULL)
                .name(name)
                .lore(lore)
                .item();

        return Item.simple(item);
    }

    public static Item warp(Player player, Plot plot, PlotsPlugin plugin) {
        var name = plugin.messenger().component(player, NodePath.path("gui", "main", "item", "warp", "name"));
        var lore = plugin.messenger().componentList(player, NodePath.path("gui", "main", "item", "warp", "lore"));

        var item = ItemBuilder.of(WARP_SKULL)
                .name(name)
                .lore(lore)
                .item();


        return Item.builder().setItemProvider(item).addClickHandler(click -> {
            // WarpGui.open(player, plot, plugin);
            player.playSound(player.getLocation(), "ui.button.click", 1, 1);
        }).build();
    }


    public static Item biome(Player player, Plot plot, PlotsPlugin plugin) {
        var name = plugin.messenger().component(player, NodePath.path("gui", "main", "item", "biome", "name"));
        var lore = plugin.messenger().componentList(player, NodePath.path("gui", "main", "item", "biome", "lore"), Placeholder.component(
                "current-biome", Component.translatable(plot.biome().translationKey())
        ));

        var item = ItemBuilder.of(Material.CHERRY_SAPLING)
                .name(name)
                .lore(lore)
                .item();


        return Item.builder().setItemProvider(item).addClickHandler(click -> {
            BiomeChangeGUI.open(player, plot, plugin);
            player.playSound(player.getLocation(), "ui.button.click", 1, 1);
        }).build();
    }

    public static Item members(Player player, Plot plot, PlotsPlugin plugin) {
        var name = plugin.messenger().component(player, NodePath.path("gui", "main", "item", "members", "name"),
                plot.tagResolvers(player, plugin.messenger()));
        var lore = plugin.messenger().componentList(player, NodePath.path("gui", "main", "item", "members", "lore"),
                plot.tagResolvers(player, plugin.messenger()));

        var item = ItemBuilder.of(Material.SKELETON_SKULL)
                .name(name)
                .lore(lore)
                .item();


        return Item.builder().setItemProvider(item).addClickHandler(click -> {
            MembersGUI.open(player, plot, plugin);
            player.playSound(player.getLocation(), "ui.button.click", 1, 1);
        }).build();
    }

    public static Item bannedPlayers(Player player, Plot plot, PlotsPlugin plugin) {
        var name = plugin.messenger().component(player, NodePath.path("gui", "main", "item", "banned", "name"));
        var lore = plugin.messenger().componentList(player, NodePath.path("gui", "main", "item", "banned", "lore"));

        var item = ItemBuilder.of(Material.WITHER_SKELETON_SKULL)
                .name(name)
                .lore(lore)
                .item();


        return Item.builder().setItemProvider(item).addClickHandler(click -> {
            BannedPlayersGUI.open(player, plot, plugin);
            player.playSound(player.getLocation(), "ui.button.click", 1, 1);
        }).build();
    }

    public static Item flags(Player player, Plot plot, PlotsPlugin plugin) {
        var name = plugin.messenger().component(player, NodePath.path("gui", "main", "item", "flags", "name"));
        var lore = plugin.messenger().componentList(player, NodePath.path("gui", "main", "item", "flags", "lore"));

        var item = ItemBuilder.of(Material.WRITTEN_BOOK)
                .name(name)
                .lore(lore)
                .item();


        return Item.builder().setItemProvider(item).addClickHandler(click -> {
            FlagsGUI.open(player, plot, plugin);
            player.playSound(player.getLocation(), "ui.button.click", 1, 1);
        }).build();
    }

    public static Item interactables(Player player, Plot plot, PlotsPlugin plugin) {
        var name = plugin.messenger().component(player, NodePath.path("gui", "main", "item", "interactable", "name"));
        var lore = plugin.messenger().componentList(player, NodePath.path("gui", "main", "item", "interactable", "lore"));

        var item = ItemBuilder.of(Material.SMITHING_TABLE)
                .name(name)
                .lore(lore)
                .item();


        return Item.builder().setItemProvider(item).addClickHandler(click -> {
            InteractablesGUI.open(player, plot, plugin);
            player.playSound(player.getLocation(), "ui.button.click", 1, 1);
        }).build();
    }

    public static Item addMember(Player player, Plot plot, PlotsPlugin plugin) {
        var name = plugin.messenger().component(player, NodePath.path("gui", "members", "item", "add", "name"));
        var lore = plugin.messenger().componentList(player, NodePath.path("gui", "members", "item", "add", "lore"));

        var item = ItemBuilder.of(Material.ENDER_EYE)
                .name(name)
                .lore(lore)
                .itemModel(NamespacedKey.fromString(plugin.configuration().gui().buttonModelPlus()))
                .item();


        return Item.builder().setItemProvider(item).addClickHandler(click -> {
            AddPlayerGui.open(player, plot, plugin, AddPlayerGui.AddPlayerGuiType.MEMBER);
            player.playSound(player.getLocation(), "ui.button.click", 1, 1);
        }).build();
    }

    public static Item addBannedPlayer(Player player, Plot plot, PlotsPlugin plugin) {
        var name = plugin.messenger().component(player, NodePath.path("gui", "banned-players", "item", "add", "name"));
        var lore = plugin.messenger().componentList(player, NodePath.path("gui", "banned-players", "item", "add", "lore"));

        var item = ItemBuilder.of(Material.ENDER_EYE)
                .name(name)
                .lore(lore)
                .itemModel(NamespacedKey.fromString(plugin.configuration().gui().buttonModelPlus()))
                .item();


        return Item.builder().setItemProvider(item).addClickHandler(click -> {
            AddPlayerGui.open(player, plot, plugin, AddPlayerGui.AddPlayerGuiType.BANNED_PLAYER);
            player.playSound(player.getLocation(), "ui.button.click", 1, 1);
        }).build();
    }
}
