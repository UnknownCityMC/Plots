package de.unknowncity.plots.command.admin;

import de.unknowncity.plots.Permissions;
import de.unknowncity.plots.PlotsPlugin;
import de.unknowncity.plots.command.SubCommand;
import de.unknowncity.plots.service.EconomyService;
import de.unknowncity.plots.service.PlotService;
import de.unknowncity.plots.service.RegionService;
import de.unknowncity.plots.service.backup.BackupService;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;
import org.spongepowered.configurate.NodePath;

import static org.incendo.cloud.bukkit.parser.PlayerParser.playerParser;

public class PlotAdminLoadBackupCommand extends SubCommand {
    private final PlotService plotService = plugin.serviceRegistry().getRegistered(PlotService.class);
    private final BackupService backupService = plugin.serviceRegistry().getRegistered(BackupService.class);
    private final RegionService regionService = plugin.serviceRegistry().getRegistered(RegionService.class);
    private final EconomyService economyService = plugin.serviceRegistry().getRegistered(EconomyService.class);

    public PlotAdminLoadBackupCommand(PlotsPlugin plugin, Command.Builder<CommandSender> builder) {
        super(plugin, builder);
    }

    @Override
    public void apply(CommandManager<CommandSender> commandManager) {
        commandManager.command(builder.literal("loadBackup")
                .permission(Permissions.COMMAND_PLOT_ADMIN)
                .required("target", playerParser())
                .senderType(Player.class)
                .handler(this::handleLoadBackup)
        );
    }

    private void handleLoadBackup(@NonNull CommandContext<Player> context) {
        var player = context.sender();
        var target = (Player) context.get("target");
        var region = regionService.getSuitableRegion(player.getLocation());

        region.ifPresentOrElse(protectedRegion -> {
            if (!plotService.existsPlot(protectedRegion)) {
                plugin.messenger().sendMessage(player, NodePath.path("command", "plotadmin", "no-plot"));
                return;
            }

            var plot = plotService.getPlot(protectedRegion);

            if (!backupService.hasBackup(plot, target.getUniqueId())) {
                plugin.messenger().sendMessage(player, NodePath.path("command", "plotadmin", "load-backup", "not-found"));
                return;
            }

            if (!economyService.hasEnoughFunds(target.getUniqueId(), plot.price())) {
                plugin.messenger().sendMessage(player, NodePath.path("command", "plotadmin", "load-backup", "not-enough-money"));
                return;
            }

            backupService.loadBackupForPlayer(plot, target);
            plotService.claimPlot(target, plot);
        }, () -> plugin.messenger().sendMessage(player, NodePath.path("command", "plotadmin", "no-suitable-region")));
    }
}
