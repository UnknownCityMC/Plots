package de.unknowncity.plots.feature;

import de.unknowncity.astralib.common.registry.Registry;
import de.unknowncity.plots.PlotsPlugin;

public class FeatureRegistry extends Registry<PlotsPlugin, Feature> {

    public FeatureRegistry(PlotsPlugin plugin) {
        super(plugin);
    }
}
