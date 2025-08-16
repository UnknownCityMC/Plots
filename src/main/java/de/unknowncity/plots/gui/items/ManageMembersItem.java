package de.unknowncity.plots.gui.items;

import de.unknowncity.plots.plot.Plot;
import de.unknowncity.plots.plot.access.entity.PlotMember;
import de.unknowncity.plots.plot.access.type.PlotMemberRole;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.Click;
import xyz.xenondevs.invui.item.AbstractItem;
import xyz.xenondevs.invui.item.ItemProvider;

public class ManageMembersItem extends AbstractItem {
    private final Plot plot;
    private final ItemProvider itemProvider;
    private final PlotMember member;

    public ManageMembersItem(@NotNull ItemProvider itemProvider, Plot plot, PlotMember member) {
        this.itemProvider = itemProvider;
        this.plot = plot;
        this.member = member;
    }

    @Override
    public @NotNull ItemProvider getItemProvider(@NotNull Player viewer) {
        return itemProvider;
    }

    @Override
    public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull Click click) {
        if (clickType == ClickType.SHIFT_LEFT) {
            plot.members().removeIf(plotMember -> plotMember.uuid().equals(member.uuid()));
            return;
        }

        var role = member.role();

        if (clickType == ClickType.LEFT) {
            role = getPreviousRole(role);
        } else if (clickType == ClickType.RIGHT) {
            role = getNextRole(role);
        }

        plot.changeMemberRole(member.uuid(), role);
    }
    public void changeMemberRole(PlotMember member, Plot plot, PlotMemberRole role) {
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
