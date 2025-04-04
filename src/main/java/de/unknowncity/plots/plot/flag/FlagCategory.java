package de.unknowncity.plots.plot.flag;

import java.util.List;

public class FlagCategory {

    private final String name;
    private final List<PlotFlag<?>> flags;

    public FlagCategory(String name, List<PlotFlag<?>> flags) {
        this.name = name;
        this.flags = flags;
    }

    public String name() {
        return name;
    }

    public List<PlotFlag<?>> flags() {
        return flags;
    }

    public void addFlag(PlotFlag<?> flag) {
        flags.add(flag);
    }
}
