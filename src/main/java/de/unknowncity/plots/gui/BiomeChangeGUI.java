package de.unknowncity.plots.gui;

import de.unknowncity.astralib.paper.api.item.ItemBuilder;
import de.unknowncity.plots.PlotsPlugin;
import de.unknowncity.plots.gui.items.BiomeChangeItem;
import de.unknowncity.plots.gui.items.NextPageItem;
import de.unknowncity.plots.gui.items.PreparedItems;
import de.unknowncity.plots.gui.items.PrevPageItem;
import de.unknowncity.plots.plot.model.Plot;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Material;
import org.bukkit.Registry;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.NodePath;
import xyz.xenondevs.invui.gui.Markers;
import xyz.xenondevs.invui.gui.PagedGui;
import xyz.xenondevs.invui.gui.Structure;
import xyz.xenondevs.invui.item.Item;
import xyz.xenondevs.invui.window.Window;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static io.papermc.paper.registry.keys.BiomeKeys.*;

public class BiomeChangeGUI {

    final static Registry<@NotNull Biome> biomeRegistry = RegistryAccess
            .registryAccess()
            .getRegistry(RegistryKey.BIOME);

    private static final Map<Biome, Material> biomeBlockMap = new HashMap<>();


    static {
        // Overworld Biomes
        biomeBlockMap.put(biomeRegistry.get(PLAINS), Material.GRASS_BLOCK);
        biomeBlockMap.put(biomeRegistry.get(FOREST), Material.OAK_LOG);
        biomeBlockMap.put(biomeRegistry.get(JUNGLE), Material.JUNGLE_LOG);
        biomeBlockMap.put(biomeRegistry.get(SAVANNA), Material.ACACIA_LOG);
        biomeBlockMap.put(biomeRegistry.get(DESERT), Material.SAND);
        biomeBlockMap.put(biomeRegistry.get(SNOWY_PLAINS), Material.SNOW_BLOCK);
        biomeBlockMap.put(biomeRegistry.get(MUSHROOM_FIELDS), Material.MYCELIUM);
        biomeBlockMap.put(biomeRegistry.get(SWAMP), Material.SLIME_BLOCK);
        biomeBlockMap.put(biomeRegistry.get(MEADOW), Material.TERRACOTTA);

        // Ocean Biomes
        biomeBlockMap.put(biomeRegistry.get(OCEAN), Material.WATER_BUCKET);
        biomeBlockMap.put(biomeRegistry.get(COLD_OCEAN), Material.BLUE_ICE);
        biomeBlockMap.put(biomeRegistry.get(FROZEN_OCEAN), Material.ICE);
        biomeBlockMap.put(biomeRegistry.get(WARM_OCEAN), Material.BRAIN_CORAL_BLOCK);

        // Nether Biomes
        biomeBlockMap.put(biomeRegistry.get(NETHER_WASTES), Material.NETHERRACK);

        // The End
        biomeBlockMap.put(biomeRegistry.get(THE_END), Material.END_STONE);
    }

    public static void open(Player player, Plot plot, PlotsPlugin plugin) {
        var messenger = plugin.messenger();

        var title = messenger.component(player, NodePath.path("gui", "biome", "title"));


        var items = biomeBlockMap.keySet().stream().map(biome -> new BiomeChangeItem(Item.simple(ItemBuilder.of(getItemForBiome(biome)).name(
                messenger.component(player, NodePath.path("gui", "biome", "item", "biome", "name"), Placeholder.component("biome", Component.translatable(biome.translationKey())))
        ).item()).getItemProvider(player), plot, biome, plugin)).collect(Collectors.toList());

        var gui = PagedGui.ofItems(
                new Structure(
                        "# # # # # # # # #",
                        "# . . . . . . . #",
                        "# . . . . . . . #",
                        "# . . . . . . . #",
                        "# . . . . . . . #",
                        "# # < # # # > # B"
                )
                        .addIngredient('B', PreparedItems.back(player, plugin, () -> PlotMainGUI.open(player, plot, plugin)))
                        .addIngredient('<', new PrevPageItem(ItemStack.of(Material.PAPER), messenger, plugin.configuration().gui()))
                        .addIngredient('>', new NextPageItem(ItemStack.of(Material.PAPER), messenger, plugin.configuration().gui()))
                        .addIngredient('.', Markers.CONTENT_LIST_SLOT_HORIZONTAL),
                items
        );

        var window = Window.builder().setUpperGui(gui).setTitle(title).build(player);
        window.open();
    }

    public static Material getItemForBiome(Biome biome) {
        return biomeBlockMap.getOrDefault(biome, Material.BARRIER);
    }
}
