package de.unknowncity.plots.plot;

import de.unknowncity.plots.Permissions;
import de.unknowncity.plots.PlotsPlugin;
import de.unknowncity.plots.plot.access.PlotState;
import de.unknowncity.plots.plot.model.Plot;
import de.unknowncity.plots.plot.group.PlotGroup;
import de.unknowncity.plots.service.PlotService;
import de.unknowncity.plots.service.RegionService;
import de.unknowncity.plots.util.PermissionUtil;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
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

        return plotService.getPlot(plotId);
    }

    public static boolean checkPlotOwner(Player player, Plot plot, PlotsPlugin plugin) {
        if (!plot.owner().uuid().equals(player.getUniqueId()) && !player.hasPermission(Permissions.COMMAND_PLOT_ADMIN)) {
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

        return plotService.getPlot(plotId);
    }


    public static boolean checkPlotGroupLimit(Player player, String groupName, PlotsPlugin plugin) {
        var plotService = plugin.serviceRegistry().getRegistered(PlotService.class);
        var plotLimit = PermissionUtil.getPermValueInt(PlotGroup.permission(groupName), player);

        if (plotService.findPlotsByOwnerUUIDForGroup(player.getUniqueId(), groupName).size() >= plotLimit) {
            plugin.messenger().sendMessage(player, NodePath.path("command", "plot", "claim", "limit-reached"),
                    Placeholder.unparsed("group", String.valueOf(groupName)),
                    Placeholder.unparsed("limit", String.valueOf(plotLimit))
            );
            return false;
        }
        return true;
    }
}
