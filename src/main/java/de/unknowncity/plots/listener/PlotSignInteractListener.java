package de.unknowncity.plots.listener;

import com.destroystokyo.paper.MaterialTags;
import de.unknowncity.astralib.paper.api.message.PaperMessenger;
import de.unknowncity.plots.PlotsPlugin;
import de.unknowncity.plots.plot.PlotUtil;
import de.unknowncity.plots.plot.access.PlotState;
import de.unknowncity.plots.plot.location.signs.SignManager;
import de.unknowncity.plots.service.EconomyService;
import de.unknowncity.plots.service.PlotService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.spongepowered.configurate.NodePath;

public class PlotSignInteractListener implements Listener {
    private final PlotService plotService;
    private final SignManager signManager;
    private final PaperMessenger messenger;
    private final EconomyService economyService;
    private final PlotsPlugin plugin;

    public PlotSignInteractListener(PlotsPlugin plugin) {
        this.plotService = plugin.serviceRegistry().getRegistered(PlotService.class);
        this.signManager = plugin.signManager();
        this.messenger = plugin.messenger();
        this.economyService = plugin.serviceRegistry().getRegistered(EconomyService.class);
        this.plugin = plugin;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.LEFT_CLICK_BLOCK && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        if (event.getClickedBlock() == null) {
            return;
        }

        if (!MaterialTags.SIGNS.isTagged(event.getClickedBlock().getType())) {
            return;
        }

        var player = event.getPlayer();

        var possibleEditSession = signManager.findOpenEditSession(event.getPlayer());

        if (possibleEditSession.isPresent()) {
            return;
        }

        var possiblePlot = plotService.getPlotForSignLocation(event.getClickedBlock().getLocation());
        if (possiblePlot.isEmpty()) {
            return;
        }

        event.setCancelled(true);

        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        var plot = possiblePlot.get();
        var groupName = plot.groupName();

        if (!PlotUtil.checkPlotGroupLimit(player, groupName, plugin)) {
            return;
        }

        if (plot.state() != PlotState.AVAILABLE) {
            messenger.sendMessage(player, NodePath.path("command", "plot", "claim", "unavailable"), plot.tagResolvers(player, messenger));
            return;
        }

        if (!economyService.hasEnoughFunds(player.getUniqueId(), plot.price())) {
            messenger.sendMessage(player, NodePath.path("command", "plot", "claim", "not-enough-money"), plot.tagResolvers(player, messenger));
            return;
        }

        plotService.claimPlot(player, plot);
        messenger.sendMessage(player, NodePath.path("command", "plot", "claim", "success"), plot.tagResolvers(player, messenger));
    }
}
