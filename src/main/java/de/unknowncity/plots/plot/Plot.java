package de.unknowncity.plots.plot;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import de.unknowncity.astralib.common.message.lang.Language;
import de.unknowncity.astralib.paper.api.message.PaperMessenger;
import de.unknowncity.plots.plot.access.entity.BannedPlayer;
import de.unknowncity.plots.plot.access.entity.PlotMember;
import de.unknowncity.plots.plot.access.PlotState;
import de.unknowncity.plots.plot.flag.PlotFlag;
import de.unknowncity.plots.plot.flag.PlotInteractable;
import de.unknowncity.plots.plot.flag.WorldGuardFlag;
import de.unknowncity.plots.plot.location.PlotLocation;
import de.unknowncity.plots.plot.location.signs.PlotSign;
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
import java.util.*;

public abstract class Plot {
    private final String plotId;
    private final String regionId;
    private final String worldName;
    private UUID owner;
    private String groupName;
    private double price;
    private PlotState state;

    private List<PlotMember> members = new ArrayList<>();
    private List<BannedPlayer> bannedPlayers = new ArrayList<>();
    private final Map<PlotFlag<?>, Object> flags = new HashMap<>();
    private List<PlotInteractable> interactables = new ArrayList<>();
    private List<PlotLocation> locations = new ArrayList<>();
    private List<PlotSign> signs = new ArrayList<>();

    public Plot(String plotId, String groupName, UUID owner, String regionId, double price, String worldName, PlotState state) {
        this.plotId = plotId;
        this.groupName = groupName;
        this.owner = owner;
        this.regionId = regionId;
        this.price = price;
        this.worldName = worldName;
        this.state = state;
    }

    public List<PlotMember> members() {
        return members;
    }

    public List<BannedPlayer> bannedPlayers() {
        return bannedPlayers;
    }

    public void bannedPlayers(List<BannedPlayer> bannedPlayers) {
        this.bannedPlayers = bannedPlayers;
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

    public List<PlotLocation> locations() {
        return locations;
    }

    public List<PlotSign> signs() {
        return signs;
    }

    public Map<PlotFlag<?>, ?> flags() {
        return flags;
    }

    public List<PlotInteractable> interactables() {
        return interactables;
    }

    public void interactables(List<PlotInteractable> interactables) {
        this.interactables = interactables;
    }

    /**
     * Only call when certain that material is an interactable
     */
    public PlotInteractable getInteractable(Material material) {
        return interactables.stream().filter(plotInteractable -> plotInteractable.blockType() == material).findFirst().orElse(null);
    }

    public void members(List<PlotMember> plotMembers) {
        this.members = plotMembers;
    }

    public void locations(List<PlotLocation> locations) {
        this.locations = locations;
    }

    public void signs(List<PlotSign> signs) {
        this.signs = signs;
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
                Placeholder.parsed("height", String.valueOf(height())),
                Placeholder.parsed("width", String.valueOf(width())),
                Placeholder.parsed("depth", String.valueOf(depth())),

                Placeholder.parsed("price", String.valueOf(price())),

                Placeholder.component("group", groupName() != null ? Component.text(groupName()) :
                        messenger.component(player, NodePath.path("plot", "no-group"))),
                Placeholder.parsed("price", String.valueOf(price())),
                Placeholder.parsed("state", state().name()),
                Placeholder.component("owner", owner() != null ? Component.text(owner().toString())
                        : messenger.component(player, NodePath.path("plot", "no-owner"))),
                Placeholder.parsed("world", worldName()),

                Placeholder.component("members", !members().isEmpty() ? messenger.component(player, NodePath.path("plot", "no-members")) :
                        Component.text(String.join(", ", members().stream().map(PlotMember::name).toList()))),
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

                Placeholder.component("banned", !bannedPlayers().isEmpty() ? Component.text(String.join(", ", bannedPlayers().stream().map(BannedPlayer::name).toList())) :
                        messenger.component(Language.GERMAN, NodePath.path("plot", "no-banned"))),
                Placeholder.parsed("flags", flags() != null ? flags().toString() : ""),
        };
    }

    @SuppressWarnings("unchecked")
    public <T extends PlotFlag<V>, V> V getFlag(T flag) {
        return (V) flags.get(flag);
    }

    @SuppressWarnings("unchecked")
    public <V> void setFlag(PlotFlag<?> flag, Object val) {
        if (val == null) {
            flags.remove(flag);
        } else {
            flags.put(flag, val);
            if (flag instanceof WorldGuardFlag<?> worldGuardFlag) {
                protectedRegion().setFlag((Flag<V>) worldGuardFlag.worldGuardFlag(), (V) val);
            }
        }
    }

    public Optional<PlotMember> findPlotMember(UUID uuid) {
        return members.stream().filter(plotMember -> plotMember.memberID().equals(uuid)).findFirst();
    }

    public Optional<BannedPlayer> findPlotBannedPlayer(UUID uuid) {
        return bannedPlayers.stream().filter(bannedPlayer -> bannedPlayer.uuid().equals(uuid)).findFirst();
    }

    public int height() {
        return protectedRegion().getMaximumPoint().y() - protectedRegion().getMinimumPoint().y() + 1;
    }

    public int width() {
        return protectedRegion().getMaximumPoint().z() - protectedRegion().getMinimumPoint().z() + 1;
    }

    public int depth() {
        return protectedRegion().getMaximumPoint().x() - protectedRegion().getMinimumPoint().x() + 1;
    }
}