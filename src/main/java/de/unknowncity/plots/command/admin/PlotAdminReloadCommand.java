package de.unknowncity.plots.command.admin;

import de.unknowncity.plots.PlotsPlugin;
import de.unknowncity.plots.command.SubCommand;
import org.bukkit.command.CommandSender;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;
import org.spongepowered.configurate.NodePath;

public class PlotAdminReloadCommand extends SubCommand {
    public PlotAdminReloadCommand(PlotsPlugin plugin, Command.Builder<CommandSender> builder) {
        super(plugin, builder);
    }

    @Override
    public void apply(CommandManager<CommandSender> commandManager) {
        commandManager.command(builder.literal("reload")
                .permission("plots.command.plotadmin")
                .handler(this::handleReload)
        );
    }

    private void handleReload(@NonNull CommandContext<CommandSender> context) {
        plugin.onPluginReload();
        plugin.messenger().sendMessage(context.sender(), NodePath.path("command", "plotadmin", "reload"));
    }
}
