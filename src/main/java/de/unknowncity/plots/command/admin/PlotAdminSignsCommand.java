package de.unknowncity.plots.command.admin;

import de.unknowncity.plots.PlotsPlugin;
import de.unknowncity.plots.command.SubCommand;
import de.unknowncity.plots.plot.PlotUtil;
import de.unknowncity.plots.plot.location.signs.SignManager;
import de.unknowncity.plots.service.PlotService;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;
import org.spongepowered.configurate.NodePath;

import java.util.ArrayList;

public class PlotAdminSignsCommand extends SubCommand {

    public PlotAdminSignsCommand(PlotsPlugin plugin, Command.Builder<CommandSender> builder) {
        super(plugin, builder);
    }

    @Override
    public void apply(CommandManager<CommandSender> commandManager) {
        commandManager.command(builder.literal("signs")
                .permission("plots.command.plotadmin")
                .senderType(Player.class)
                .literal("deleteAll")
                .handler(this::handleDeleteAll)
        );

        commandManager.command(builder.literal("signs")
                .permission("plots.command.plotadmin")
                .senderType(Player.class)
                .literal("updateAll")
                .handler(this::handleUpdateAll)
        );
    }

    private void handleDeleteAll(@NonNull CommandContext<Player> context) {
        var sender = context.sender();
        PlotUtil.getPlotIfPresent(sender, plugin).ifPresentOrElse(plot -> {
            plot.signs().forEach(sign -> {
                SignManager.clearSign(new Location(plot.world(), sign.x(), sign.y(), sign.z()));
            });
            plot.signs(new ArrayList<>());
            plugin.serviceRegistry().getRegistered(PlotService.class).savePlot(plot);
            plugin.messenger().sendMessage(sender, NodePath.path("command", "plotadmin", "signs", "deleteAll"));
        }, () -> plugin.messenger().sendMessage(sender, NodePath.path("command", "plot", "no-plot")));
    }

    private void handleUpdateAll(@NonNull CommandContext<Player> context) {
        var sender = context.sender();
        var plotService = plugin.serviceRegistry().getRegistered(PlotService.class);
        plotService.plotCache().asMap().forEach((id, plot) -> {
            SignManager.updateSings(plot, plugin.messenger());
        });
        plugin.messenger().sendMessage(sender, NodePath.path("command", "plotadmin", "signs", "updateAll"));
    }
}
