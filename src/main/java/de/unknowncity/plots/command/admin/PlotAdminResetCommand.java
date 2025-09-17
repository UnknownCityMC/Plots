package de.unknowncity.plots.command.admin;

import de.unknowncity.plots.PlotsPlugin;
import de.unknowncity.plots.command.SubCommand;
import de.unknowncity.plots.plot.PlotUtil;
import de.unknowncity.plots.service.PlotService;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.suggestion.Suggestion;
import org.spongepowered.configurate.NodePath;

import java.util.concurrent.CompletableFuture;

import static org.incendo.cloud.parser.standard.StringParser.stringParser;

public class PlotAdminResetCommand extends SubCommand {

    private final PlotService plotService = plugin.serviceRegistry().getRegistered(PlotService.class);

    public PlotAdminResetCommand(PlotsPlugin plugin, Command.Builder<CommandSender> builder) {
        super(plugin, builder);
    }

    @Override
    public void apply(CommandManager<CommandSender> commandManager) {
        commandManager.command(builder.literal("reset")
                .permission("plots.command.plotadmin")
                .optional("id", stringParser(), (sender, input) ->
                        CompletableFuture.completedFuture(plotService.plotCache().asMap().keySet().stream().map(Suggestion::suggestion).toList()))
                .apply(plugin.confirmationManager())
                .handler(this::handleDelete)
                .build()
        );

        commandManager.command(builder.literal("reset")
                .permission("plots.command.plotadmin")
                .literal("confirm")
                .handler(plugin.confirmationManager().createExecutionHandler())
                .build()
        );
    }

    private void handleDelete(CommandContext<CommandSender> commandContext) {
        var player = (Player) commandContext.sender();
        var id = commandContext.getOrDefault("id", null);


        if (id != null) {
            if (!plotService.existsPlot(String.valueOf(id))) {
                plugin.messenger().sendMessage(player, NodePath.path("command", "plotadmin", "resye", "not-exists"));
                return;
            }
            plotService.resetPlot(plotService.getPlot(String.valueOf(id)));
        } else {
            PlotUtil.getPlotIfPresent(player, plugin).ifPresent(plotService::resetPlot);
        }

        plugin.messenger().sendMessage(player, NodePath.path("command", "plotadmin", "reset", "success"));
    }
}