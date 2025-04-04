package de.unknowncity.plots.command;

import de.unknowncity.astralib.paper.api.command.PaperCommand;
import de.unknowncity.plots.PlotsPlugin;
import org.bukkit.command.CommandSender;
import org.incendo.cloud.Command;

public abstract class SubCommand extends PaperCommand<PlotsPlugin> {
    protected Command.Builder<CommandSender> builder;

    public SubCommand(PlotsPlugin plugin, Command.Builder<CommandSender> builder) {
        super(plugin);
        this.builder = builder;
    }
}
