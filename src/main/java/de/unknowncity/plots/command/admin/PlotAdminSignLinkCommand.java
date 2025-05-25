package de.unknowncity.plots.command.admin;

import de.unknowncity.plots.PlotsPlugin;
import de.unknowncity.plots.command.SubCommand;
import de.unknowncity.plots.plot.location.signs.SignManager;
import de.unknowncity.plots.service.PlotService;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;
import org.spongepowered.configurate.NodePath;

public class PlotAdminSignLinkCommand extends SubCommand {
    private final SignManager signManager;

    public PlotAdminSignLinkCommand(PlotsPlugin plugin, Command.Builder<CommandSender> builder) {
        super(plugin, builder);
        this.signManager = plugin.serviceRegistry().getRegistered(PlotService.class).signManager();
    }

    @Override
    public void apply(CommandManager<CommandSender> commandManager) {
        commandManager.command(builder.literal("signLink")
                .permission("plots.command.plotadmin")
                .senderType(Player.class)
                .handler(this::signLink)
                .build()
        );
    }


    private void signLink(CommandContext<Player> commandContext) {
        var player = commandContext.sender();
        var uuid = player.getUniqueId();

        var possibleEditSession = signManager.findOpenEditSession(player);

        if (possibleEditSession.isPresent()) {
            signManager.closeEditSession(player);
            plugin.messenger().sendMessage(player, NodePath.path("command", "plotadmin", "sign", "disable"));
            return;
        }

        signManager.openEditSession(player);
        plugin.messenger().sendMessage(player, NodePath.path("command", "plotadmin", "sign", "enable"));
    }
}
