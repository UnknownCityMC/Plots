package de.unknowncity.plots.command.admin;

import de.unknowncity.plots.PlotsPlugin;
import de.unknowncity.plots.command.SubCommand;
import de.unknowncity.plots.service.backup.BackupService;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.command.CommandSender;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;
import org.spongepowered.configurate.NodePath;

public class PlotAdminBackupCommand extends SubCommand {
    private final BackupService backupService = plugin.serviceRegistry().getRegistered(BackupService.class);
    public PlotAdminBackupCommand(PlotsPlugin plugin, Command.Builder<CommandSender> builder) {
        super(plugin, builder);
    }

    @Override
    public void apply(CommandManager<CommandSender> commandManager) {
        commandManager.command(builder.literal("globalbackup")
                .literal("create")
                .handler(this::handleBackupCreate)
        );

        commandManager.command(builder.literal("globalbackup")
                .literal("load")
                .handler(this::handleBackupLoad)
        );
    }

    private void handleBackupCreate(@NonNull CommandContext<CommandSender> commandSenderCommandContext) {
        var player = commandSenderCommandContext.sender();
        plugin.messenger().sendMessage(player, NodePath.path("command", "plotadmin", "backup", "create", "starting"));

        var millisStart = System.currentTimeMillis();

        backupService.backupAllPlots();

        var millisEnd = System.currentTimeMillis();

        plugin.messenger().sendMessage(player, NodePath.path("command", "plotadmin", "backup", "create", "ended"),
                Placeholder.unparsed("time", String.valueOf(millisEnd - millisStart)));
    }

    private void handleBackupLoad(@NonNull CommandContext<CommandSender> commandSenderCommandContext) {
        var player = commandSenderCommandContext.sender();
        plugin.messenger().sendMessage(player, NodePath.path("command", "plotadmin", "backup", "load", "starting"));

        var millisStart = System.currentTimeMillis();

        backupService.loadAllBackups().whenComplete((amount, throwable) -> {
            var millisEnd = System.currentTimeMillis();

            plugin.messenger().sendMessage(player, NodePath.path("command", "plotadmin", "backup", "load", "ended"),
                    Placeholder.unparsed("time", String.valueOf(millisEnd - millisStart)),
                    Placeholder.unparsed("amount", String.valueOf(amount)));
        });
    }
}
