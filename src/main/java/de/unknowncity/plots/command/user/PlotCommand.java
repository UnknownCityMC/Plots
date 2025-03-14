package de.unknowncity.plots.command.user;

import de.unknowncity.astralib.paper.api.command.PaperCommand;
import de.unknowncity.plots.PlotsPlugin;
import org.bukkit.command.CommandSender;
import org.incendo.cloud.CommandManager;

public class PlotCommand extends PaperCommand<PlotsPlugin> {
    public PlotCommand(PlotsPlugin plugin) {
        super(plugin);
    }

    @Override
    public void apply(CommandManager<CommandSender> commandManager) {
        var builder = commandManager.commandBuilder("plot");

        new PlotClaimCommand(plugin, builder).apply(commandManager);
        new PlotUnClaimCommand(plugin, builder).apply(commandManager);
        new PlotInfoCommand(plugin, builder).apply(commandManager);
        new PlotAddMemberCommand(plugin, builder).apply(commandManager);
        new PlotRemoveMemberCommand(plugin, builder).apply(commandManager);
    }
}
