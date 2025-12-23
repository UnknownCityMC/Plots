package de.unknowncity.plots.command.land;

import de.unknowncity.plots.PlotsPlugin;
import de.unknowncity.plots.command.SubCommand;
import de.unknowncity.plots.plot.PlotExpandDirection;
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

public class LandExpandCommand extends SubCommand {

    private final PlotService plotService = plugin.serviceRegistry().getRegistered(PlotService.class);
    private final RegionService regionService = plugin.serviceRegistry().getRegistered(RegionService.class);
    private final EconomyService economyService = plugin.serviceRegistry().getRegistered(EconomyService.class);

    public LandExpandCommand(PlotsPlugin plugin, Command.Builder<CommandSender> builder) {
        super(plugin, builder);
    }

    @Override
    public void apply(CommandManager<CommandSender> commandManager) {
        commandManager.command(builder.literal("expand")
                .permission("plots.command.land.expand")
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

            if (!plotService.existsPlot(protectedRegion)) {
                plugin.messenger().sendMessage(player, NodePath.path("command", "land", "expand", "no-region"));
                return;
            }

            var plot = plotService.getPlot(protectedRegion);

            var price = regionService.calculateExpandArea(protectedRegion, direction, blocks, world) * plugin.configuration().fb().price() - plot.price();

            if (plot.owner() != null && !economyService.hasEnoughFunds(plot.owner().uuid(), price)) {
                plugin.messenger().sendMessage(player, NodePath.path("command", "land", "expand", "not-enough-money"), Placeholder.parsed("price", String.valueOf(price)));
                return;
            }

            if (regionService.expandRegionInDirection(protectedRegion, direction, blocks, world)) {
                var newSquareMeters = regionService.calculateAreaSquareMeters(regionService.getRegion(protectedRegion.getId(), player.getWorld()));
                var newPrice = newSquareMeters * plugin.configuration().fb().price();
                plotService.setPlotPrice(newPrice, plot);
                if (plot.owner() != null) {
                    economyService.withdraw(plot.owner().uuid(), price);
                }
                plugin.messenger().sendMessage(player, NodePath.path("command", "land", "expand", "success"));
            } else {
                plugin.messenger().sendMessage(player, NodePath.path("command", "land", "expand", "error"));
            }
        }, () -> plugin.messenger().sendMessage(player, NodePath.path("command", "plotadmin", "no-suitable-region")));
    }
}