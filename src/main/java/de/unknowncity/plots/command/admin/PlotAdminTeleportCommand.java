package de.unknowncity.plots.command.admin;

import de.unknowncity.plots.Permissions;
import de.unknowncity.plots.PlotsPlugin;
import de.unknowncity.plots.command.SubCommand;
import de.unknowncity.plots.service.PlotService;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.suggestion.Suggestion;
import org.spongepowered.configurate.NodePath;

import java.util.concurrent.CompletableFuture;

import static org.incendo.cloud.parser.standard.StringParser.stringParser;

public class PlotAdminTeleportCommand extends SubCommand {
    private final PlotService plotService = plugin.serviceRegistry().getRegistered(PlotService.class);

    public PlotAdminTeleportCommand(PlotsPlugin plugin, Command.Builder<CommandSender> builder) {
        super(plugin, builder);
    }

    @Override
    public void apply(CommandManager<CommandSender> commandManager) {
        commandManager.command(builder.literal("teleport")
                .permission(Permissions.COMMAND_PLOT_ADMIN)
                .senderType(Player.class)
                .optional("plotId", stringParser(), (sender, input) ->
                        CompletableFuture.completedFuture(plotService.plotCache().asMap().keySet().stream().map(Suggestion::suggestion).toList()))
                .handler(this::handleTeleportId)
                .build()
        );
    }

    private void handleTeleportId(@NonNull CommandContext<Player> context) {
        var sender = context.sender();
        var id = context.getOrDefault("plotId", "");

        if (!plotService.existsPlot(id)) {
            plugin.messenger().sendMessage(sender, NodePath.path("command", "plot", "plot-tp", "not-found"));
            return;
        }

        var plot = plotService.getPlot(id);
        
        sender.teleport(plot.plotHome().getLocation(plot.world()));
        plugin.messenger().sendMessage(sender, NodePath.path("command", "plot", "plot-tp", "success"));
    }
}
