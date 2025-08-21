package de.unknowncity.plots.command.user;

import de.unknowncity.plots.PlotsPlugin;
import de.unknowncity.plots.command.SubCommand;
import de.unknowncity.plots.plot.PlotUtil;
import de.unknowncity.plots.plot.access.type.PlotMemberRole;
import de.unknowncity.plots.service.PlotService;
import de.unknowncity.plots.service.RegionService;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;
import org.spongepowered.configurate.NodePath;

import static de.unknowncity.plots.command.argument.UcPlayerParser.ucPlayerParser;
import static org.incendo.cloud.parser.standard.EnumParser.enumParser;

public class PlotChangeRoleCommand extends SubCommand {
    private final RegionService regionService = plugin.serviceRegistry().getRegistered(RegionService.class);
    private final PlotService plotService = plugin.serviceRegistry().getRegistered(PlotService.class);

    public PlotChangeRoleCommand(PlotsPlugin plugin, Command.Builder<CommandSender> builder) {
        super(plugin, builder);
    }

    @Override
    public void apply(CommandManager<CommandSender> commandManager) {
        commandManager.command(builder.literal("member").literal("changeRole")
                .permission("plots.command.plot.member.changeRole")
                .required("target", ucPlayerParser())
                .required("role", enumParser(PlotMemberRole.class))
                .senderType(Player.class)
                .handler(this::handleAdd)
                .build());
    }

    private void handleAdd(@NonNull CommandContext<Player> context) {
        var sender = context.sender();
        var target = (OfflinePlayer) context.get("target");
        var role = (PlotMemberRole) context.get("role");

        PlotUtil.getPlotIfPresent(sender, plugin).ifPresent(plot -> {
            if (!PlotUtil.checkPlotOwner(sender, plot, plugin)) {
                return;
            }

            if (plot.isMember(target.getUniqueId())) {
                plugin.messenger().sendMessage(sender, NodePath.path("command", "plot", "member", "already-member"), plot.tagResolvers(sender, plugin.messenger()));
            }

            plot.changeMemberRole(target.getUniqueId(), role);
            plotService.savePlot(plot);

            plugin.messenger().sendMessage(sender, NodePath.path("command", "plot", "member", "success"), plot.tagResolvers(sender, plugin.messenger()));
        });
    }
}