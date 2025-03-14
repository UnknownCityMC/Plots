package de.unknowncity.plots.data.model.plot;

import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import de.unknowncity.plots.data.model.plot.flag.PlotFlag;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public abstract class Plot {
    private final String plotId;
    private final String regionId;
    private final String worldName;
    private UUID owner;
    private String groupName;
    private double price;
    private PlotState state;

    private List<PlotMember> members = new ArrayList<>();
    private List<PlotFlag> flags = new ArrayList<>();
    private List<RelativePlotLocation> locations = new ArrayList<>();

    public Plot(String plotId, String groupName, UUID owner, String regionId, double price, String worldName, PlotState state) {
        this.plotId = plotId;
        this.groupName = groupName;
        this.owner = owner;
        this.regionId = regionId;
        this.price = price;
        this.worldName = worldName;
        this.state = state;
    }

    public List<PlotFlag> flags() {
        return flags;
    }

    public List<PlotMember> members() {
        return members;
    }

    public String id() {
        return plotId;
    }

    public String regionId() {
        return regionId;
    }

    public UUID owner() {
        return owner;
    }

    public void owner(UUID owner) {
        this.owner = owner;
    }

    public String groupName() {
        return groupName;
    }

    public List<RelativePlotLocation> locations() {
        return locations;
    }

    public void flags(List<PlotFlag> plotFlags) {
        this.flags = plotFlags;
    }

    public void members(List<PlotMember> plotMembers) {
        this.members = plotMembers;
    }

    public void locations(List<RelativePlotLocation> locations) {
        this.locations = locations;
    }

    public void groupName(String groupName) {
        this.groupName = groupName;
    }

    public PlotState state() {
        return state;
    }

    public void state(PlotState state) {
        this.state = state;
    }

    public String worldName() {
        return worldName;
    }

    public World world() {
        return Bukkit.getWorld(worldName);
    }

    public ProtectedRegion protectedRegion() {
        return WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(world())).getRegions().get(regionId);
    }

    public double price() {
        return price;
    }

    public void price(double price) {
        this.price = price;
    }
}