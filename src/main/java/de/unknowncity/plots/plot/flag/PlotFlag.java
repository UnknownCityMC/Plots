package de.unknowncity.plots.plot.flag;

import java.util.Set;

public abstract class PlotFlag {
    private PlotFlagAccessModifier accessModifier;
    private String id;

    public abstract void checkAccess();

    public static Set<PlotFlag> defaults() {
        return Set.of(

        );
    }
}
