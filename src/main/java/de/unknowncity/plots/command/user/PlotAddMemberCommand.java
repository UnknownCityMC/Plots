package de.unknowncity.plots.command.user;

import de.unknowncity.plots.PlotsPlugin;
import de.unknowncity.plots.command.SubCommand;
import de.unknowncity.plots.data.model.plot.PlotMemberRole;
import de.unknowncity.plots.service.PlotService;
import de.unknowncity.plots.service.RegionService;
import de.unknowncity.plots.util.PlotId;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.suggestion.Suggestion;
import org.spongepowered.configurate.NodePath;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

import static org.incendo.cloud.bukkit.parser.PlayerParser.playerParser;
import static org.incendo.cloud.parser.standard.EnumParser.enumParser;
import static org.incendo.cloud.parser.standard.StringParser.stringParser;

public class PlotAddMemberCommand extends SubCommand {
    private RegionService regionService = plugin.serviceRegistry().getRegistered(RegionService.class);
    private PlotService plotService = plugin.serviceRegistry().getRegistered(PlotService.class);

    public PlotAddMemberCommand(PlotsPlugin plugin, Command.Builder<CommandSender> builder) {
        super(plugin, builder);
    }

    @Override
    public void apply(CommandManager<CommandSender> commandManager) {
        commandManager.command(builder.literal("member").literal("add")
                .permission("plots.command.plot.member.add")
                .required("target", playerParser())
                .required("role", enumParser(PlotMemberRole.class))
                .senderType(Player.class)
                .handler(this::handleAdd)
                .build());
    }

    private void handleAdd(@NonNull CommandContext<Player> context) {
        var sender = context.sender();
        var target = (Player) context.get("target");
        var role = (PlotMemberRole) context.get("role");

        var possibleRegion = regionService.getSuitableRegion(sender.getLocation());

        if (possibleRegion.isEmpty()) {
            plugin.messenger().sendMessage(sender, NodePath.path("command", "plot", "no-plot"));
            return;
        }

        var plotId = PlotId.generate(sender.getWorld(), possibleRegion.get());

        if (!plotService.existsPlot(plotId)) {
            plugin.messenger().sendMessage(sender, NodePath.path("command", "plot", "no-plot"));
            return;
        }

        var plot = plotService.getPlot(plotId);
        if (!plot.owner().equals(sender.getUniqueId())) {
            plugin.messenger().sendMessage(sender, NodePath.path("command", "plot", "member", "no-owner"));
            return;
        }

        if(plot.members().stream().anyMatch(plotMember -> plotMember.memberID().equals(target.getUniqueId()))){
            plugin.messenger().sendMessage(sender, NodePath.path("command", "plot", "member", "already-member"));
            return;
        }

        plotService.addMember(target, role, plot);
        plugin.messenger().sendMessage(sender, NodePath.path("command", "plot", "member", "success"));
    }
}