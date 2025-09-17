package de.unknowncity.plots.plot.freebuild;

import de.unknowncity.plots.PlotsPlugin;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class LandEditSessionHandler {
    private final PlotsPlugin plugin;

    private final Set<LandCreateSession> openSessions = new HashSet<>();

    public LandEditSessionHandler(PlotsPlugin plugin) {
        this.plugin = plugin;
    }

    public void openSession(Player player) {
        var session = new LandCreateSession(player, plugin);
        openSessions.add(session);
    }

    public Optional<LandCreateSession> findEditSession(Player player) {
        return openSessions.stream()
                .filter(landEditSession -> landEditSession.player().getUniqueId().equals(player.getUniqueId()))
                .findFirst();
    }

    public void closeEditSession(Player player) {
        findEditSession(player).ifPresent(openSessions::remove);
    }
}
