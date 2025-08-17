package de.unknowncity.plots.plot;

import de.unknowncity.plots.PlotsPlugin;
import de.unknowncity.plots.service.PlotService;
import de.unknowncity.plots.service.RegionService;
import org.bukkit.entity.Player;
import org.spongepowered.configurate.NodePath;

import java.util.Optional;

public class PlotUtil {

    public static Optional<Plot> checkPlotConditionsAndGetPlotIfPresent(Player sender, RegionService regionService, PlotService plotService, PlotsPlugin plugin) {
        var plotOptional = checkForAndGetPlotIfPresent(sender, regionService, plotService, plugin);

        if (plotOptional.isEmpty()) {
            return plotOptional;
        }

        var plot = plotOptional.get();

        if (!plot.owner().uuid().equals(sender.getUniqueId()) && !sender.hasPermission("ucplots.command.plotadmin")) {
            plugin.messenger().sendMessage(sender, NodePath.path("command", "plot", "no-owner"));
            return Optional.empty();
        }

        return Optional.of(plot);
    }

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
}
