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
import org.incendo.cloud.suggestion.Suggestion;
import org.spongepowered.configurate.NodePath;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;

import static org.incendo.cloud.parser.standard.DoubleParser.doubleParser;
import static org.incendo.cloud.parser.standard.DurationParser.durationParser;
import static org.incendo.cloud.parser.standard.StringParser.stringParser;

public class PlotAdminCreateCommand extends SubCommand {

    private final PlotService plotService = plugin.serviceRegistry().getRegistered(PlotService.class);
    private final RegionService regionService = plugin.serviceRegistry().getRegistered(RegionService.class);

    public PlotAdminCreateCommand(PlotsPlugin plugin, Command.Builder<CommandSender> builder) {
        super(plugin, builder);
    }

    @Override
    public void apply(CommandManager<CommandSender> commandManager) {
        commandManager.command(builder.literal("createBuyFromRegion")
                .permission("ucplots.command.plotadmin")
                .required("price", doubleParser())
                .flag(commandManager.flagBuilder("plotGroup").withComponent(stringParser()).build())
                .senderType(Player.class)
                .handler(this::handleCreateBuy)
                .build()
        );

        commandManager.command(builder.literal("createBuyFromRegionName")
                .permission("ucplots.command.plotadmin")
                .required("region", stringParser(), (sender, input) -> CompletableFuture.completedFuture(regionService.getAllRegions().keySet().stream().map(Suggestion::suggestion).toList()))
                .required("price", doubleParser())
                .flag(commandManager.flagBuilder("plotGroup").withComponent(stringParser()).build())
                .senderType(Player.class)
                .handler(this::handleCreateBuyName)
                .build()
        );

        commandManager.command(builder.literal("createRentFromRegion")
                .permission("ucplots.command.plotadmin")
                .required("price", doubleParser())
                .required("rentInterval", durationParser())
                .flag(commandManager.flagBuilder("plot-group").withComponent(stringParser()).build())
                .senderType(Player.class)
                .handler(this::handleCreateRent)
                .build()
        );

        commandManager.command(builder.literal("createRentFromRegionName")
                .permission("ucplots.command.plotadmin")
                .required("region", stringParser(), (sender, input) -> CompletableFuture.completedFuture(regionService.getAllRegions().keySet().stream().map(Suggestion::suggestion).toList()))
                .required("price", doubleParser())
                .required("rentInterval", durationParser())
                .flag(commandManager.flagBuilder("plot-group").withComponent(stringParser()).build())
                .senderType(Player.class)
                .handler(this::handleCreateRentName)
                .build()
        );

    }

    private void handleCreateBuy(CommandContext<Player> commandContext) {
        var player = commandContext.sender();
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
        }, () -> plugin.messenger().sendMessage(player, NodePath.path("command", "plotadmin", "no-suitable-region")));
    }

    private void handleCreateBuyName(CommandContext<Player> commandContext) {
        var player = commandContext.sender();
        var regionId = (String) commandContext.get("region");
        var price = (double) commandContext.get("price");
        var groupName = (String) commandContext.flags().get("plot-group");

        var world = player.getWorld();
        var protectedRegion = regionService.getRegion(regionId, world);

        if (protectedRegion == null) {
            plugin.messenger().sendMessage(player, NodePath.path("command", "plotadmin", "no-suitable-region"));
            return;
        }

        if (plotService.existsPlot(protectedRegion, world)) {
            plugin.messenger().sendMessage(player, NodePath.path("command", "plotadmin", "create", "aleady-exists"));
            return;
        }

        if (plotService.createBuyPlotFromRegion(protectedRegion, world, price, groupName)) {
            plugin.messenger().sendMessage(player, NodePath.path("command", "plotadmin", "create", "success"));
        } else {
            plugin.messenger().sendMessage(player, NodePath.path("command", "plotadmin", "create", "error"));
        }
    }

    private void handleCreateRent(CommandContext<Player> commandContext) {
        var player = commandContext.sender();
        var price = (double) commandContext.get("price");
        var groupName = (String) commandContext.flags().get("plot-group");

        var rentInterval = commandContext.flags().getValue("rentInterval", Duration.ofDays(30));

        var region = regionService.getSuitableRegion(player.getLocation());

        region.ifPresentOrElse(protectedRegion -> {
            var world = player.getWorld();

            if (plotService.existsPlot(protectedRegion, world)) {
                plugin.messenger().sendMessage(player, NodePath.path("command", "plotadmin", "create", "already-exists"));
                return;
            }

            if (plotService.createRentPlotFromRegion(protectedRegion, world, price, groupName, rentInterval)) {
                plugin.messenger().sendMessage(player, NodePath.path("command", "plotadmin", "create", "success"));
            } else {
                plugin.messenger().sendMessage(player, NodePath.path("command", "plotadmin", "create", "error"));
            }
        }, () -> plugin.messenger().sendMessage(player, NodePath.path("command", "plotadmin", "no-suitable-region")));
    }

    private void handleCreateRentName(CommandContext<Player> commandContext) {
        var player = commandContext.sender();
        var regionId = (String) commandContext.get("region");
        var price = (double) commandContext.get("price");
        var groupName = (String) commandContext.flags().get("plot-group");

        var rentInterval = commandContext.flags().getValue("rentInterval", Duration.ofDays(30));

        var world = player.getWorld();
        var protectedRegion = regionService.getRegion(regionId, world);

        if (protectedRegion == null) {
            plugin.messenger().sendMessage(player, NodePath.path("command", "plotadmin", "no-suitable-region"));
            return;
        }

        if (plotService.existsPlot(protectedRegion, world)) {
            plugin.messenger().sendMessage(player, NodePath.path("command", "plotadmin", "create", "already-exists"));
            return;
        }

        if (plotService.createRentPlotFromRegion(protectedRegion, world, price, groupName, rentInterval)) {
            plugin.messenger().sendMessage(player, NodePath.path("command", "plotadmin", "create", "success"));
        } else {
            plugin.messenger().sendMessage(player, NodePath.path("command", "plotadmin", "create", "error"));
        }
    }
}