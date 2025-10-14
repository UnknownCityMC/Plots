package de.unknowncity.plots.command.admin;

import de.unknowncity.plots.Permissions;
import de.unknowncity.plots.PlotsPlugin;
import de.unknowncity.plots.command.SubCommand;
import de.unknowncity.plots.service.PlotService;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;
import org.spongepowered.configurate.NodePath;

import static org.incendo.cloud.parser.standard.StringParser.stringParser;

public class PlotAdminGroupCreateCommand extends SubCommand {

    private final PlotService plotService = plugin.serviceRegistry().getRegistered(PlotService.class);

    public PlotAdminGroupCreateCommand(PlotsPlugin plugin, Command.Builder<CommandSender> builder) {
        super(plugin, builder);
    }

    @Override
    public void apply(CommandManager<CommandSender> commandManager) {
        commandManager.command(builder.literal("group").literal("create")
                .permission(Permissions.COMMAND_PLOT_ADMIN)
                .required("group-name", stringParser())
                .handler(this::handleCreate)
                .build()
        );
    }

    private void handleCreate(CommandContext<CommandSender> commandContext) {
        var player = (Player) commandContext.sender();
        var groupName = commandContext.<String>get("group-name");

        if (plotService.existsGroup(groupName)) {
            plugin.messenger().sendMessage(player, NodePath.path("command", "plotadmin", "group", "create", "exists"));
            return;
        }

        plotService.createPlotGroup(groupName);

        plugin.messenger().sendMessage(player, NodePath.path("command", "plotadmin", "group", "create", "success"));
    }
}
