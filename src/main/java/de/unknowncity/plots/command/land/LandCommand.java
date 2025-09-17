package de.unknowncity.plots.command.land;

import de.unknowncity.astralib.paper.api.command.PaperCommand;
import de.unknowncity.plots.PlotsPlugin;
import org.bukkit.command.CommandSender;
import org.incendo.cloud.CommandManager;

public class LandCommand extends PaperCommand<PlotsPlugin> {
    public LandCommand(PlotsPlugin plugin) {
        super(plugin);
    }

    @Override
    public void apply(CommandManager<CommandSender> commandManager) {
        var builder = commandManager.commandBuilder("land");

        new LandClaimCommand(plugin, builder).apply(commandManager);
        new LandExpandCommand(plugin, builder).apply(commandManager);
    }
}
