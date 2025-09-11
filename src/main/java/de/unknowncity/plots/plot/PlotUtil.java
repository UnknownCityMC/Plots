package de.unknowncity.plots.plot;

import de.unknowncity.plots.PlotsPlugin;
import de.unknowncity.plots.plot.access.PlotState;
import de.unknowncity.plots.plot.model.Plot;
import de.unknowncity.plots.service.PlotService;
import de.unknowncity.plots.service.RegionService;
import org.bukkit.entity.Player;
import org.spongepowered.configurate.NodePath;

import java.util.Optional;

public class PlotUtil {

    public static Optional<Plot> checkForAndGetPlotIfPresent(Player sender, RegionService regionService, PlotService plotService, PlotsPlugin plugin) {
        var possibleRegion = regionService.getSuitableRegion(sender.getLocation());

        if (possibleRegion.isEmpty()) {
            plugin.messenger().sendMessage(sender, NodePath.path("command", "plot", "no-plot"));
            return Optional.empty();
        }

        var plotId = possibleRegion.get().getId();

        if (!plotService.existsPlot(plotId)) {
            plugin.messenger().sendMessage(sender, NodePath.path("command", "plot", "no-plot"));
            return Optional.empty();
        }

        var plot = plotService.getPlot(plotId);
        return Optional.of(plot);
    }

    public static boolean checkPlotOwner(Player player, Plot plot, PlotsPlugin plugin) {
        if (!plot.owner().uuid().equals(player.getUniqueId()) && !player.hasPermission("ucplots.command.plotadmin")) {
            plugin.messenger().sendMessage(player, NodePath.path("command", "plot", "no-owner"));
            return false;
        }
        return true;
    }

    public static boolean checkPlotSold(Player player, Plot plot, PlotsPlugin plugin) {
        if (plot.state() != PlotState.SOLD) {
            plugin.messenger().sendMessage(player, NodePath.path("command", "plot", "not-sold"));
            return false;
        }
        return true;
    }

    public static Optional<Plot> getPlotIfPresent(Player player, PlotsPlugin plugin) {
        var regionService = plugin.serviceRegistry().getRegistered(RegionService.class);
        var plotService = plugin.serviceRegistry().getRegistered(PlotService.class);

        var possibleRegion = regionService.getSuitableRegion(player.getLocation());
        if (possibleRegion.isEmpty()) {
            return Optional.empty();
        }
        var plotId = possibleRegion.get().getId();
        if (!plotService.existsPlot(plotId)) {
            return Optional.empty();
        }
        var plot = plotService.getPlot(plotId);
        return Optional.of(plot);
    }
}
