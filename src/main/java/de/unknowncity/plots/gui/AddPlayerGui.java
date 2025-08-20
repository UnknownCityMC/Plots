package de.unknowncity.plots.gui;

import de.unknowncity.astralib.paper.api.item.ItemBuilder;
import de.unknowncity.astralib.paper.api.message.PaperMessenger;
import de.unknowncity.plots.PlotsPlugin;
import de.unknowncity.plots.gui.items.PreparedItems;
import de.unknowncity.plots.plot.Plot;
import de.unknowncity.plots.plot.access.entity.PlotMember;
import de.unknowncity.plots.plot.access.type.PlotMemberRole;
import de.unknowncity.plots.service.PlotService;
import de.unknowncity.plots.util.SkullHelper;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.spongepowered.configurate.NodePath;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.gui.Structure;
import xyz.xenondevs.invui.item.Item;
import xyz.xenondevs.invui.window.AnvilWindow;

public class AddPlayerGui {
    public enum AddPlayerGuiType {
        MEMBER,
        BANNED_PLAYER;
    }
    public static void open(Player player, Plot plot, PlotsPlugin plugin, AddPlayerGuiType type) {
        var messenger = plugin.messenger();
        var plotService = plugin.serviceRegistry().getRegistered(PlotService.class);

        var title = messenger.component(player, NodePath.path("gui", type == AddPlayerGuiType.MEMBER ? "member-add" : "banned-players-add", "title"));


        var playerNotFoundItem = Item.builder().setItemProvider(ItemBuilder.of(Material.BARRIER).name(
                        messenger.component(player, NodePath.path("gui", type == AddPlayerGuiType.MEMBER ? "member-add" : "banned-players-add", "item", "not-found", "name"))
                ).lore(
                        messenger.componentList(player, NodePath.path("gui", type == AddPlayerGuiType.MEMBER ? "member-add" : "banned-players-add", "item", "not-found", "lore"))
                ).item())
                .addClickHandler(click -> {
                    player.playSound(player.getLocation(), "entity.villager.no", 1, 1);

                }).build();


        var gui = Gui.of(
                new Structure(
                        ". P B"
                )
                        .addIngredient('B', type == AddPlayerGuiType.MEMBER ? PreparedItems.back(player, plugin, () -> MembersGUI.open(player, plot, plugin)) :
                                PreparedItems.back(player, plugin, () -> BannedPlayersGUI.open(player, plot, plugin)))
                        .addIngredient('P', playerNotFoundItem)
        );


        var window = AnvilWindow.builder().setUpperGui(gui).setTitle(title).build(player);

        window.addCloseHandler((reason) -> plotService.savePlot(plot));
        window.addRenameHandler(input -> {
            if (input.isEmpty() || input.length() > 17) {
                gui.setItem(1, playerNotFoundItem);
                return;
            }

            var target = Bukkit.getOfflinePlayerIfCached(input);
            if (target == null) {
                gui.setItem(1, playerNotFoundItem);
            } else {
                gui.setItem(1, createItem(target, plot, messenger, player, plugin, type));
            }
        });
        window.open();
    }

    public static Item createItem(OfflinePlayer target, Plot plot, PaperMessenger messenger, Player player, PlotsPlugin plugin, AddPlayerGuiType type) {
        var isPresent = target.getUniqueId().equals(plot.owner().uuid()) || type == AddPlayerGuiType.MEMBER ?
                plot.findPlotMember(target.getUniqueId()).isPresent() :
                plot.findPlotBannedPlayer(target.getUniqueId()).isPresent();

        var member = new PlotMember(target.getUniqueId(), target.getName(), PlotMemberRole.MEMBER);

        return Item.builder().setItemProvider(
                ItemBuilder.of(SkullHelper.getSkull(target.getUniqueId()))
                        .name(
                                messenger.component(player, NodePath.path("gui", type == AddPlayerGuiType.MEMBER ? "member-add" : "banned-players-add", "item", "player", "name"), member.tagResolvers())
                        )
                        .lore(
                                !isPresent ? messenger.componentList(player, NodePath.path("gui", type == AddPlayerGuiType.MEMBER ? "member-add" : "banned-players-add", "item", "player", "lore"), member.tagResolvers()) :
                                        messenger.componentList(player, NodePath.path("gui", type == AddPlayerGuiType.MEMBER ? "member-add" : "banned-players-add", "item", "player", "lore-already-present"), member.tagResolvers())
                        )
                        .item()
        ).addClickHandler((item, click) -> {
            if (type == AddPlayerGuiType.MEMBER) {
                if (isPresent) {
                    messenger.sendMessage(player, NodePath.path("command", "plot", "member", "already-member"), Placeholder.parsed("name", target.getName()));
                    player.playSound(player.getLocation(), "entity.villager.no", 1, 1);
                    return;
                }
                plot.members().add(member);
                player.playSound(player.getLocation(), "entity.experience_orb.pickup", 1, 1);
                if (target instanceof Player targetPlayer) {
                    plugin.messenger().sendMessage(player, NodePath.path("event", "plot", "member", "added"), plot.tagResolvers(targetPlayer, messenger));
                }
                messenger.sendMessage(player, NodePath.path("command", "plot", "member", "success"), Placeholder.parsed("name", target.getName()));
                MembersGUI.open(player, plot, plugin);
            } else {
                if (isPresent) {
                    messenger.sendMessage(player, NodePath.path("command", "plot", "banned-player", "already-banned"), Placeholder.parsed("name", target.getName()));
                    player.playSound(player.getLocation(), "entity.villager.no", 1, 1);
                    return;
                }
                plot.denyPlayer(target);
                player.playSound(player.getLocation(), "entity.experience_orb.pickup", 1, 1);
                if (target instanceof Player targetPlayer) {
                    plugin.messenger().sendMessage(player, NodePath.path("event", "plot", "kick", "target"), plot.tagResolvers(targetPlayer, messenger));
                }
                messenger.sendMessage(player, NodePath.path("command", "plot", "member", "success"), Placeholder.parsed("name", target.getName()));
                BannedPlayersGUI.open(player, plot, plugin);
            }
        }).build();
    }
}
