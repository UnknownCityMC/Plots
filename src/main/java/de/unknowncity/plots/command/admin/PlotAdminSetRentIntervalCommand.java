package de.unknowncity.plots.command.admin;

import de.unknowncity.plots.Permissions;
import de.unknowncity.plots.PlotsPlugin;
import de.unknowncity.plots.command.SubCommand;
import de.unknowncity.plots.plot.PlotUtil;
import de.unknowncity.plots.plot.location.signs.SignManager;
import de.unknowncity.plots.plot.model.RentPlot;
import de.unknowncity.plots.service.PlotService;
import de.unknowncity.plots.service.RegionService;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;
import org.spongepowered.configurate.NodePath;

import java.time.Duration;

import static org.incendo.cloud.parser.standard.DurationParser.durationParser;

public class PlotAdminSetRentIntervalCommand extends SubCommand {

    private final PlotService plotService = plugin.serviceRegistry().getRegistered(PlotService.class);
    private final RegionService regionService = plugin.serviceRegistry().getRegistered(RegionService.class);

    public PlotAdminSetRentIntervalCommand(PlotsPlugin plugin, Command.Builder<CommandSender> builder) {
        super(plugin, builder);
    }

    @Override
    public void apply(CommandManager<CommandSender> commandManager) {
        commandManager.command(builder.literal("setRentInterval")
                .permission(Permissions.COMMAND_PLOT_ADMIN)
                .required("interval", durationParser())
                .senderType(Player.class)
                .handler(this::setRentInterval)
                .build()
        );
    }

    private void setRentInterval(CommandContext<Player> commandContext) {
        var player = commandContext.sender();
        var interval = commandContext.<Duration>get("interval");

        PlotUtil.getPlotIfPresent(player, plugin).ifPresentOrElse(plot -> {
            if (!(plot instanceof RentPlot rentPlot)) {
                plugin.messenger().sendMessage(player, NodePath.path("command", "plotadmin", "setinterval", "not-rent-plot"), plot.tagResolvers(player, plugin.messenger()));
                return;
            }
            rentPlot.rentIntervalInMin(interval.toMinutes());
            plotService.savePlot(plot);
            SignManager.updateSings(plot, plugin.messenger());
            plugin.messenger().sendMessage(player, NodePath.path("command", "plotadmin", "setinterval", "success"), plot.tagResolvers(player, plugin.messenger()));
        }, () -> plugin.messenger().sendMessage(player, NodePath.path("command", "plot", "no-plot")));
    }
}
