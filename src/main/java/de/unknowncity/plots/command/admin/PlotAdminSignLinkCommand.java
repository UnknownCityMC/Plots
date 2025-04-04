package de.unknowncity.plots.command.admin;

import de.unknowncity.plots.PlotsPlugin;
import de.unknowncity.plots.command.SubCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;
import org.spongepowered.configurate.NodePath;

public class PlotAdminSignLinkCommand extends SubCommand {

    public PlotAdminSignLinkCommand(PlotsPlugin plugin, Command.Builder<CommandSender> builder) {
        super(plugin, builder);
    }

    @Override
    public void apply(CommandManager<CommandSender> commandManager) {
        commandManager.command(builder.literal("signLink")
                .permission("ucplots.command.plotadmin")
                .senderType(Player.class)
                .handler(this::signLink)
                .build()
        );
    }


    private void signLink(CommandContext<Player> commandContext) {
        var player = commandContext.sender();
        var uuid = player.getUniqueId();

        if (plugin.signLinkPlayers.containsKey(uuid)) {
            plugin.signLinkPlayers.remove(uuid);
            plugin.messenger().sendMessage(player, NodePath.path("command", "plotadmin", "sign", "disable"));
            return;
        }

        plugin.signLinkPlayers.put(uuid, null);
        plugin.messenger().sendMessage(player, NodePath.path("command", "plotadmin", "sign", "enable"));
    }
}
