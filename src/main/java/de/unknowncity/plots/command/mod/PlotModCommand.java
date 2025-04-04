package de.unknowncity.plots.command.mod;

import de.unknowncity.astralib.paper.api.command.PaperCommand;
import de.unknowncity.plots.PlotsPlugin;
import org.bukkit.command.CommandSender;
import org.incendo.cloud.CommandManager;

public class PlotModCommand extends PaperCommand<PlotsPlugin> {
    public PlotModCommand(PlotsPlugin plugin) {
        super(plugin);
    }

    @Override
    public void apply(CommandManager<CommandSender> commandManager) {
        var builder = commandManager.commandBuilder("plotmod");

        new PlotModCreateCommand(plugin, builder).apply(commandManager);
        new PlotModExpandCommand(plugin, builder).apply(commandManager);
    }
}
