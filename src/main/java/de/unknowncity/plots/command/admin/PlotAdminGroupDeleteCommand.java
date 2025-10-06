package de.unknowncity.plots.command.admin;

import de.unknowncity.plots.Permissions;
import de.unknowncity.plots.PlotsPlugin;
import de.unknowncity.plots.command.SubCommand;
import de.unknowncity.plots.plot.group.PlotGroup;
import de.unknowncity.plots.service.PlotService;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.suggestion.Suggestion;
import org.spongepowered.configurate.NodePath;

import java.util.concurrent.CompletableFuture;

import static de.unknowncity.plots.command.argument.PlotGroupParser.plotGroupParser;
import static org.incendo.cloud.parser.standard.StringParser.stringParser;

public class PlotAdminGroupDeleteCommand extends SubCommand {

    private final PlotService plotService = plugin.serviceRegistry().getRegistered(PlotService.class);

    public PlotAdminGroupDeleteCommand(PlotsPlugin plugin, Command.Builder<CommandSender> builder) {
        super(plugin, builder);
    }

    @Override
    public void apply(CommandManager<CommandSender> commandManager) {
        commandManager.command(builder.literal("group").literal("delete")
                .permission(Permissions.COMMAND_PLOT_ADMIN)
                .required("group", plotGroupParser(plotService))
                .apply(plugin.confirmationManager())
                .handler(this::handleDelete)
                .build()
        );

        commandManager.command(builder.literal("group").literal("delete")
                .permission(Permissions.COMMAND_PLOT_ADMIN)
                .literal("confirm")
                .handler(plugin.confirmationManager().createExecutionHandler())
                .build()
        );
    }

    private void handleDelete(CommandContext<CommandSender> commandContext) {
        var player = (Player) commandContext.sender();
        var group = commandContext.<PlotGroup>get("group");

        plotService.deletePlotGroup(group.name());

        plugin.messenger().sendMessage(player, NodePath.path("command", "plotadmin", "group", "delete", "success"));
    }
}
