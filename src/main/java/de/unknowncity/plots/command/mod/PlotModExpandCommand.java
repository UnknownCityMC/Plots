package de.unknowncity.plots.command.mod;

import de.unknowncity.plots.PlotsPlugin;
import de.unknowncity.plots.command.SubCommand;
import de.unknowncity.plots.data.model.plot.PlotExpandDirection;
import de.unknowncity.plots.service.EconomyService;
import de.unknowncity.plots.service.PlotService;
import de.unknowncity.plots.service.RegionService;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;
import org.spongepowered.configurate.NodePath;

import static org.incendo.cloud.parser.standard.EnumParser.enumParser;
import static org.incendo.cloud.parser.standard.IntegerParser.integerParser;

public class PlotModExpandCommand extends SubCommand {

    private final PlotService plotService = plugin.serviceRegistry().getRegistered(PlotService.class);
    private final RegionService regionService = plugin.serviceRegistry().getRegistered(RegionService.class);
    private final EconomyService economyService = plugin.serviceRegistry().getRegistered(EconomyService.class);

    public PlotModExpandCommand(PlotsPlugin plugin, Command.Builder<CommandSender> builder) {
        super(plugin, builder);
    }

    @Override
    public void apply(CommandManager<CommandSender> commandManager) {
        commandManager.command(builder.literal("expand")
                .permission("ucplots.command.plotmod")
                .required("direction", enumParser(PlotExpandDirection.class))
                .required("blocks", integerParser())
                .senderType(Player.class)
                .handler(this::handleExpand)
                .build()
        );
    }

    private void handleExpand(CommandContext<Player> commandContext) {
        var player = commandContext.sender();
        var direction = (PlotExpandDirection) commandContext.get("direction");
        var blocks = (int) commandContext.get("blocks");

        var region = regionService.getSuitableRegion(player.getLocation());

        region.ifPresentOrElse(protectedRegion -> {
            var world = player.getWorld();

            if (!plotService.existsPlot(protectedRegion, world)) {
                plugin.messenger().sendMessage(player, NodePath.path("command", "plotmod", "expand", "no-region"));
                return;
            }

            var plot = plotService.getPlot(world, protectedRegion);

            var price = plot.price() - regionService.expandRegionInDirectionBlockCount(protectedRegion, direction, blocks, world) * plugin.configuration().fb().price();

            if (plot.owner() != null && !economyService.hasEnoughFunds(plot.owner(), price)) {
                plugin.messenger().sendMessage(player, NodePath.path("command", "plotmod", "expand", "not-enough-money"), Placeholder.parsed("price", String.valueOf(price)));
                return;
            }

            if (regionService.expandRegionInDirection(protectedRegion, direction, blocks, world)) {
                var newSquareMeters = regionService.calculateAreaSquareMeters(regionService.getRegion(protectedRegion.getId(), player.getWorld()));
                var newPrice = newSquareMeters * plugin.configuration().fb().price();
                plotService.setPlotPrice(newPrice, plot);
                if (plot.owner() != null) {
                    economyService.deposit(plot.owner(), price);
                }
                plugin.messenger().sendMessage(player, NodePath.path("command", "plotmod", "expand", "success"));
            } else {
                plugin.messenger().sendMessage(player, NodePath.path("command", "plotmod", "expand", "error"));
            }
        }, () -> plugin.messenger().sendMessage(player, NodePath.path("command", "plotadmin", "no-suitable-region")));
    }
}