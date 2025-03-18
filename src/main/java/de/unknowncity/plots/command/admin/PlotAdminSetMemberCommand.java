package de.unknowncity.plots.command.admin;

import de.unknowncity.plots.PlotsPlugin;
import de.unknowncity.plots.command.SubCommand;
import de.unknowncity.plots.data.model.plot.PlotMemberRole;
import de.unknowncity.plots.service.PlotService;
import de.unknowncity.plots.service.RegionService;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;
import org.spongepowered.configurate.NodePath;

import static org.incendo.cloud.bukkit.parser.PlayerParser.playerParser;
import static org.incendo.cloud.parser.standard.EnumParser.enumParser;

public class PlotAdminSetMemberCommand extends SubCommand {

    private final PlotService plotService = plugin.serviceRegistry().getRegistered(PlotService.class);
    private final RegionService regionService = plugin.serviceRegistry().getRegistered(RegionService.class);

    public PlotAdminSetMemberCommand(PlotsPlugin plugin, Command.Builder<CommandSender> builder) {
        super(plugin, builder);
    }

    @Override
    public void apply(CommandManager<CommandSender> commandManager) {
        commandManager.command(builder.literal("setMember")
                .permission("ucplots.command.plotadmin")
                .required("target", playerParser())
                .required("role", enumParser(PlotMemberRole.class))
                .senderType(Player.class)
                .handler(this::setMember)
                .build()
        );
    }

    private void setMember(CommandContext<Player> commandContext) {
        var player = (Player) commandContext.get("target");
        var target = (Player) commandContext.get("target");
        var role = (PlotMemberRole) commandContext.get("role");
        var region = regionService.getSuitableRegion(player.getLocation());

        region.ifPresentOrElse(protectedRegion -> {
            var world = player.getWorld();

            if (!plotService.existsPlot(protectedRegion, world)) {
                plugin.messenger().sendMessage(player, NodePath.path("command", "plotadmin", "no-plot"));
                return;
            }

            var plot = plotService.getPlot(world, protectedRegion);

            if (plot.owner() == null) {
                plugin.messenger().sendMessage(player, NodePath.path("command", "plotadmin", "member", "not-sold"));
                return;
            }

            if (plot.members().stream().anyMatch(plotMember -> plotMember.memberID().equals(target.getUniqueId()))) {
                plotService.removeMember(target, plot);
            }

            plotService.addMember(target, role, plot);
            plugin.messenger().sendMessage(player, NodePath.path("command", "plotadmin", "set-member", "success"));
        }, () -> plugin.messenger().sendMessage(player, NodePath.path("command", "plotadmin", "no-suitable-region")));
    }
}