package de.unknowncity.plots.plot.freebuild;

import de.unknowncity.plots.plot.Plot;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

public class FreeBuildPlotCreationManager {
    private HashMap<UUID, FreeBuildPlotCreateSession> createSessions;

    public FreeBuildPlotCreateSession openSession(Player player) {
        var session = new FreeBuildPlotCreateSession();
        session.open();
        createSessions.put(player.getUniqueId(), session);
        return session;
    }

    public Optional<FreeBuildPlotCreateSession> findOpenSession(Player player) {
        return createSessions.containsKey(player.getUniqueId()) ? Optional.of(createSessions.get(player.getUniqueId())) : Optional.empty();
    }

    public void closeSession(Player player) {
        findOpenSession(player).ifPresent(freeBuildPlotCreateSession -> {
            freeBuildPlotCreateSession.close();
        });
        createSessions.remove(player.getUniqueId());
    }
}
