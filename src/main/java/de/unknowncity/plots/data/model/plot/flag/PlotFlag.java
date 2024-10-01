package de.unknowncity.plots.data.model.plot.flag;

import java.util.List;

public abstract class PlotFlag {
    private PlotFlagAccessModifier accessModifier;
    private String actionId;

    public abstract void checkAccess();

    public static List<PlotFlag> defaults() {
        return List.of(

        );
    }

    public static PlotFlag create(String actionId, PlotFlagAccessModifier accessModifier) {
        return null;
    }

    public PlotFlagAccessModifier accessModifier() {
        return accessModifier;
    }

    public String actionId() {
        return actionId;
    }
}
