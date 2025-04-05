package de.unknowncity.plots.plot.access.entity;

import de.unknowncity.plots.plot.access.type.PlotMemberRole;

import java.util.UUID;

public class PlotMember {
    private final UUID memberID;
    private final PlotMemberRole plotMemberRole;
    private String name;

    public PlotMember(UUID memberID, PlotMemberRole plotMemberRole) {
        this.memberID = memberID;
        this.plotMemberRole = plotMemberRole;
    }

    public PlotMember(UUID memberID, PlotMemberRole plotMemberRole, String name) {
        this.memberID = memberID;
        this.plotMemberRole = plotMemberRole;
        this.name = name;
    }

    public String name() {
        return name;
    }

    public void name(String name) {
        this.name = name;
    }

    public UUID memberID() {
        return memberID;
    }

    public PlotMemberRole plotMemberRole() {
        return plotMemberRole;
    }
}
