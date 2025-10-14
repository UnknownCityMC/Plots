package de.unknowncity.plots.feature;

import de.unknowncity.astralib.common.registry.registrable.Registrable;
import de.unknowncity.plots.PlotsPlugin;

public class Feature implements Registrable<PlotsPlugin> {
    protected final PlotsPlugin plugin;

    public Feature(PlotsPlugin plugin) {
        this.plugin = plugin;
    }
}
