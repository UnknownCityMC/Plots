package de.unknowncity.plots.command.admin;

import de.unknowncity.astralib.paper.api.command.PaperCommand;
import de.unknowncity.plots.PlotsPlugin;
import org.bukkit.command.CommandSender;
import org.incendo.cloud.CommandManager;

public class PlotAdminCommand extends PaperCommand<PlotsPlugin> {
    public PlotAdminCommand(PlotsPlugin plugin) {
        super(plugin);
    }

    @Override
    public void apply(CommandManager<CommandSender> commandManager) {
        var builder = commandManager.commandBuilder("plotadmin");

        new PlotAdminGroupCommand(plugin, builder).apply(commandManager);
        new PlotAdminCreateCommand(plugin, builder).apply(commandManager);
        new PlotAdminDeleteCommand(plugin, builder).apply(commandManager);
        new PlotAdminSetOwnerCommand(plugin, builder).apply(commandManager);
        new PlotAdminSetGroupCommand(plugin, builder).apply(commandManager);

        new PlotAdminReloadCommand(plugin, builder).apply(commandManager);
    }
}
