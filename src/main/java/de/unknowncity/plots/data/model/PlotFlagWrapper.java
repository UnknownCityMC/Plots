package de.unknowncity.plots.data.model;

import de.unknowncity.plots.plot.flag.PlotFlag;

public record PlotFlagWrapper<T>(String plotId, PlotFlag<T> flag, T flagValue) {

}
