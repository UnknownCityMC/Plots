package de.unknowncity.plots.plot.model;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import de.chojo.sadu.mapper.annotation.MappingProvider;
import de.chojo.sadu.mapper.reader.StandardReader;
import de.chojo.sadu.mapper.rowmapper.RowMapping;
import de.unknowncity.astralib.common.message.lang.Language;
import de.unknowncity.astralib.paper.api.message.PaperMessenger;
import de.unknowncity.plots.plot.access.PlotState;
import de.unknowncity.plots.plot.access.type.PlotAccessModifier;
import de.unknowncity.plots.plot.access.type.PlotMemberRole;
import de.unknowncity.plots.plot.economy.PlotPaymentType;
import de.unknowncity.plots.plot.flag.PlotFlag;
import de.unknowncity.plots.plot.flag.PlotInteractable;
import de.unknowncity.plots.plot.flag.WorldGuardFlag;
import de.unknowncity.plots.plot.location.PlotHome;
import de.unknowncity.plots.plot.location.signs.PlotSign;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.*;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.spongepowered.configurate.NodePath;

import java.time.LocalDateTime;
import java.util.*;
import java.util.logging.Logger;

public abstract class Plot {
    private boolean broken = false;
    private final String plotId;
    private final String regionId;
    private final String worldName;
    private PlotPlayer owner;
    private String groupName;
    private double price;
    private PlotState state;
    private final LocalDateTime claimed;

    private final List<PlotMember> members = new ArrayList<>();
    private final List<PlotPlayer> deniedPlayers = new ArrayList<>();
    private final Map<PlotFlag<?>, Object> flags = new HashMap<>();
    private List<PlotInteractable> interactables = new ArrayList<>();
    private PlotHome plotHome;
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

    public abstract LocalDateTime lastRentPaid();

    public abstract long rentIntervalInMin();

    public abstract PlotPaymentType paymentType();

    public List<PlotMember> members() {
        return members;
    }

    public List<PlotPlayer> deniedPlayers() {
        return deniedPlayers;
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

    public PlotHome plotHome() {
        return plotHome;
    }

    public void plotHome(PlotHome plotHome) {
        this.plotHome = plotHome;
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

    public void updateInteractable(Material material, PlotAccessModifier modifier) {
        interactables.removeIf(plotInteractable -> plotInteractable.blockType() == material);
        interactables.add(new PlotInteractable(plotId, material, modifier));
    }

    public LocalDateTime claimed() {
        return claimed;
    }

    /**
     * Only call when certain that material is an interactable
     */
    public PlotInteractable getInteractable(Material material) {
        return interactables.stream().filter(plotInteractable -> plotInteractable.blockType() == material).findFirst().orElse(null);
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
        var location = new org.bukkit.Location(world(), protectedRegion().getMinimumPoint().x(), protectedRegion().getMinimumPoint().y(), protectedRegion().getMinimumPoint().z());
        return world().getBiome(location);
    }

    public double price() {
        return price;
    }

    public void updatePrice(double price) {
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
                Placeholder.component("state", messenger.component(player, NodePath.path("plot", "info", "state", state.name()))),
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
                try {
                    protectedRegion().setFlag((Flag<V>) worldGuardFlag.worldGuardFlag(), (V) val);
                } catch (Exception e) {
                    broken = true;
                    Logger.getLogger(JavaPlugin.class.getName()).severe("Plot " + plotId + " is broken (No region present)!");
                }
            }
        }
    }

    public Optional<PlotMember> findPlotMember(UUID uuid) {
        return members.stream().filter(plotMember -> plotMember.uuid().equals(uuid)).findFirst();
    }

    public Optional<PlotPlayer> findPlotBannedPlayer(UUID uuid) {
        return deniedPlayers.stream().filter(bannedPlayer -> bannedPlayer.uuid().equals(uuid)).findFirst();
    }

    public boolean isOwner(UUID uuid) {
        return owner != null && owner.uuid().equals(uuid);
    }

    public boolean isMember(UUID uuid) {
        return findPlotMember(uuid).isPresent();
    }

    public boolean isDenied(UUID uuid) {
        return findPlotBannedPlayer(uuid).isPresent();
    }

    public PlotMember addMember(OfflinePlayer offlinePlayer, PlotMemberRole role) {
        var plotMember = new PlotMember(plotId, offlinePlayer.getUniqueId(), offlinePlayer.getName(), role);
        members.add(plotMember);
        return plotMember;
    }

    public void removeMember(UUID uuid) {
        members.removeIf(member -> member.uuid().equals(uuid));
    }

    public PlotPlayer addDeniedPlayer(OfflinePlayer offlinePlayer) {
        var deniedPlayer = new PlotPlayer(plotId, offlinePlayer.getUniqueId(), offlinePlayer.getName());
        deniedPlayers.add(deniedPlayer);
        return deniedPlayer;
    }

    public void removeDeniedPlayer(UUID uuid) {
        deniedPlayers.removeIf(member -> member.uuid().equals(uuid));
    }

    public PlotSign addSign(Location location) {
        var sign = new PlotSign(plotId, location.getBlockX(), location.getBlockY(), location.getBlockZ());
        signs.add(sign);
        return sign;
    }

    public void removeSign(Location location) {
        signs.removeIf(plotSign -> plotSign.equals(new PlotSign(plotId, location.getBlockX(), location.getBlockY(), location.getBlockZ())));
    }

    public int height() {
        return broken ? 0 : protectedRegion().getMaximumPoint().y() - protectedRegion().getMinimumPoint().y() + 1;
    }

    public int width() {
        return broken ? 0 : protectedRegion().getMaximumPoint().z() - protectedRegion().getMinimumPoint().z() + 1;
    }

    public int depth() {
        return broken ? 0 : protectedRegion().getMaximumPoint().x() - protectedRegion().getMinimumPoint().x() + 1;
    }

    @MappingProvider({"payment_type", "id", "owner_id", "group_name", "region_id", "price", "world", "state", "claimed", "last_rent_paid", "rent_interval"})
    public static RowMapping<Plot> map() {
        return row -> {
            var plotId = row.getString("id");

            var paymentType = row.getEnum("payment_type", PlotPaymentType.class);

            var ownerId = row.get("owner_id", StandardReader.UUID_FROM_STRING);
            var owner = ownerId != null ? new PlotPlayer(
                    plotId,
                    ownerId,
                    Bukkit.getOfflinePlayer(ownerId).getName()
            ) : null;

            if (paymentType == PlotPaymentType.BUY) {
                return new BuyPlot(
                        plotId,
                        owner,
                        row.getString("group_name"),
                        row.getString("region_id"),
                        row.getDouble("price"),
                        row.getString("world"),
                        row.getEnum("state", PlotState.class),
                        row.get("claimed", StandardReader.LOCAL_DATE_TIME)
                );
            } else {
                return new RentPlot(
                        plotId,
                        owner,
                        row.getString("group_name"),
                        row.getString("region_id"),
                        row.getDouble("price"),
                        row.getString("world"),
                        row.getEnum("state", PlotState.class),
                        row.get("claimed", StandardReader.LOCAL_DATE_TIME),
                        row.get("last_rent_paid", StandardReader.LOCAL_DATE_TIME),
                        row.getLong("rent_interval")
                );
            }
        };
    }

    public boolean isBroken() {
        return broken;
    }
}