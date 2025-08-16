package de.unknowncity.plots.gui.items;

import de.unknowncity.plots.plot.Plot;
import de.unknowncity.plots.plot.access.entity.PlotPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.Click;
import xyz.xenondevs.invui.item.AbstractItem;
import xyz.xenondevs.invui.item.ItemBuilder;
import xyz.xenondevs.invui.item.ItemProvider;

public class ManageBannedMember extends AbstractItem {
    private final ItemBuilder itemBuilder;
    private final Plot plot;
    private final PlotPlayer bannedPlayer;

    public ManageBannedMember(ItemBuilder itemBuilder, Plot plot, PlotPlayer bannedPlayer) {
        this.itemBuilder = itemBuilder;
        this.plot = plot;
        this.bannedPlayer = bannedPlayer;
    }

    @Override
    public @NotNull ItemProvider getItemProvider(@NotNull Player viewer) {
        return itemBuilder;
    }

    @Override
    public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull Click click) {
        if (clickType == ClickType.SHIFT_LEFT) {
            plot.bannedPlayers().remove(bannedPlayer);
        }
    }
}
