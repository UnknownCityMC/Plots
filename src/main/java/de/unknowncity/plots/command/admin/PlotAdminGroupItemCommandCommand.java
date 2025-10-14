package de.unknowncity.plots.command.admin;

import de.unknowncity.plots.Permissions;
import de.unknowncity.plots.PlotsPlugin;
import de.unknowncity.plots.command.SubCommand;
import de.unknowncity.plots.plot.group.PlotGroup;
import de.unknowncity.plots.service.PlotService;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;
import org.spongepowered.configurate.NodePath;

import static de.unknowncity.plots.command.argument.PlotGroupParser.plotGroupParser;

public class PlotAdminGroupItemCommandCommand extends SubCommand {

    private final PlotService plotService = plugin.serviceRegistry().getRegistered(PlotService.class);

    public PlotAdminGroupItemCommandCommand(PlotsPlugin plugin, Command.Builder<CommandSender> builder) {
        super(plugin, builder);
    }

    @Override
    public void apply(CommandManager<CommandSender> commandManager) {
        var itemCommandBuilder = builder.literal("group").literal("item")
                .permission(Permissions.COMMAND_PLOT_ADMIN)
                .required("group", plotGroupParser(plotService));

        commandManager.command(
                itemCommandBuilder.literal("set")
                        .handler(this::handleSet)
                        .build()
        );

        commandManager.command(
                itemCommandBuilder.literal("unset")
                        .handler(this::handleSet)
                        .build()
        );
    }

    private void handleSet(CommandContext<CommandSender> commandContext) {
        var player = (Player) commandContext.sender();
        var group = commandContext.<PlotGroup>get("group");

        var itemStack = player.getInventory().getItemInMainHand();

        if (itemStack.getType().isAir()) {
            plugin.messenger().sendMessage(player, NodePath.path("command", "plotadmin", "group", "setitem", "no-item"));
            return;
        }

        plotService.setPlotGroupDisplayItem(group, itemStack);

        plugin.messenger().sendMessage(player, NodePath.path("command", "plotadmin", "group", "setitem", "success"));
    }

    private void handleUnset(CommandContext<CommandSender> commandContext) {
        var player = (Player) commandContext.sender();
        var group = commandContext.<PlotGroup>get("group");

        plotService.unsetPlotGroupDisplayItem(group);
        plugin.messenger().sendMessage(player, NodePath.path("command", "plotadmin", "group", "unsetitem", "success"));
    }
}
