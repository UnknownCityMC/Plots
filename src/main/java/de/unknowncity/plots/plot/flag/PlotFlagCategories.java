package de.unknowncity.plots.plot.flag;

import de.unknowncity.plots.plot.flag.type.IceMeltFlag;

import java.util.List;

public class PlotFlagCategories {
    private static final List<Class<? extends PlotFlag<?>>> BLOCK_FLAGS = List.of(
            IceMeltFlag.class
    );

    public static List<Class<? extends PlotFlag<?>>> block() {
        return BLOCK_FLAGS;
    }
}
