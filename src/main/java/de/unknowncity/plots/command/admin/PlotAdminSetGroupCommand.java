package de.unknowncity.plots.command.admin;

import de.unknowncity.plots.PlotsPlugin;
import de.unknowncity.plots.command.SubCommand;
import de.unknowncity.plots.service.PlotService;
import de.unknowncity.plots.service.RegionService;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;
import org.spongepowered.configurate.NodePath;

import static org.incendo.cloud.parser.standard.StringParser.stringParser;

public class PlotAdminSetGroupCommand extends SubCommand {

    private final PlotService plotService = plugin.serviceRegistry().getRegistered(PlotService.class);
    private final RegionService regionService = plugin.serviceRegistry().getRegistered(RegionService.class);

    public PlotAdminSetGroupCommand(PlotsPlugin plugin, Command.Builder<CommandSender> builder) {
        super(plugin, builder);
    }

    @Override
    public void apply(CommandManager<CommandSender> commandManager) {
        commandManager.command(builder.literal("setGroup")
                .permission("plots.command.plotadmin")
                .required("group-name", stringParser())
                .senderType(Player.class)
                .handler(this::setGroup)
                .build()
        );
    }


    private void setGroup(CommandContext<Player> commandContext) {
        var player = commandContext.sender();
        var groupName = commandContext.<String>get("group-name");

        if (!plotService.existsGroup(groupName)) {
            plugin.messenger().sendMessage(player, NodePath.path("command", "plotadmin", "set-group", "no-group"));
            return;
        }

        var region = regionService.getSuitableRegion(player.getLocation());

        region.ifPresentOrElse(protectedRegion -> {
            if (!plotService.existsPlot(protectedRegion)) {
                plugin.messenger().sendMessage(player, NodePath.path("command", "plotadmin", "no-plot"));
                return;
            }

            var plot = plotService.getPlot(protectedRegion);

            plotService.setPlotGroup(groupName, plot);
            plugin.messenger().sendMessage(player, NodePath.path("command", "plotadmin", "set-group", "success"), plot.tagResolvers(player, plugin.messenger()));

        }, () -> plugin.messenger().sendMessage(player, NodePath.path("command", "plotadmin", "no-suitable-region")));
    }
}
