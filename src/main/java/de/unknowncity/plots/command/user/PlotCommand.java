package de.unknowncity.plots.command.user;

import de.unknowncity.astralib.paper.api.command.PaperCommand;
import de.unknowncity.plots.PlotsPlugin;
import de.unknowncity.plots.gui.PlotMainGUI;
import de.unknowncity.plots.plot.PlotUtil;
import de.unknowncity.plots.service.PlotService;
import de.unknowncity.plots.service.RegionService;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;
import org.spongepowered.configurate.NodePath;

public class PlotCommand extends PaperCommand<PlotsPlugin> {
    private final RegionService regionService = plugin.serviceRegistry().getRegistered(RegionService.class);
    private final PlotService plotService = plugin.serviceRegistry().getRegistered(PlotService.class);

    public PlotCommand(PlotsPlugin plugin) {
        super(plugin);
    }

    @Override
    public void apply(CommandManager<CommandSender> commandManager) {
        var builder = commandManager.commandBuilder("plot");

        commandManager.command(commandManager.commandBuilder("plot")
                .permission("plots.command.plot")
                .senderType(Player.class)
                .handler(this::handle)
        );

        new PlotClaimCommand(plugin, builder).apply(commandManager);
        new PlotSellCommand(plugin, builder).apply(commandManager);
        new PlotInfoCommand(plugin, builder).apply(commandManager);
        new PlotAddMemberCommand(plugin, builder).apply(commandManager);
        new PlotRemoveMemberCommand(plugin, builder).apply(commandManager);
        new PlotChangeRoleCommand(plugin, builder).apply(commandManager);
        new PlotHomeCommand(plugin, builder).apply(commandManager);
        new PlotDenyCommand(plugin, builder).apply(commandManager);
        new PlotUnDenyCommand(plugin, builder).apply(commandManager);
        new PlotAutoCommand(plugin, builder).apply(commandManager);

        new PlotHomeSetCommand(plugin, builder).apply(commandManager);
        new PlotHomeVisibilityCommand(plugin, builder).apply(commandManager);
    }

    private void handle(@NonNull CommandContext<Player> context) {
        var sender = context.sender();

        PlotUtil.getPlotIfPresent(sender, plugin).ifPresentOrElse(plot -> {
            if (!PlotUtil.checkPlotSold(sender, plot, plugin)) {
                return;
            }

            if (!plot.isOwner(sender.getUniqueId())) {
                plugin.messenger().sendMessage(sender, NodePath.path("command", "plot", "only-owner"), plot.tagResolvers(sender, plugin.messenger()));
                return;
            }

            PlotMainGUI.open(sender, plot, plugin);
        }, () -> {
            plugin.messenger().sendMessage(sender, NodePath.path("command", "plot", "no-plot"));
        });
    }
}
