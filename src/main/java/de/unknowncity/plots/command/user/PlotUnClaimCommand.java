package de.unknowncity.plots.command.user;

import de.unknowncity.plots.PlotsPlugin;
import de.unknowncity.plots.command.SubCommand;
import de.unknowncity.plots.plot.access.PlotState;
import de.unknowncity.plots.service.EconomyService;
import de.unknowncity.plots.service.PlotService;
import de.unknowncity.plots.service.RegionService;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;
import org.spongepowered.configurate.NodePath;

public class PlotUnClaimCommand extends SubCommand {
    private final RegionService regionService = plugin.serviceRegistry().getRegistered(RegionService.class);
    private final PlotService plotService = plugin.serviceRegistry().getRegistered(PlotService.class);
    private final EconomyService economyService = plugin.serviceRegistry().getRegistered(EconomyService.class);

    public PlotUnClaimCommand(PlotsPlugin plugin, Command.Builder<CommandSender> builder) {
        super(plugin, builder);
    }

    @Override
    public void apply(CommandManager<CommandSender> commandManager) {
        commandManager.command(builder.literal("unclaim")
                .permission("plots.command.plot.unclaim")
                .senderType(Player.class)
                .handler(this::handleUnClaim)
                .build());
    }

    private void handleUnClaim(@NonNull CommandContext<Player> context) {
        var sender = context.sender();
        var possibleRegion = regionService.getSuitableRegion(sender.getLocation());

        if (possibleRegion.isEmpty()) {
            plugin.messenger().sendMessage(sender, NodePath.path("command", "plot", "no-plot"));
            return;
        }

        var plotId = possibleRegion.get().getId();

        if (!plotService.existsPlot(plotId)) {
            plugin.messenger().sendMessage(sender, NodePath.path("command", "plot", "no-plot"));
            return;
        }

        var plot = plotService.getPlot(plotId);
        if (plot.state() != PlotState.SOLD) {
            plugin.messenger().sendMessage(sender, NodePath.path("command", "plot", "unclaim", "unavailable"));
            return;
        }

        if (!plot.owner().uuid().equals(sender.getUniqueId())) {
            plugin.messenger().sendMessage(sender, NodePath.path("command", "plot", "unclaim", "no-owner"), plot.tagResolvers(sender, plugin.messenger()));
            return;
        }

        plotService.unClaimPlot(plot);
        plugin.messenger().sendMessage(sender, NodePath.path("command", "plot", "unclaim", "success"), plot.tagResolvers(sender, plugin.messenger()));
    }
}