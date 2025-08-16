package de.unknowncity.plots.plot.location.signs;

import com.destroystokyo.paper.MaterialTags;
import de.unknowncity.astralib.common.message.lang.Language;
import de.unknowncity.astralib.paper.api.message.PaperMessenger;
import de.unknowncity.plots.PlotsPlugin;
import de.unknowncity.plots.plot.Plot;
import de.unknowncity.plots.service.PlotService;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.entity.Player;
import org.spongepowered.configurate.NodePath;

import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

import static de.unknowncity.plots.plot.location.signs.SignOutline.setOutline;

public class SignManager {
    private final HashMap<UUID, SignEditSession> editSessions = new HashMap<>();
    private final PlotsPlugin plugin;

    public SignManager(PlotsPlugin plugin) {
        this.plugin = plugin;
    }

    public SignEditSession openEditSession(Player player) {
        var editSession = new SignEditSession(plugin);
        editSessions.put(player.getUniqueId(), editSession);
        return editSession;
    }

    public Optional<SignEditSession> findOpenEditSession(Player player) {
        return editSessions.containsKey(player.getUniqueId()) ? Optional.of(editSessions.get(player.getUniqueId())) : Optional.empty();
    }

    public void closeEditSession(Player player) {
        findOpenEditSession(player).ifPresent(signEditSession -> {
            signEditSession.finish();
            editSessions.remove(player.getUniqueId());
        });
    }

    public void collectGarbage() {
        plugin.serviceRegistry().getRegistered(PlotService.class).plotCache().forEach((s, foundPlot) -> foundPlot.signs().forEach(plotSign -> {
            setOutline(foundPlot, plotSign, false);
        }));
    }

    public static void updateSings(Plot plot, PaperMessenger messenger) {
        plot.signs().forEach(relativePlotLocation -> {
            var loc = new Location(plot.world(), relativePlotLocation.x(), relativePlotLocation.y(), relativePlotLocation.z());
            var block = plot.world().getBlockAt(loc);

            if (!MaterialTags.SIGNS.isTagged(block)) {
                plot.signs().remove(relativePlotLocation);
                return;
            }

            var state = plot.state().name().toLowerCase();

            Sign sign = (Sign) block.getState();

            sign.getSide(Side.FRONT).line(0, messenger.component(Language.GERMAN, NodePath.path("sign", state, "line-1"), plot.tagResolvers(null, messenger)));
            sign.getSide(Side.FRONT).line(1, messenger.component(Language.GERMAN, NodePath.path("sign", state, "line-2"), plot.tagResolvers(null, messenger)));
            sign.getSide(Side.FRONT).line(2, messenger.component(Language.GERMAN, NodePath.path("sign", state, "line-3"), plot.tagResolvers(null, messenger)));
            sign.getSide(Side.FRONT).line(3, messenger.component(Language.GERMAN, NodePath.path("sign", state, "line-4"), plot.tagResolvers(null, messenger)));
            sign.update();
        });
    }

    public static void clearSign(Location location) {
        Sign sign = (Sign) location.getBlock().getState();
        sign.getSide(Side.FRONT).line(0, Component.empty());
        sign.getSide(Side.FRONT).line(1, Component.empty());
        sign.getSide(Side.FRONT).line(2, Component.empty());
        sign.getSide(Side.FRONT).line(3, Component.empty());
        sign.update();
    }
}