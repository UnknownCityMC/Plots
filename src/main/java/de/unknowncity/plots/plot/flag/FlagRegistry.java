package de.unknowncity.plots.plot.flag;

import de.unknowncity.astralib.common.registry.Registry;
import de.unknowncity.plots.PlotsPlugin;

public class FlagRegistry extends Registry<PlotsPlugin, PlotFlag<?>> {
    public FlagRegistry(PlotsPlugin plugin) {
        super(plugin);
    }

    public PlotFlag<?> getRegistered(String id) {
        return registered.stream().filter(plotFlag -> plotFlag.flagId.equals(id)).findFirst().orElse(null);
    }
}
