package de.unknowncity.plots.gui.items;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import de.unknowncity.plots.PlotsPlugin;
import de.unknowncity.plots.plot.Plot;
import de.unknowncity.plots.service.PlotService;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.Click;
import xyz.xenondevs.invui.item.AbstractItem;
import xyz.xenondevs.invui.item.ItemProvider;

public class BiomeChangeItem extends AbstractItem {
    private final PlotService plotService;
    private final Biome biome;
    private final ItemProvider itemProvider;
    private final Plot plot;

    public BiomeChangeItem(@NotNull ItemProvider itemProvider, Plot plot, Biome biome, PlotsPlugin plugin) {
        this.itemProvider = itemProvider;
        this.plot = plot;
        this.plotService = plugin.serviceRegistry().getRegistered(PlotService.class);
        this.biome = biome;
    }

    @Override
    public @NotNull ItemProvider getItemProvider(@NotNull Player viewer) {
        return itemProvider;
    }


    @Override
    public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull Click click) {
        plotService.setBiome(plot, BukkitAdapter.adapt(biome));
        player.playSound(player.getLocation(), "ui.button.click", 1, 1);
    }
}
