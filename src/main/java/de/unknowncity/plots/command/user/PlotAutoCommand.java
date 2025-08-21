package de.unknowncity.plots.command.user;

import de.unknowncity.plots.PlotsPlugin;
import de.unknowncity.plots.command.SubCommand;
import de.unknowncity.plots.plot.PlotUtil;
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

import java.util.concurrent.ThreadLocalRandom;

public class PlotAutoCommand extends SubCommand {
    private final RegionService regionService = plugin.serviceRegistry().getRegistered(RegionService.class);
    private final PlotService plotService = plugin.serviceRegistry().getRegistered(PlotService.class);
    private final EconomyService economyService = plugin.serviceRegistry().getRegistered(EconomyService.class);

    public PlotAutoCommand(PlotsPlugin plugin, Command.Builder<CommandSender> builder) {
        super(plugin, builder);
    }

    @Override
    public void apply(CommandManager<CommandSender> commandManager) {
        commandManager.command(builder.literal("auto")
                .permission("plots.command.plot.claim")
                .senderType(Player.class)
                .handler(this::handleClaim)
                .build());
    }

    private void handleClaim(@NonNull CommandContext<Player> context) {
        var sender = context.sender();

        var ownedPlots = plotService.findPlotsByOwnerUUID(sender.getUniqueId());

        if (ownedPlots.isEmpty()) {
            var availablePlots = plotService.findAvailablePlots();
            var randomPlot = availablePlots.get(ThreadLocalRandom.current().nextInt(availablePlots.size()));
            if (!plugin.serviceRegistry().getRegistered(EconomyService.class).hasEnoughFunds(sender.getUniqueId(), randomPlot.price())) {
                plugin.messenger().sendMessage(sender, NodePath.path("command", "plot", "claim", "not-enough-money"));
                return;
            }

            plotService.claimPlot(sender, randomPlot);
            sender.teleport(randomPlot.plotHome().getLocation(randomPlot.world()));
            plugin.messenger().sendMessage(sender, NodePath.path("command", "plot", "claim", "success"),
                    randomPlot.tagResolvers(sender, plugin.messenger()));
        } else {
            var plot = ownedPlots.getFirst();
            sender.teleport(plot.plotHome().getLocation(plot.world()));
        }
    }
}