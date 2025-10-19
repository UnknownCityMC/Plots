package de.unknowncity.plots.plot.freebuild;

import com.sk89q.worldedit.math.BlockVector3;
import de.unknowncity.plots.PlotsPlugin;
import de.unknowncity.plots.service.EconomyService;
import de.unknowncity.plots.service.PlotService;
import de.unknowncity.plots.service.RegionService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.spongepowered.configurate.NodePath;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.UUID;

public class LandCreateSession {
    private final PlotsPlugin plugin;
    private final Player player;
    private Location pos1;
    private Location pos2;

    private RegionService regionService;
    private PlotService plotService;
    private EconomyService economyService;
    private FreeBuildEditOutline freeBuildEditOutline;
    private int squareMeeters = 0;
    private double price = 0;
    private BukkitTask task;

    private static final String FREEBUILD_GROUP_NAME = "freebuild";

    public LandCreateSession(Player player, PlotsPlugin plugin) {
        this.player = player;
        this.plugin = plugin;
        this.freeBuildEditOutline = new FreeBuildEditOutline(player);

        this.regionService = plugin.serviceRegistry().getRegistered(RegionService.class);
        this.plotService = plugin.serviceRegistry().getRegistered(PlotService.class);
        this.economyService = plugin.serviceRegistry().getRegistered(EconomyService.class);
    }

    public Player player() {
        return player;
    }

    public void setPos1(Location location) {
        this.pos1 = location;
        updateOutline();
        updateSquareMeters();
        displayPrice();
    }

    public void setPos2(Location location) {
        this.pos2 = location;
        updateOutline();
        updateSquareMeters();
        displayPrice();
    }

    private void updateOutline() {
        boolean canAfford = false;
        if (pos1 != null && pos2 != null) {
            var loc1 = BlockVector3.at(pos1.getBlockX(), pos1.getBlockY(), pos1.getBlockZ());
            var loc2 = BlockVector3.at(pos2.getBlockX(), pos2.getBlockY(), pos2.getBlockZ());
            var price = regionService.calculateAreaSquareMeters(loc1, loc2) * plugin.configuration().fb().price();
            canAfford = economyService.hasEnoughFunds(player.getUniqueId(), price);
        }
        freeBuildEditOutline.updateOutline(plugin, pos1, pos2, true, canAfford);
    }

    private void updateSquareMeters() {
        if (pos1 == null || pos2 == null) {
            return;
        }

        this.squareMeeters = regionService.calculateAreaSquareMeters(pos1, pos2);
        this.price = Math.round(squareMeeters * plugin.configuration().fb().price() * 100.0) / 100.0 + 10;
    }

    public void complete() {
        var world = player.getWorld();

        String id = null;

        for (int i = 0; i < 5; i++) {
            var newId = generateId();
            if (newId == null) {
                continue;
            }

            if (plotService.existsPlot(newId)) {
                continue;
            }
            id = newId;
            break;
        }

        if (id == null || plotService.existsPlot(id)) {
            plugin.messenger().sendMessage(player, NodePath.path("command", "land", "buy", "id-not-generated"));
            return;
        }

        if (pos1 == null) {
            plugin.messenger().sendMessage(player, NodePath.path("command", "land", "buy", "not-selected"));
            return;
        }

        if (pos2 == null) {
            plugin.messenger().sendMessage(player, NodePath.path("command", "land", "buy", "not-selected"));
            return;
        }

        var loc1 = BlockVector3.at(pos1.getBlockX(), -64, pos1.getBlockZ());
        var loc2 = BlockVector3.at(pos2.getBlockX(), 320, pos2.getBlockZ());

        if (!economyService.hasEnoughFunds(player.getUniqueId(), price)) {
            plugin.messenger().sendMessage(player, NodePath.path("command", "land", "expand", "not-enough-money"), Placeholder.parsed("price", String.valueOf(price)));
            return;
        }

        var regionExist = regionService.doesRegionExistBetweenLocations(world, loc1, loc2);

        if (regionExist) {
            plugin.messenger().sendMessage(player, NodePath.path("command", "land", "buy", "already-exists"));
            return;
        }

        var protectedRegion = regionService.createRegionFromLocations(world, loc1, loc2, id);
        var plotOpt = plotService.createBuyPlotFromRegion(protectedRegion, world, price, FREEBUILD_GROUP_NAME);

        if (plotOpt.isPresent()) {
            plugin.messenger().sendMessage(player, NodePath.path("command", "land", "claim", "disable"));
            plugin.messenger().sendMessage(player, NodePath.path("command", "land", "buy", "success"));
            plotService.claimPlot(player, plotOpt.get());
        } else {
            plugin.messenger().sendMessage(player, NodePath.path("command", "land", "buy", "error"));
        }
    }

    public void close() {
        freeBuildEditOutline.updateOutline(plugin, pos1, pos2, false, false);
        player.sendActionBar(Component.empty());
        this.task.cancel();
    }

    private String generateId() {
        var uuid = UUID.randomUUID().toString();
        try {
            for (int i = 0; i < 5; i++) {
                var shortHash = Base64.getEncoder().withoutPadding()
                        .encodeToString(
                                MessageDigest.getInstance("SHA-256")
                                        .digest(uuid.getBytes())
                        ).substring(0, 10);
                if (!plotService.existsPlot(shortHash)) {
                    return shortHash;
                }
            }
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
        return null;
    }

    private void displayPrice() {
        if (this.task != null) {
            return;
        }
        this.task = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            if (economyService.hasEnoughFunds(player.getUniqueId(), price)) {
                plugin.messenger().sendActionBar(player, NodePath.path("command", "land", "claim", "price-enough"),
                        Placeholder.unparsed("price", String.valueOf(price)));

            } else {
                plugin.messenger().sendActionBar(player, NodePath.path("command", "land", "claim", "price-too-expensive"),
                        Placeholder.unparsed("price", String.valueOf(price)));

            }
        }, 0, 20);
    }
}
