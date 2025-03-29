package de.unknowncity.plots.data.model.plot;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import de.unknowncity.astralib.common.message.lang.Language;
import de.unknowncity.astralib.paper.api.message.PaperMessenger;
import de.unknowncity.plots.data.model.plot.flag.PlotFlag;
import de.unknowncity.plots.data.model.plot.flag.PlotInteractable;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.apache.logging.log4j.util.Strings;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;
import org.spongepowered.configurate.NodePath;

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
    private List<PlotInteractable> interactables = new ArrayList<>();
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

    public List<PlotInteractable> interactables() {
        return interactables;
    }

    public void interactables(List<PlotInteractable> interactables) {
        this.interactables = interactables;
    }

    /**
     * Only call when certain that material is a interactable
     */
    public PlotInteractable getInteractable(Material material) {
        return interactables.stream().filter(plotInteractable -> plotInteractable.blockType() == material).findFirst().orElse(null);
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

    public Biome biome() {
        var location = new Location(world(), protectedRegion().getMinimumPoint().x(), protectedRegion().getMinimumPoint().y(), protectedRegion().getMinimumPoint().z());
        return world().getBiome(location);
    }

    public double price() {
        return price;
    }

    public void price(double price) {
        this.price = price;
    }

    public TagResolver[] tagResolvers(Player player, PaperMessenger messenger) {
        return new TagResolver[]{
                Placeholder.parsed("id", plotId),
                Placeholder.component("group", groupName() != null ? Component.text(groupName()) :
                        messenger.component(player, NodePath.path("plot", "no-group"))),
                Placeholder.parsed("price", String.valueOf(price())),
                Placeholder.parsed("state", state().name()),
                Placeholder.component("owner", owner() != null ? Component.text(owner().toString())
                        : messenger.component(player, NodePath.path("plot", "no-owner"))),
                Placeholder.parsed("world", worldName()),
                Placeholder.component("members", members().isEmpty() ? messenger.component(Language.GERMAN, NodePath.path("plot", "no-members")) :
                        Component.text(Strings.join(members().stream().map(PlotMember::memberID).toList(), ','))),
                Placeholder.parsed("flags", flags() != null ? flags().toString() : ""),
        };
    }

    public TagResolver[] tagResolvers(PaperMessenger messenger) {
        return new TagResolver[]{
                Placeholder.parsed("id", plotId),
                Placeholder.component("group", groupName() != null ? Component.text(groupName()) :
                        messenger.component(Language.GERMAN, NodePath.path("plot", "no-group"))),
                Placeholder.parsed("price", String.valueOf(price())),
                Placeholder.parsed("state", state().name()),
                Placeholder.component("owner", owner() != null ? Component.text(owner().toString())
                        : messenger.component(Language.GERMAN, NodePath.path("plot", "no-owner"))),
                Placeholder.parsed("world", worldName()),
                Placeholder.component("members", members().isEmpty() ? messenger.component(Language.GERMAN, NodePath.path("plot", "no-members")) :
                        Component.text(Strings.join(members().stream().map(PlotMember::memberID).toList(), ','))),
                Placeholder.parsed("flags", flags() != null ? flags().toString() : ""),
        };
    }
}