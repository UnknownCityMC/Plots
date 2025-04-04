package de.unknowncity.plots.data.model;

import de.unknowncity.plots.plot.flag.PlotFlag;

public record PlotFlagWrapper<T>(PlotFlag<T> flag, T flagValue) {

}
