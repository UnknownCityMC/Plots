package de.unknowncity.plots.command.user;

import de.unknowncity.astralib.paper.api.command.PaperCommand;
import de.unknowncity.plots.PlotsPlugin;
import de.unknowncity.plots.gui.PlotMainGUI;
import de.unknowncity.plots.service.PlotService;
import de.unknowncity.plots.service.RegionService;
import de.unknowncity.plots.util.PlotId;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.incendo.cloud.CommandManager;
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

        commandManager.command(builder
                .senderType(Player.class)
                .handler(commandContext -> {
                    var sender = commandContext.sender();
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

                    if (!plot.owner().equals(sender.getUniqueId()) && !sender.hasPermission("ucplots.command.plotadmin")) {
                        plugin.messenger().sendMessage(sender, NodePath.path("command", "plot", "member", "no-owner"));
                        return;
                    }

                    PlotMainGUI.open(sender, plot, plugin);
                }));

        new PlotClaimCommand(plugin, builder).apply(commandManager);
        new PlotUnClaimCommand(plugin, builder).apply(commandManager);
        new PlotInfoCommand(plugin, builder).apply(commandManager);
        new PlotAddMemberCommand(plugin, builder).apply(commandManager);
        new PlotRemoveMemberCommand(plugin, builder).apply(commandManager);
        new PlotChangeRoleCommand(plugin, builder).apply(commandManager);
    }
}
