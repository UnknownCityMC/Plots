package de.unknowncity.plots.command.user;

import de.unknowncity.plots.PlotsPlugin;
import de.unknowncity.plots.command.SubCommand;
import de.unknowncity.plots.service.PlotService;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;
import org.spongepowered.configurate.NodePath;

import static de.unknowncity.plots.command.argument.UcPlayerParser.ucPlayerParser;
import static org.incendo.cloud.parser.standard.IntegerParser.integerParser;

public class PlotTeleportCommand extends SubCommand {
    private final PlotService plotService = plugin.serviceRegistry().getRegistered(PlotService.class);

    public PlotTeleportCommand(PlotsPlugin plugin, Command.Builder<CommandSender> builder) {
        super(plugin, builder);
    }

    @Override
    public void apply(CommandManager<CommandSender> commandManager) {
        commandManager.command(builder.literal("teleport", "tp", "home", "h")
                .permission("plots.command.plot.teleport")
                .senderType(Player.class)
                .required("player", ucPlayerParser())
                .optional("id", integerParser(1))
                .handler(this::handleTeleportPlayer)
                .build()
        );
    }

    private void handleTeleportPlayer(@NonNull CommandContext<Player> context) {
        var sender = context.sender();
        var id = context.getOrDefault("id", 1);
        var targetPlayer = (OfflinePlayer) context.getOrDefault("player", sender);

        var plots = plotService.findPlotsByOwnerUUID(targetPlayer.getUniqueId());

        if (plots.size() < id) {
            plugin.messenger().sendMessage(sender, NodePath.path("command", "plot-tp", "not-found"));
            return;
        }

        var plot = plots.get(id - 1);
        if (
                plot.plotHome().isPublic() ||
                        plot.owner().equals(sender.getUniqueId()) ||
                        plot.members().stream().anyMatch(plotMember -> plotMember.memberID().equals(sender.getUniqueId())) ||
                        sender.hasPermission("plots.command.plot.teleport.others")
        ) {
            sender.teleport(plot.plotHome().getLocation(plot.world()));
            plugin.messenger().sendMessage(sender, NodePath.path("command", "plot-tp", "success"));
        }
    }
}
