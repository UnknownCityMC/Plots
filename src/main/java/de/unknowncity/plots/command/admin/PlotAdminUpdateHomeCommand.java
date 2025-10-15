package de.unknowncity.plots.command.admin;

import de.unknowncity.plots.Permissions;
import de.unknowncity.plots.PlotsPlugin;
import de.unknowncity.plots.command.SubCommand;
import de.unknowncity.plots.plot.PlotUtil;
import de.unknowncity.plots.plot.location.PlotPosition;
import de.unknowncity.plots.plot.location.signs.SignManager;
import de.unknowncity.plots.service.plot.PlotLocationService;
import de.unknowncity.plots.service.plot.SignService;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;
import org.spongepowered.configurate.NodePath;

public class PlotAdminUpdateHomeCommand extends SubCommand {
    private final SignService signService = plugin.serviceRegistry().getRegistered(SignService.class);

    public PlotAdminUpdateHomeCommand(PlotsPlugin plugin, Command.Builder<CommandSender> builder) {
        super(plugin, builder);
    }

    @Override
    public void apply(CommandManager<CommandSender> commandManager) {
        commandManager.command(builder.literal("updatehome")
                .permission(Permissions.COMMAND_PLOT_ADMIN)
                .senderType(Player.class)
                .handler(this::handleUpdateHome)
        );
    }

    private void handleUpdateHome(@NonNull CommandContext<Player> context) {
        var sender = context.sender();
        PlotUtil.getPlotIfPresent(sender, plugin).ifPresentOrElse(plot -> {
            var location = sender.getLocation();
            plugin.serviceRegistry().getRegistered(PlotLocationService.class).setPlotHome(plot, true, location);
            plugin.serviceRegistry().getRegistered(PlotLocationService.class).setPlotHomeResetLocation(plot, PlotPosition.fromLocation(plot.id(), location));

            plugin.messenger().sendMessage(sender, NodePath.path("command", "plotadmin", "updatehome", "success"));
        }, () -> plugin.messenger().sendMessage(sender, NodePath.path("command", "plot", "no-plot")));
    }
}
