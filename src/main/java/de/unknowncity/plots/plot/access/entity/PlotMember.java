package de.unknowncity.plots.plot.access.entity;

import de.unknowncity.plots.plot.access.type.PlotMemberRole;

import java.util.UUID;

public class PlotMember extends PlotPlayer {
    private PlotMemberRole role;

    public PlotMember(UUID uuid, String name, PlotMemberRole role) {
        super(uuid, name);
        this.role = role;
    }

    public PlotMemberRole role() {
        return role;
    }

    public void role(PlotMemberRole role) {
        this.role = role;
    }
}
