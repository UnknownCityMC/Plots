package de.unknowncity.plots.data.model.plot;

import de.unknowncity.plots.data.model.plot.flag.PlotFlag;

import java.util.HashSet;
import java.util.Set;

public class Plot {
    private final String plotId;
    private final String groupName;
    private final String regionId;
    private PlotMeta meta;
    private Set<PlotMember> members = new HashSet<>();
    private Set<PlotFlag> flags = new HashSet<>();
    private Set<RelativePlotLocation> locations = new HashSet<>();

    public Plot(String plotId, String groupName, String regionId) {
        this.plotId = plotId;
        this.groupName = groupName;
        this.regionId = regionId;
    }

    public Set<PlotFlag> flags() {
        return flags;
    }

    public Set<PlotMember> members() {
        return members;
    }

    public PlotMeta meta() {
        return meta;
    }

    public String id() {
        return plotId;
    }

    public String regionId() {
        return regionId;
    }

    public String groupName() {
        return groupName;
    }

    public Set<RelativePlotLocation> locations() {
        return locations;
    }

    public void meta(PlotMeta plotMeta) {
        this.meta = plotMeta;
    }

    public void flags(Set<PlotFlag> plotFlags) {
        this.flags = plotFlags;
    }

    public void members(Set<PlotMember> plotMembers) {
        this.members = plotMembers;
    }

    public void locations(Set<RelativePlotLocation> locations) {
        this.locations = locations;
    }
}
