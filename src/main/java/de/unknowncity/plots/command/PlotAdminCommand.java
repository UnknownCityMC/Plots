package de.unknowncity.plots.command;

import de.unknowncity.astralib.paper.api.command.PaperCommand;
import de.unknowncity.astralib.paper.api.command.sender.PaperCommandSource;
import de.unknowncity.astralib.paper.api.command.sender.PaperPlayerCommandSource;
import de.unknowncity.plots.PlotsPlugin;
import de.unknowncity.plots.plot.PlotPaymentType;
import de.unknowncity.plots.service.PlotService;
import de.unknowncity.plots.service.RegionService;
import org.bukkit.entity.Player;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;
import org.spongepowered.configurate.NodePath;

import java.time.Duration;

import static org.incendo.cloud.parser.standard.DoubleParser.doubleParser;
import static org.incendo.cloud.parser.standard.DurationParser.durationParser;
import static org.incendo.cloud.parser.standard.EnumParser.enumParser;

public class PlotAdminCommand extends PaperCommand<PlotsPlugin> {

    public PlotAdminCommand(PlotsPlugin plugin) {
        super(plugin);
    }

    @Override
    public void apply(CommandManager<PaperCommandSource> commandManager) {
        commandManager.command(commandManager.commandBuilder("plotadmin", "padmin")
                .permission("ucplots.command.plotadmin")
                .senderType(PaperPlayerCommandSource.class)
                .literal("createfromexisting")
                .required("paymentType", enumParser(PlotPaymentType.class))
                .required("price", doubleParser())
                .flag(commandManager.flagBuilder("rentInterval").withComponent(durationParser()).build())
                .handler(this::handleCreate)
        );
    }

    private void handleCreate(CommandContext<PaperPlayerCommandSource> commandContext) {
        var player = (Player) commandContext.sender().platformCommandSender();
        var plotPaymentType = (PlotPaymentType) commandContext.get("paymentType");
        var price = (double) commandContext.get("price");
        var rentInterval = commandContext.flags().getValue("rentInterval", Duration.ofDays(30));

        var regionService = plugin.serviceRegistry().getRegistered(RegionService.class);
        var region = regionService.getSuitableRegion(player.getLocation());

        region.ifPresentOrElse(protectedRegion -> {
            var plotService = plugin.serviceRegistry().getRegistered(PlotService.class);
            var world = player.getWorld();

            if (plotService.existsPlot(protectedRegion, world)) {

                return;
            }

            var success = false;

            if (plotPaymentType == PlotPaymentType.SELL) {
                success = plotService.createSellPlotFromExisting(protectedRegion, world, price);
            }

            if (plotPaymentType == PlotPaymentType.RENT) {
                success = plotService.createRentPlotFromExisting(protectedRegion, world, price, rentInterval);
            }

            if (success) {
                plugin.messenger().sendMessage(player, NodePath.path("command", "plotadmin", "create", "success"));
            } else {
                plugin.messenger().sendMessage(player, NodePath.path("command", "plotadmin", "create", "error"));
            }
        }, () -> {
            plugin.messenger().sendMessage(player, NodePath.path("command", "plotadmin", "create", "no-suitable-region"));
        });
    }

    @Override
    public void startup(PlotsPlugin plotsPlugin) {

    }
}
