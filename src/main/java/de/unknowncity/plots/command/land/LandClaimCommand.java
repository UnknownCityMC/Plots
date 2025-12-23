package de.unknowncity.plots.command.land;

import de.unknowncity.plots.PlotsPlugin;
import de.unknowncity.plots.command.SubCommand;
import de.unknowncity.plots.service.PlotService;
import de.unknowncity.plots.service.RegionService;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;
import org.spongepowered.configurate.NodePath;

public class LandClaimCommand extends SubCommand {

    public LandClaimCommand(PlotsPlugin plugin, Command.Builder<CommandSender> builder) {
        super(plugin, builder);
    }

    @Override
    public void apply(CommandManager<CommandSender> commandManager) {
        commandManager.command(builder.literal("select")
                .permission("plots.command.land.claim")
                .senderType(Player.class)
                .handler(this::handleClaimLand)
                .build()
        );

        commandManager.command(builder.literal("buy")
                .permission("plots.command.land.claim")
                .senderType(Player.class)
                .handler(this::handleBuyLand)
        );
    }

    private void handleBuyLand(@NonNull CommandContext<Player> context) {
        var player = context.sender();
        var world = player.getWorld();
        if (!plugin.configuration().fb().freeBuildWorlds().contains(world.getName())) {
            plugin.messenger().sendMessage(player, NodePath.path("command", "land", "claim", "not-freebuild-world"));
            return;
        }

        var openEditSessionOpt = plugin.landEditSessionHandler().findEditSession(player);

        if (openEditSessionOpt.isPresent()) {
            openEditSessionOpt.get().complete();
            plugin.landEditSessionHandler().closeEditSession(player);
        } else {
            plugin.messenger().sendMessage(player, NodePath.path("command", "land", "buy", "not-found"));
        }
    }

    private void handleClaimLand(CommandContext<Player> commandContext) {
        var player = commandContext.sender();
        var world = player.getWorld();
        if (!plugin.configuration().fb().freeBuildWorlds().contains(world.getName())) {
            plugin.messenger().sendMessage(player, NodePath.path("command", "land", "claim", "not-freebuild-world"));
            return;
        }

        var openEditSessionOpt = plugin.landEditSessionHandler().findEditSession(player);

        if (openEditSessionOpt.isPresent()) {
            plugin.landEditSessionHandler().closeEditSession(player);
            return;
        }

        plugin.landEditSessionHandler().openSession(player);
        plugin.messenger().sendMessage(player, NodePath.path("command", "land", "claim", "enable"));
    }
}