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

import static org.incendo.cloud.bukkit.parser.PlayerParser.playerParser;

public class PlotAdminRemoveMemberCommand extends SubCommand {

    private final PlotService plotService = plugin.serviceRegistry().getRegistered(PlotService.class);
    private final RegionService regionService = plugin.serviceRegistry().getRegistered(RegionService.class);

    public PlotAdminRemoveMemberCommand(PlotsPlugin plugin, Command.Builder<CommandSender> builder) {
        super(plugin, builder);
    }

    @Override
    public void apply(CommandManager<CommandSender> commandManager) {
        commandManager.command(builder.literal("setMember")
                .permission("plots.command.plotadmin")
                .required("target", playerParser())
                .senderType(Player.class)
                .handler(this::setMember)
                .build()
        );
    }

    private void setMember(CommandContext<Player> commandContext) {
        var player = (Player) commandContext.get("target");
        var target = (Player) commandContext.get("target");
        var region = regionService.getSuitableRegion(player.getLocation());

        region.ifPresentOrElse(protectedRegion -> {
            if (!plotService.existsPlot(protectedRegion)) {
                plugin.messenger().sendMessage(player, NodePath.path("command", "plotadmin", "no-plot"));
                return;
            }

            var plot = plotService.getPlot(protectedRegion);

            if (plot.owner() == null) {
                plugin.messenger().sendMessage(player, NodePath.path("command", "plotadmin", "member", "not-sold"));
                return;
            }

            plotService.removeMember(target, plot);

            plugin.messenger().sendMessage(player, NodePath.path("command", "plotadmin", "remove-member", "success"));
        }, () -> plugin.messenger().sendMessage(player, NodePath.path("command", "plotadmin", "no-suitable-region")));
    }
}