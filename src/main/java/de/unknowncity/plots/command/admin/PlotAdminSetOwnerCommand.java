package de.unknowncity.plots.command.admin;

import de.unknowncity.plots.PlotsPlugin;
import de.unknowncity.plots.command.SubCommand;
import de.unknowncity.plots.service.PlotService;
import de.unknowncity.plots.service.RegionService;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;
import org.spongepowered.configurate.NodePath;

import static de.unknowncity.plots.command.argument.UcPlayerParser.ucPlayerParser;
import static org.incendo.cloud.parser.standard.StringParser.stringParser;

public class PlotAdminSetOwnerCommand extends SubCommand {

    private final PlotService plotService = plugin.serviceRegistry().getRegistered(PlotService.class);
    private final RegionService regionService = plugin.serviceRegistry().getRegistered(RegionService.class);

    public PlotAdminSetOwnerCommand(PlotsPlugin plugin, Command.Builder<CommandSender> builder) {
        super(plugin, builder);
    }

    @Override
    public void apply(CommandManager<CommandSender> commandManager) {
        commandManager.command(builder.literal("setOwner")
                .permission("plots.command.plotadmin")
                .required("target", ucPlayerParser())
                .flag(commandManager.flagBuilder("plotGroup").withComponent(stringParser()).build())
                .senderType(Player.class)
                .handler(this::setOwner)
                .build()
        );
    }

    private void setOwner(CommandContext<Player> commandContext) {
        var target = (OfflinePlayer) commandContext.get("target");
        var player = commandContext.sender();
        var region = regionService.getSuitableRegion(player.getLocation());

        region.ifPresentOrElse(protectedRegion -> {
            if (!plotService.existsPlot(protectedRegion)) {
                plugin.messenger().sendMessage(player, NodePath.path("command", "plotadmin", "no-plot"));
                return;
            }

            plotService.setPlotOwner(target, plotService.getPlot(protectedRegion));
            plugin.messenger().sendMessage(player, NodePath.path("command", "plotadmin", "set-owner", "success"));

        }, () -> plugin.messenger().sendMessage(player, NodePath.path("command", "plotadmin", "no-suitable-region")));
    }
}