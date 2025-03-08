package de.unknowncity.plots.command;

import de.unknowncity.astralib.paper.api.command.PaperCommand;
import de.unknowncity.plots.PlotsPlugin;
import de.unknowncity.plots.service.PlotService;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;

import static org.incendo.cloud.parser.standard.StringParser.stringParser;

public class PlotAdminGroupCommand extends PaperCommand<PlotsPlugin> {

    private PlotService plotService;

    public PlotAdminGroupCommand(PlotsPlugin plugin) {
        super(plugin);
        this.plotService = plugin.serviceRegistry().getRegistered(PlotService.class);
    }

    @Override
    public void apply(CommandManager<CommandSender> commandManager) {
        commandManager.command(commandManager.commandBuilder("plotadmin", "padmin")
                .permission("ucplots.command.plotadmin")
                .literal("group")
                .literal("create")
                .required("group-name", stringParser())
                .handler(this::handleCreate)
        );

        commandManager.command(commandManager.commandBuilder("plotadmin", "padmin")
                .permission("ucplots.command.plotadmin")
                .literal("group")
                .literal("delete")
                .required("group-name", stringParser())
                .apply(plugin.confirmationManager())
                .handler(this::handleDelete)
        );

        commandManager.command(commandManager.commandBuilder("plotadmin", "padmin")
                .permission("ucplots.command.plotadmin")
                .literal("group")
                .literal("delete")
                .literal("confirm")
                .handler(plugin.confirmationManager().createExecutionHandler())
        );
    }

    private void handleCreate(CommandContext<CommandSender> commandContext) {
        var player = (Player) commandContext.sender();
        var groupName = commandContext.<String>get("group-name");

        plotService.createPlotGroup(groupName);

        player.sendMessage("Group created");
    }

    private void handleDelete(CommandContext<CommandSender> commandContext) {
        var player = (Player) commandContext.sender();
        var groupName = commandContext.<String>get("group-name");

        plotService.deletePlotGroup(groupName);

        player.sendMessage("Group deleted");
    }
}
