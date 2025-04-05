package de.unknowncity.plots.plot.flag;

import de.unknowncity.plots.plot.access.type.PlotAccessModifier;
import de.unknowncity.plots.service.PlotService;
import org.bukkit.Material;

import java.util.List;

public class PlotAccessModifierFlag extends PlotFlag<PlotAccessModifier> {
    protected final PlotService plotService;

    public PlotAccessModifierFlag(String flagId, PlotAccessModifier defaultValue, Material displayMaterial, PlotService plotService) {
        super(flagId, defaultValue, displayMaterial);
        this.plotService = plotService;
    }

    @Override
    public PlotAccessModifier unmarshall(String input) {
        return PlotAccessModifier.valueOf(input);
    }

    @Override
    public String marshall(Object input) {
        return input.toString();
    }

    @Override
    public List<PlotAccessModifier> possibleValues() {
        return List.of(PlotAccessModifier.values());
    }
}
