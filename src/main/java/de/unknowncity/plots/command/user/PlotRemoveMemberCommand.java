package de.unknowncity.plots.command.user;

import de.unknowncity.plots.PlotsPlugin;
import de.unknowncity.plots.command.SubCommand;
import de.unknowncity.plots.plot.PlotUtil;
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

public class PlotRemoveMemberCommand extends SubCommand {
    private final AccessService accessService = plugin.serviceRegistry().getRegistered(AccessService.class);

    public PlotRemoveMemberCommand(PlotsPlugin plugin, Command.Builder<CommandSender> builder) {
        super(plugin, builder);
    }

    @Override
    public void apply(CommandManager<CommandSender> commandManager) {
        commandManager.command(builder.literal("member").literal("remove")
                .permission("plots.command.plot.member.remove")
                .required("target", ucPlayerParser())
                .senderType(Player.class)
                .handler(this::handleAdd)
                .build());
    }

    private void handleAdd(@NonNull CommandContext<Player> context) {
        var sender = context.sender();
        var target = (OfflinePlayer) context.get("target");

        PlotUtil.getPlotIfPresent(sender, plugin).ifPresentOrElse(plot -> {
            if (!PlotUtil.checkPlotOwner(sender, plot, plugin)) {
                return;
            }

            if (plot.members().stream().noneMatch(plotMember -> plotMember.uuid().equals(target.getUniqueId()))) {
                plugin.messenger().sendMessage(sender, NodePath.path("command", "plot", "member", "no-member"), plot.tagResolvers(sender, plugin.messenger()));
                return;
            }

            accessService.removeMember(plot, target.getUniqueId());

            plugin.messenger().sendMessage(sender, NodePath.path("command", "plot", "member", "remove", "success"), plot.tagResolvers(sender, plugin.messenger()));
        }, () -> plugin.messenger().sendMessage(sender, NodePath.path("command", "plot", "no-plot")));
    }
}