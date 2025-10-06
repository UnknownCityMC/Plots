package de.unknowncity.plots.command.user;

import de.unknowncity.plots.PlotsPlugin;
import de.unknowncity.plots.command.SubCommand;
import de.unknowncity.plots.plot.PlotUtil;
import de.unknowncity.plots.plot.access.type.PlotMemberRole;
import de.unknowncity.plots.service.plot.AccessService;
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

public class PlotAddMemberCommand extends SubCommand {
    private final AccessService accessService = plugin.serviceRegistry().getRegistered(AccessService.class);

    public PlotAddMemberCommand(PlotsPlugin plugin, Command.Builder<CommandSender> builder) {
        super(plugin, builder);
    }

    @Override
    public void apply(CommandManager<CommandSender> commandManager) {
        commandManager.command(builder.literal("member").literal("add")
                .permission("plots.command.plot.member.add")
                .required("target", ucPlayerParser())
                .optional("role", enumParser(PlotMemberRole.class))
                .senderType(Player.class)
                .handler(this::handleAdd)
                .build());
    }

    private void handleAdd(@NonNull CommandContext<Player> context) {
        var sender = context.sender();
        var target = (OfflinePlayer) context.get("target");
        var role = context.getOrDefault("role", PlotMemberRole.MEMBER);

        PlotUtil.getPlotIfPresent(sender, plugin).ifPresentOrElse(plot -> {
            if (!PlotUtil.checkPlotOwner(sender, plot, plugin)) {
                return;
            }

            if (plot.isMember(target.getUniqueId()) || plot.isOwner(target.getUniqueId())) {
                plugin.messenger().sendMessage(sender, NodePath.path("command", "plot", "member", "already-member"), plot.tagResolvers(sender, plugin.messenger()));
                return;
            }

            accessService.addMember(plot, target, role);

            plugin.messenger().sendMessage(sender, NodePath.path("command", "plot", "member", "add", "success"), plot.tagResolvers(sender, plugin.messenger()));
        }, () -> plugin.messenger().sendMessage(sender, NodePath.path("command", "plot", "no-plot")));
    }
}