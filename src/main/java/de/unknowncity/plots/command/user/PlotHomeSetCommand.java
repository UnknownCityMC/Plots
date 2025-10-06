package de.unknowncity.plots.command.user;

import de.unknowncity.plots.PlotsPlugin;
import de.unknowncity.plots.command.SubCommand;
import de.unknowncity.plots.plot.PlotUtil;
import de.unknowncity.plots.service.plot.PlotLocationService;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;
import org.spongepowered.configurate.NodePath;

public class PlotHomeSetCommand extends SubCommand {
    private final PlotLocationService locationService = plugin.serviceRegistry().getRegistered(PlotLocationService.class);

    public PlotHomeSetCommand(PlotsPlugin plugin, Command.Builder<CommandSender> builder) {
        super(plugin, builder);
    }

    @Override
    public void apply(CommandManager<CommandSender> commandManager) {
        commandManager.command(builder.literal("home")
                .literal("setlocation")
                .permission("plots.command.plot.unclaim")
                .senderType(Player.class)
                .handler(this::handleHomeSet)
                .build());
    }

    private void handleHomeSet(@NonNull CommandContext<Player> context) {
        var sender = context.sender();

        PlotUtil.getPlotIfPresent(sender, plugin).ifPresentOrElse(plot -> {
            if (!PlotUtil.checkPlotSold(sender, plot, plugin)) {
                return;
            }

            if (!PlotUtil.checkPlotOwner(sender, plot, plugin)) {
                return;
            }

            var location = sender.getLocation();
            var oldHome = plot.plotHome();

            locationService.setPlotHome(plot, oldHome.isPublic(), location);

            plugin.messenger().sendMessage(sender, NodePath.path("command", "plot", "home", "setlocation", "success"), plot.tagResolvers(sender, plugin.messenger()));
        }, () -> plugin.messenger().sendMessage(sender, NodePath.path("command", "plot", "no-plot")));
    }
}