package de.unknowncity.plots.gui.items;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import de.unknowncity.plots.PlotsPlugin;
import de.unknowncity.plots.plot.Plot;
import de.unknowncity.plots.service.PlotService;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.impl.AbstractItem;

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
    public ItemProvider getItemProvider() {
        return itemProvider;
    }

    @Override
    public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent event) {
        plotService.setBiome(plot, BukkitAdapter.adapt(biome));
    }
}
