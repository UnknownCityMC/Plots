package de.unknowncity.plots.plot.flag;

import de.unknowncity.astralib.common.registry.Registry;
import de.unknowncity.plots.PlotsPlugin;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FlagRegistry extends Registry<PlotsPlugin, PlotFlag<?>> {
    private final Map<PlotFlag.Category, List<PlotFlag<?>>> flagCategories = new HashMap<>();

    public FlagRegistry(PlotsPlugin plugin) {
        super(plugin);
        flagCategories.put(PlotFlag.Category.PLAYER, new ArrayList<>());
        flagCategories.put(PlotFlag.Category.ENTITY, new ArrayList<>());
        flagCategories.put(PlotFlag.Category.VEHICLE, new ArrayList<>());
        flagCategories.put(PlotFlag.Category.BLOCK, new ArrayList<>());
    }

    public PlotFlag<?> getRegistered(String id) {
        return registered.stream().filter(plotFlag -> plotFlag.flagId.equals(id)).findFirst().orElse(null);
    }

    @Override
    public <R extends PlotFlag<?>> void register(R registrable) {
        super.register(registrable);

        if (registrable instanceof Listener listener) {
            plugin.getServer().getPluginManager().registerEvents(listener, plugin);
        }
    }

    public void registerToCat(PlotFlag.Category category, PlotFlag<?> plotFlag) {
        flagCategories.get(category).add(plotFlag);
        register(plotFlag);
    }

    public Map<PlotFlag.Category, List<PlotFlag<?>>> flagCategories() {
        return flagCategories;
    }
}
