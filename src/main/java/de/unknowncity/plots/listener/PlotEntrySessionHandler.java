package de.unknowncity.plots.listener;

import com.sk89q.worldedit.util.Location;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.session.MoveType;
import com.sk89q.worldguard.session.Session;
import com.sk89q.worldguard.session.handler.Handler;
import de.unknowncity.astralib.common.temporal.CooldownAction;
import de.unknowncity.astralib.paper.api.message.PaperMessenger;
import de.unknowncity.plots.Permissions;
import de.unknowncity.plots.service.PlotService;
import org.bukkit.Bukkit;
import org.spongepowered.configurate.NodePath;

import java.time.Duration;
import java.util.Set;

public class PlotEntrySessionHandler extends Handler {
    private final PlotService plotService;
    private final PaperMessenger messenger;
    private final CooldownAction cooldownAction = new CooldownAction(Duration.ofSeconds(2));


    public static class Factory extends Handler.Factory<PlotEntrySessionHandler> {
        private final PlotService plotService;
        private final PaperMessenger paperMessenger;

        public Factory(PlotService plotService, PaperMessenger paperMessenger) {
            this.plotService = plotService;
            this.paperMessenger = paperMessenger;
        }

        @Override
        public PlotEntrySessionHandler create(Session session) {
            return new PlotEntrySessionHandler(session, plotService, paperMessenger);
        }
    }

    protected PlotEntrySessionHandler(Session session, PlotService plotService, PaperMessenger messenger) {
        super(session);
        this.plotService = plotService;
        this.messenger = messenger;
    }

    @Override
    public boolean onCrossBoundary(LocalPlayer player, Location from, Location to, ApplicableRegionSet toSet, Set<ProtectedRegion> entered, Set<ProtectedRegion> exited, MoveType moveType) {
        for (ProtectedRegion protectedRegion : entered) {
            var possiblePlot = plotService.findPlotForRegion(protectedRegion);
            if (possiblePlot.isEmpty()) {
                continue;
            }

            var plot = possiblePlot.get();
            var bannedPlayer = plot.findPlotBannedPlayer(player.getUniqueId());

            if (bannedPlayer.isPresent()) {
                var bukkitPlayer = Bukkit.getPlayer(player.getUniqueId());
                if (bukkitPlayer == null) {
                    return false;
                }

                if (bukkitPlayer.hasPermission(Permissions.BYPASS_ENTRY)) {
                    return true;
                }

                cooldownAction.execute(() ->{
                    messenger.sendMessage(bukkitPlayer, NodePath.path("event", "plot", "enter", "deny"));
                    bukkitPlayer.playSound(bukkitPlayer, "block.note_block.bass", 1, 1);
                });
                return false;
            }
        }
        return true;
    }
}
