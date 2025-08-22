package de.unknowncity.plots.plot;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import de.unknowncity.astralib.common.message.lang.Language;
import de.unknowncity.astralib.paper.api.message.PaperMessenger;
import de.unknowncity.plots.plot.access.PlotState;
import de.unknowncity.plots.plot.access.entity.PlotMember;
import de.unknowncity.plots.plot.access.entity.PlotPlayer;
import de.unknowncity.plots.plot.access.type.PlotMemberRole;
import de.unknowncity.plots.plot.flag.PlotFlag;
import de.unknowncity.plots.plot.flag.PlotInteractable;
import de.unknowncity.plots.plot.flag.WorldGuardFlag;
import de.unknowncity.plots.plot.location.PlotLocation;
import de.unknowncity.plots.plot.location.signs.PlotSign;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.*;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;
import org.spongepowered.configurate.NodePath;

import java.time.LocalDateTime;
import java.util.*;

public abstract class Plot {
    private final String plotId;
    private final String regionId;
    private final String worldName;
    private PlotPlayer owner;
    private String groupName;
    private double price;
    private PlotState state;
    private LocalDateTime claimed;

    private List<PlotMember> members = new ArrayList<>();
    private List<PlotPlayer> deniedPlayers = new ArrayList<>();
    private final Map<PlotFlag<?>, Object> flags = new HashMap<>();
    private List<PlotInteractable> interactables = new ArrayList<>();
    private PlotLocation plotHome;
    private List<PlotSign> signs = new ArrayList<>();

    public Plot(String plotId, String groupName, PlotPlayer owner, String regionId, double price, String worldName, PlotState state, LocalDateTime claimed) {
        this.plotId = plotId;
        this.groupName = groupName;
        this.owner = owner;
        this.regionId = regionId;
        this.price = price;
        this.worldName = worldName;
        this.state = state;
        this.claimed = claimed;
    }

    public List<PlotMember> members() {
        return members;
    }

    public List<PlotPlayer> deniedPlayers() {
        return deniedPlayers;
    }

    public void deniedPlayers(List<PlotPlayer> bannedPlayers) {
        this.deniedPlayers.addAll(bannedPlayers);
    }

    public String id() {
        return plotId;
    }

    public String regionId() {
        return regionId;
    }

    public PlotPlayer owner() {
        return owner;
    }

    public void owner(PlotPlayer owner) {
        this.owner = owner;
    }

    public String groupName() {
        return groupName;
    }

    public PlotLocation plotHome() {
        return plotHome;
    }

    public void plotHome(PlotLocation plotHome) {
        this.plotHome = plotHome;
    }

    public List<PlotSign> signs() {
        return signs;
    }

    public LocalDateTime claimed() {
        return claimed;
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
                Placeholder.component("home-visibility", plotHome.isPublic() ?
                        messenger.component(player, NodePath.path("plot", "info", "home-public")) :
                        messenger.component(player, NodePath.path("plot", "info", "home-private"))),
                Placeholder.parsed("price", String.valueOf(price())),

                Placeholder.component("group", groupName() != null ? Component.text(groupName()) :
                        messenger.component(player, NodePath.path("plot", "info", "no-group"))),
                Placeholder.parsed("price", String.valueOf(price())),
                Placeholder.parsed("state", state().name()),
                Placeholder.component("owner-id", owner() != null ? Component.text(owner().toString())
                        : messenger.component(player, NodePath.path("plot", "info", "no-owner"))),
                Placeholder.component("owner-name", owner() != null ? Component.text(owner.name())
                        : messenger.component(Language.GERMAN, NodePath.path("plot", "info", "no-owner"))),
                Placeholder.parsed("world", worldName()),

                Placeholder.component("members", members().isEmpty() ? messenger.component(player, NodePath.path("plot", "info", "no-members")) :
                        Component.text(String.join(", ", members().stream().map(PlotMember::name).toList()))),
                Placeholder.component("banned", deniedPlayers().isEmpty() ? messenger.component(player, NodePath.path("plot", "info", "no-banned")) :
                        Component.text(String.join(", ", deniedPlayers().stream().map(PlotPlayer::name).toList()))),
                Placeholder.component("flags", flags() != null ? flags().keySet().stream().map(plotFlag ->
                        messenger.component(player, NodePath.path("plot", "info", "flag-format"),
                                Placeholder.component("flag-value", messenger.component(player, NodePath.path("flags", "value", flags.get(plotFlag).toString()))),
                                Placeholder.component("flag-id", messenger.component(player, NodePath.path("flags", "name", plotFlag.flagId()))))).reduce(Component::append).orElseGet(Component::empty) : Component.empty())
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
        return members.stream().filter(plotMember -> plotMember.uuid().equals(uuid)).findFirst();
    }

    public Optional<PlotPlayer> findPlotBannedPlayer(UUID uuid) {
        return deniedPlayers.stream().filter(bannedPlayer -> bannedPlayer.uuid().equals(uuid)).findFirst();
    }

    public void changeMemberRole(UUID memberId, PlotMemberRole newRole) {
        findPlotMember(memberId).ifPresent(plotMember -> {
            plotMember.role(newRole);
        });
    }

    public boolean isMemberOrOwner(UUID uuid) {
        return findPlotMember(uuid).isPresent() || owner().uuid().equals(uuid);
    }

    public boolean isOwner(UUID uuid) {
        return owner().uuid().equals(uuid);
    }

    public boolean isMember(UUID uuid) {
        return findPlotMember(uuid).isPresent();
    }

    public boolean isDenied(UUID uuid) {
        return findPlotBannedPlayer(uuid).isPresent();
    }

    public void trustPlayer(PlotMember plotMember) {
        members.add(plotMember);
    }

    public void denyPlayer(OfflinePlayer offlinePlayer) {
        members.removeIf(member -> member.uuid().equals(offlinePlayer.getUniqueId()));
        deniedPlayers.add(new PlotPlayer(offlinePlayer.getUniqueId(), offlinePlayer.getName()));
        if (offlinePlayer instanceof Player player) {
            if (player.hasPermission("plots.entry.bypass")) {
                return;
            }
            kick(player);
        }
    }

    public void kick(Player player) {
        if (player.hasPermission("plots.entry.bypass")) {
            return;
        }
        player.teleport(world().getSpawnLocation());
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