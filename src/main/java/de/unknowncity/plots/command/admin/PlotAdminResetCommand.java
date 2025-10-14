package de.unknowncity.plots.command.admin;

import de.unknowncity.plots.Permissions;
import de.unknowncity.plots.PlotsPlugin;
import de.unknowncity.plots.command.SubCommand;
import de.unknowncity.plots.plot.PlotUtil;
import de.unknowncity.plots.plot.model.Plot;
import de.unknowncity.plots.service.PlotService;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.suggestion.Suggestion;
import org.spongepowered.configurate.NodePath;

import java.util.concurrent.CompletableFuture;

import static de.unknowncity.plots.command.argument.PlotParser.plotParser;
import static org.incendo.cloud.parser.standard.StringParser.stringParser;

public class PlotAdminResetCommand extends SubCommand {

    private final PlotService plotService = plugin.serviceRegistry().getRegistered(PlotService.class);

    public PlotAdminResetCommand(PlotsPlugin plugin, Command.Builder<CommandSender> builder) {
        super(plugin, builder);
    }

    @Override
    public void apply(CommandManager<CommandSender> commandManager) {
        commandManager.command(builder.literal("reset")
                .permission(Permissions.COMMAND_PLOT_ADMIN)
                .optional("plot", plotParser(plotService))
                .handler(this::handleDelete)
                .build()
        );

        commandManager.command(builder.literal("reset")
                .permission(Permissions.COMMAND_PLOT_ADMIN)
                .literal("confirm")
                .handler(plugin.confirmationManager().createExecutionHandler())
                .build()
        );
    }

    private void handleDelete(CommandContext<CommandSender> commandContext) {
        var player = (Player) commandContext.sender();

        if (commandContext.contains("plot")) {
            var plot = commandContext.<Plot>get("plot");
            plotService.resetPlot(plot);
        } else {
            var plotOpt = PlotUtil.getPlotIfPresent(player, plugin);
            if (plotOpt.isEmpty()) {
                plugin.messenger().sendMessage(player, NodePath.path("command", "plot", "no-plot"));
                return;
            }
            plotService.resetPlot(plotOpt.get());
        }

        plugin.messenger().sendMessage(player, NodePath.path("command", "plotadmin", "reset", "success"));
    }
}