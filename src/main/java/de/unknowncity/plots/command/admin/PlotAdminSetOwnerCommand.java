package de.unknowncity.plots.command.admin;

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

import static org.incendo.cloud.bukkit.parser.PlayerParser.playerParser;
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
                .permission("ucplots.command.plotadmin")
                .required("target", playerParser())
                .flag(commandManager.flagBuilder("plotGroup").withComponent(stringParser()).build())
                .senderType(Player.class)
                .handler(this::setOwner)
                .build()
        );
    }

    private void setOwner(CommandContext<Player> commandContext) {
        var player = (Player) commandContext.get("target");
        var region = regionService.getSuitableRegion(player.getLocation());

        region.ifPresentOrElse(protectedRegion -> {
            var world = player.getWorld();

            if (!plotService.existsPlot(protectedRegion, world)) {
                plugin.messenger().sendMessage(player, NodePath.path("command", "plotadmin", "no-plot"));
                return;
            }

            plotService.setPlotOwner(player, plotService.getPlot(world, protectedRegion));
            plugin.messenger().sendMessage(player, NodePath.path("command", "plotadmin", "set-owner", "success"));

        }, () -> {
            plugin.messenger().sendMessage(player, NodePath.path("command", "plotadmin", "no-suitable-region"));
        });
    }
}