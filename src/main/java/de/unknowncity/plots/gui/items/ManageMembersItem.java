package de.unknowncity.plots.gui.items;

import de.unknowncity.astralib.paper.api.item.ItemBuilder;
import de.unknowncity.astralib.paper.api.message.PaperMessenger;
import de.unknowncity.plots.plot.model.Plot;
import de.unknowncity.plots.plot.model.PlotMember;
import de.unknowncity.plots.plot.access.type.PlotMemberRole;
import de.unknowncity.plots.util.SkullHelper;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.NodePath;
import xyz.xenondevs.invui.Click;
import xyz.xenondevs.invui.gui.PagedGui;
import xyz.xenondevs.invui.item.AbstractPagedGuiBoundItem;
import xyz.xenondevs.invui.item.Item;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.ItemWrapper;

import java.util.ArrayList;
import java.util.Arrays;

public class ManageMembersItem extends AbstractPagedGuiBoundItem {
    private final Plot plot;
    private final PlotMember member;
    private final PaperMessenger messenger;

    public ManageMembersItem(Plot plot, PlotMember member, PaperMessenger messenger) {
        this.plot = plot;
        this.member = member;
        this.messenger = messenger;
    }

    @Override
    public @NotNull ItemProvider getItemProvider(@NotNull Player player) {
        var skull = SkullHelper.getSkull(member.uuid());

        var lore = new ArrayList<>(messenger.componentList(player, NodePath.path("gui", "members", "item", "member", "lore"),
                member.tagResolversWithRole(player, messenger)));

        var name = messenger.component(player, NodePath.path("gui", "members", "item", "member", "name"),
                member.tagResolversWithRole(player, messenger));

        lore.addAll(Arrays.stream(PlotMemberRole.values()).map(memberRole -> {
            var accessModifierName = messenger.component(player, NodePath.path("member-role", "name", memberRole.name()));

            if (member.role() == memberRole) {
                return messenger.component(player, NodePath.path("gui", "members", "format", "active"),
                        Placeholder.component("role", accessModifierName));
            } else {
                return messenger.component(player, NodePath.path("gui", "members", "format", "inactive"),
                        Placeholder.component("role", accessModifierName));
            }
        }).toList());

        var builder = ItemBuilder.of(skull)
                .name(name)
                .lore(lore)
                .item();

        return new ItemWrapper(builder);
    }

    @Override
    public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull Click click) {
        if (clickType == ClickType.SHIFT_LEFT) {
            plot.members().removeIf(plotMember -> plotMember.uuid().equals(member.uuid()));
            var gui = (PagedGui<Item>) getGui();
            gui.setContent(gui.getContent().stream().filter(item -> !item.equals(this)).toList());
            player.playSound(player.getLocation(), "entity.item.break", 1, 1);

            notifyWindows();
            return;
        }

        var role = member.role();

        if (clickType == ClickType.RIGHT) {
            role = getPreviousRole(role);
        } else if (clickType == ClickType.LEFT) {
            role = getNextRole(role);
        }

        player.playSound(player.getLocation(), "ui.button.click", 1, 1);

        member.role(role);

        plot.changeMemberRole(member.uuid(), role);

        notifyWindows();
    }

    public PlotMemberRole getNextRole(PlotMemberRole role) {
        var values = PlotMemberRole.values();
        int index = role.ordinal() == values.length - 1 ? 0 : role.ordinal() + 1;
        return values[index];
    }

    public PlotMemberRole getPreviousRole(PlotMemberRole role) {
        var values = PlotMemberRole.values();
        int index = role.ordinal() == 0 ? values.length - 1 : role.ordinal() - 1;
        return values[index];
    }
}
