package de.unknowncity.plots.command.admin;

import de.unknowncity.plots.Permissions;
import de.unknowncity.plots.PlotsPlugin;
import de.unknowncity.plots.command.SubCommand;
import de.unknowncity.plots.plot.PlotUtil;
import de.unknowncity.plots.plot.access.PlotState;
import de.unknowncity.plots.service.PlotService;
import de.unknowncity.plots.service.RegionService;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;
import org.spongepowered.configurate.NodePath;

import static de.unknowncity.plots.command.argument.UcPlayerParser.ucPlayerParser;
import static org.incendo.cloud.parser.standard.EnumParser.enumParser;
import static org.incendo.cloud.parser.standard.StringParser.stringParser;

public class PlotAdminSetStatusCommand extends SubCommand {

    private final PlotService plotService = plugin.serviceRegistry().getRegistered(PlotService.class);
    private final RegionService regionService = plugin.serviceRegistry().getRegistered(RegionService.class);

    public PlotAdminSetStatusCommand(PlotsPlugin plugin, Command.Builder<CommandSender> builder) {
        super(plugin, builder);
    }

    @Override
    public void apply(CommandManager<CommandSender> commandManager) {
        commandManager.command(builder.literal("setStatus")
                .permission(Permissions.COMMAND_PLOT_ADMIN)
                .required("state", enumParser(PlotState.class))
                .senderType(Player.class)
                .handler(this::setOwner)
                .build()
        );
    }

    private void setOwner(CommandContext<Player> commandContext) {
        var player = commandContext.sender();
        var state = commandContext.<PlotState>get("state");

        PlotUtil.getPlotIfPresent(player, plugin).ifPresentOrElse(plot -> {
            plot.state(state);
            plotService.savePlot(plot, false);
            plugin.messenger().sendMessage(player, NodePath.path("command", "plotadmin", "setstatus", "success"), plot.tagResolvers(player, plugin.messenger()));
        }, () -> plugin.messenger().sendMessage(player, NodePath.path("command", "plot", "no-plot")));
    }
}