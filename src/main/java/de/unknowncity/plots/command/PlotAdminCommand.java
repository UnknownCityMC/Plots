package de.unknowncity.plots.command;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import de.unknowncity.astralib.paper.api.command.PaperCommand;
import de.unknowncity.plots.PlotsPlugin;
import de.unknowncity.plots.data.model.plot.PlotPaymentType;
import de.unknowncity.plots.service.PlotService;
import de.unknowncity.plots.service.RegionService;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;
import org.spongepowered.configurate.NodePath;

import java.time.Duration;

import static org.incendo.cloud.parser.standard.DoubleParser.doubleParser;
import static org.incendo.cloud.parser.standard.DurationParser.durationParser;
import static org.incendo.cloud.parser.standard.EnumParser.enumParser;
import static org.incendo.cloud.parser.standard.StringParser.stringParser;

public class PlotAdminCommand extends PaperCommand<PlotsPlugin> {

    private PlotService plotService = plugin.serviceRegistry().getRegistered(PlotService.class);
    private RegionService regionService = plugin.serviceRegistry().getRegistered(RegionService.class);

    public PlotAdminCommand(PlotsPlugin plugin) {
        super(plugin);
    }

    @Override
    public void apply(CommandManager<CommandSender> commandManager) {

        var baseCommandBuilder = commandManager.commandBuilder("plotadmin", "padmin")
                .permission("ucplots.command.plotadmin")
                .senderType(Player.class);

        commandManager.command(baseCommandBuilder.literal("createBuyFromRegion")
                .required("paymentType", enumParser(PlotPaymentType.class))
                .required("price", doubleParser())
                .flag(commandManager.flagBuilder("plotGroup").withComponent(stringParser()).build())
                .handler(this::handleCreateBuy)
        );

        commandManager.command(baseCommandBuilder.literal("createRentFromRegion")
                .required("paymentType", enumParser(PlotPaymentType.class))
                .required("price", doubleParser())
                .required("rentInterval", durationParser())
                .flag(commandManager.flagBuilder("plot-group").withComponent(stringParser()).build())
                .handler(this::handleCreateRent)
        );

        commandManager.command(baseCommandBuilder.literal("delete")
                .required("id", stringParser())
                .handler(this::handleDelete)
        );
    }

    private void handleDelete(@NonNull CommandContext<Player> commandContext) {
        var player = commandContext.sender();
        var id = commandContext.<String>get("id");

        if (!plotService.existsPlot(id)) {
            plugin.messenger().sendMessage(player, NodePath.path("command", "plotadmin", "delete", "not-exists"));
            return;
        }

        plotService.deletePlot(id);
        plugin.messenger().sendMessage(player, NodePath.path("command", "plotadmin", "delete", "success"));
    }

    private void handleCreateBuy(CommandContext<Player> commandContext) {
        var player = commandContext.sender();
        var plotPaymentType = (PlotPaymentType) commandContext.get("paymentType");
        var price = (double) commandContext.get("price");
        var groupName = (String) commandContext.flags().get("plot-group");

        var region = regionService.getSuitableRegion(player.getLocation());

        region.ifPresentOrElse(protectedRegion -> {
            var world = player.getWorld();

            if (plotService.existsPlot(protectedRegion, world)) {
                plugin.messenger().sendMessage(player, NodePath.path("command", "plotadmin", "create", "aleady-exists"));
                return;
            }

            if (plotService.createBuyPlotFromRegion(protectedRegion, world, price, groupName)) {
                plugin.messenger().sendMessage(player, NodePath.path("command", "plotadmin", "create", "success"));
            } else {
                plugin.messenger().sendMessage(player, NodePath.path("command", "plotadmin", "create", "error"));
            }
        }, () -> {
            plugin.messenger().sendMessage(player, NodePath.path("command", "plotadmin", "create", "no-suitable-region"));
        });
    }

    private void handleCreateRent(CommandContext<Player> commandContext) {
        var player = commandContext.sender();
        var plotPaymentType = (PlotPaymentType) commandContext.get("paymentType");
        var price = (double) commandContext.get("price");
        var groupName = (String) commandContext.flags().get("plot-group");

        var rentInterval = commandContext.flags().getValue("rentInterval", Duration.ofDays(30));

        var region = regionService.getSuitableRegion(player.getLocation());

        region.ifPresentOrElse(protectedRegion -> {
            var world = player.getWorld();

            if (plotService.existsPlot(protectedRegion, world)) {
                plugin.messenger().sendMessage(player, NodePath.path("command", "plotadmin", "create", "aleady-exists"));
                return;
            }

            if (plotService.createRentPlotFromRegion(protectedRegion, world, price, groupName, rentInterval)) {
                plugin.messenger().sendMessage(player, NodePath.path("command", "plotadmin", "create", "success"));
            } else {
                plugin.messenger().sendMessage(player, NodePath.path("command", "plotadmin", "create", "error"));
            }
        }, () -> {
            plugin.messenger().sendMessage(player, NodePath.path("command", "plotadmin", "create", "no-suitable-region"));
        });
    }
}
