package de.unknowncity.plots.gui.items;

import de.unknowncity.plots.plot.Plot;
import de.unknowncity.plots.plot.access.entity.PlotMember;
import de.unknowncity.plots.service.PlotService;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.Click;
import xyz.xenondevs.invui.item.AbstractItem;
import xyz.xenondevs.invui.item.ItemProvider;

public class ManageMembersItem extends AbstractItem {
    private final PlotService plotService;
    private final Plot plot;
    private final ItemProvider itemProvider;
    private final PlotMember member;

    public ManageMembersItem(@NotNull ItemProvider itemProvider, PlotService plotService, Plot plot, PlotMember member) {
        this.itemProvider = itemProvider;
        this.plotService = plotService;
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
            plotService.removeMember(Bukkit.getOfflinePlayer(member.memberID()), plot);
        }
    }
}
