package de.unknowncity.plots.plot;

import de.unknowncity.astralib.common.registry.registrable.Registrable;
import de.unknowncity.plots.PlotsPlugin;
import de.unknowncity.plots.plot.flag.PlotFlag;

import java.util.List;

public class RegisteredPlotFlag<T> implements Registrable<PlotsPlugin> {
        private final PlotFlag<T> plotFlag;
        private final List<T> possibleVValues;

    public RegisteredPlotFlag(PlotFlag<T> plotFlag, List<T> possibleVValues) {
        this.plotFlag = plotFlag;
        this.possibleVValues = possibleVValues;
    }

    public PlotFlag<T> plotFlag() {

        return plotFlag;
    }

    public List<T> possibleVValues() {
        return possibleVValues;
    }
}