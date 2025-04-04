package de.unknowncity.plots.command.admin;

import de.unknowncity.plots.PlotsPlugin;
import de.unknowncity.plots.command.SubCommand;
import de.unknowncity.plots.service.PlotService;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;
import org.spongepowered.configurate.NodePath;

import static org.incendo.cloud.parser.standard.StringParser.stringParser;

public class PlotAdminDeleteCommand extends SubCommand {

    private final PlotService plotService = plugin.serviceRegistry().getRegistered(PlotService.class);

    public PlotAdminDeleteCommand(PlotsPlugin plugin, Command.Builder<CommandSender> builder) {
        super(plugin, builder);
    }

    @Override
    public void apply(CommandManager<CommandSender> commandManager) {
        commandManager.command(builder.literal("delete")
                .permission("ucplots.command.plotadmin")
                .required("id", stringParser())
                .senderType(Player.class)
                .handler(this::handleDelete)
                .build()
        );
    }

    private void handleDelete(@NonNull CommandContext<Player> commandContext) {
        var player = commandContext.sender();
        var id = commandContext.<String>get("id");

        if (!plotService.existsPlot(id)) {
            plugin.messenger().sendMessage(player, NodePath.path("command", "plotadmin", "delete", "not-exists"));
            return;
        }

        plotService.deletePlot(id);
        plugin.messenger().sendMessage(player, NodePath.path("command", "plotadmin", "delete", "success"));
    }
}