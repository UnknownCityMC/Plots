package de.unknowncity.plots.command.land;

import de.unknowncity.plots.PlotsPlugin;
import de.unknowncity.plots.command.SubCommand;
import de.unknowncity.plots.service.PlotService;
import de.unknowncity.plots.service.RegionService;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;
import org.spongepowered.configurate.NodePath;

public class LandClaimCommand extends SubCommand {

    private final PlotService plotService = plugin.serviceRegistry().getRegistered(PlotService.class);
    private final RegionService regionService = plugin.serviceRegistry().getRegistered(RegionService.class);

    public LandClaimCommand(PlotsPlugin plugin, Command.Builder<CommandSender> builder) {
        super(plugin, builder);
    }

    @Override
    public void apply(CommandManager<CommandSender> commandManager) {
        commandManager.command(builder.literal("claim")
                .permission("plots.command.land.claim")
                .senderType(Player.class)
                .handler(this::handleClaimLand)
                .build()
        );

    }

    private void handleClaimLand(CommandContext<Player> commandContext) {
        var player = commandContext.sender();

        var openEditSessionOpt = plugin.landEditSessionHandler().findEditSession(player);

        if (openEditSessionOpt.isPresent()) {
            openEditSessionOpt.get().complete();
            plugin.landEditSessionHandler().closeEditSession(player);
            plugin.messenger().sendMessage(player, NodePath.path("command", "land", "claim", "disable"));
            return;
        }

        plugin.landEditSessionHandler().openSession(player);
        plugin.messenger().sendMessage(player, NodePath.path("command", "land", "claim", "enable"));
    }
}