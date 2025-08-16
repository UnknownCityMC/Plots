package de.unknowncity.plots.gui.items;

import de.unknowncity.plots.plot.Plot;
import de.unknowncity.plots.plot.access.entity.BannedPlayer;
import de.unknowncity.plots.service.PlotService;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.Click;
import xyz.xenondevs.invui.item.AbstractItem;
import xyz.xenondevs.invui.item.ItemBuilder;
import xyz.xenondevs.invui.item.ItemProvider;

public class ManageBannedMember extends AbstractItem {
    public ManageBannedMember(ItemBuilder itemBuilder, PlotService plotService, Plot plot, BannedPlayer bannedPlayer) {

    }

    @Override
    public @NotNull ItemProvider getItemProvider(@NotNull Player viewer) {
        return null;
    }

    @Override
    public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull Click click) {

    }
}
