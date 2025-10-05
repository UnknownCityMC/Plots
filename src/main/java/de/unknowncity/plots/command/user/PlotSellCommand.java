package de.unknowncity.plots.command.user;

import de.unknowncity.plots.PlotsPlugin;
import de.unknowncity.plots.command.SubCommand;
import de.unknowncity.plots.plot.PlotUtil;
import de.unknowncity.plots.plot.model.RentPlot;
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

public class PlotSellCommand extends SubCommand {
    private final RegionService regionService = plugin.serviceRegistry().getRegistered(RegionService.class);
    private final PlotService plotService = plugin.serviceRegistry().getRegistered(PlotService.class);
    private final EconomyService economyService = plugin.serviceRegistry().getRegistered(EconomyService.class);

    public PlotSellCommand(PlotsPlugin plugin, Command.Builder<CommandSender> builder) {
        super(plugin, builder);
    }

    @Override
    public void apply(CommandManager<CommandSender> commandManager) {
        commandManager.command(builder.literal("sell")
                .permission("plots.command.plot.sell")
                .apply(plugin.confirmationManager())
                .senderType(Player.class)
                .handler(this::handleUnClaim)
                .build());

        commandManager.command(builder.literal("sell")
                .permission("plots.command.plot.sell")
                .literal("confirm")
                .handler(plugin.confirmationManager().createExecutionHandler())
                .build()
        );
    }

    private void handleUnClaim(@NonNull CommandContext<Player> context) {
        var sender = context.sender();

        PlotUtil.getPlotIfPresent(sender, plugin).ifPresentOrElse(plot -> {
            if (!PlotUtil.checkPlotSold(sender, plot, plugin)) {
                return;
            }

            if (!PlotUtil.checkPlotOwner(sender, plot, plugin)) {
                return;
            }

            plotService.unClaimPlot(plot);

            if (plot instanceof RentPlot) {
                plugin.messenger().sendMessage(sender, NodePath.path("command", "plot", "sell", "success-rent"), plot.tagResolvers(sender, plugin.messenger()));
            } else {
                plugin.messenger().sendMessage(sender, NodePath.path("command", "plot", "sell", "success"), plot.tagResolvers(sender, plugin.messenger()));
            }
        }, () -> plugin.messenger().sendMessage(sender, NodePath.path("command", "plot", "no-plot")));
    }
}