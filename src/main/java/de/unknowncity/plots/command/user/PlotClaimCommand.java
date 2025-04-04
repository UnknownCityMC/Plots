package de.unknowncity.plots.command.user;

import de.unknowncity.plots.PlotsPlugin;
import de.unknowncity.plots.command.SubCommand;
import de.unknowncity.plots.plot.access.PlotState;
import de.unknowncity.plots.service.EconomyService;
import de.unknowncity.plots.service.PlotService;
import de.unknowncity.plots.service.RegionService;
import de.unknowncity.plots.util.PlotId;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;
import org.spongepowered.configurate.NodePath;

public class PlotClaimCommand extends SubCommand {
    private final RegionService regionService = plugin.serviceRegistry().getRegistered(RegionService.class);
    private final PlotService plotService = plugin.serviceRegistry().getRegistered(PlotService.class);
    private final EconomyService economyService = plugin.serviceRegistry().getRegistered(EconomyService.class);

    public PlotClaimCommand(PlotsPlugin plugin, Command.Builder<CommandSender> builder) {
        super(plugin, builder);
    }

    @Override
    public void apply(CommandManager<CommandSender> commandManager) {
        commandManager.command(builder.literal("claim")
                .permission("plots.command.plot.claim")
                .senderType(Player.class)
                .handler(this::handleClaim)
                .build());
    }

    private void handleClaim(@NonNull CommandContext<Player> context) {
        var sender = context.sender();
        var possibleRegion = regionService.getSuitableRegion(sender.getLocation());

        if (possibleRegion.isEmpty()) {
            plugin.messenger().sendMessage(sender, NodePath.path("command", "plot", "no-plot"));
            return;
        }

        var plotId = PlotId.generate(sender.getWorld(), possibleRegion.get());

        if (!plotService.existsPlot(plotId)) {
            plugin.messenger().sendMessage(sender, NodePath.path("command", "plot", "no-plot"));
            return;
        }

        var plot = plotService.getPlot(plotId);
        if (plot.state() != PlotState.AVAILABLE) {
            plugin.messenger().sendMessage(sender, NodePath.path("command", "plot", "claim", "unavailable"), plot.tagResolvers(sender, plugin.messenger()));
            return;
        }

        if (!economyService.hasEnoughFunds(sender.getUniqueId(), plot.price())) {
            plugin.messenger().sendMessage(sender, NodePath.path("command", "plot", "claim", "not-enough-money"), plot.tagResolvers(sender, plugin.messenger()));
            return;
        }

        plotService.claimPlot(sender, plot);
        plugin.messenger().sendMessage(sender, NodePath.path("command", "plot", "claim", "success"), plot.tagResolvers(sender, plugin.messenger()));
    }
}