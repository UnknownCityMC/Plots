package de.unknowncity.plots.command.user;

import de.unknowncity.plots.PlotsPlugin;
import de.unknowncity.plots.command.SubCommand;
import de.unknowncity.plots.service.PlotService;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;

import static org.incendo.cloud.parser.standard.IntegerParser.integerParser;
import static org.incendo.cloud.parser.standard.StringParser.stringParser;

public class PlotTeleportCommand extends SubCommand {
    private PlotService plotService = plugin.serviceRegistry().getRegistered(PlotService.class);

    public PlotTeleportCommand(PlotsPlugin plugin, Command.Builder<CommandSender> builder) {
        super(plugin, builder);
    }

    @Override
    public void apply(CommandManager<CommandSender> commandManager) {
        commandManager.command(commandManager.commandBuilder("teleport", "tp", "home", "h")
                .permission("plots.command.plot.teleport")
                .senderType(Player.class)
                .required("player", stringParser())
                .optional("id", integerParser(1))
                .handler(this::handleTeleportPlayer)
        );
    }

    private void handleTeleportPlayer(@NonNull CommandContext<Player> context) {
        var sender = context.sender();
        var plots = plotService.findPlotsByOwnerUUID(sender.getUniqueId());

        var targetPlayer = context.getOrDefault("player", sender.name());
    }
}
