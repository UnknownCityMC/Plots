package de.unknowncity.plots.plot.flag;

import de.unknowncity.astralib.common.registry.registrable.Registrable;
import de.unknowncity.plots.PlotsPlugin;
import org.bukkit.Material;

import java.util.List;

public abstract class PlotFlag<T> implements Registrable<PlotsPlugin> {

    protected final String flagId;
    protected final T defaultValue;
    private final Material displayMaterial;

    public PlotFlag(String flagId, T defaultValue, Material displayMaterial) {
        this.flagId = flagId;
        this.defaultValue = defaultValue;
        this.displayMaterial = displayMaterial;
    }

    public String flagId() {
        return flagId;
    }

    public T defaultValue() {
        return defaultValue;
    }

    public Material displayMaterial() {
        return displayMaterial;
    }

    public abstract T unmarshall(String input);
    public abstract String marshall(Object input);

    public abstract List<T> possibleValues();
}