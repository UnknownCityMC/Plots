package de.unknowncity.plots.command.admin;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import de.unknowncity.plots.PlotsPlugin;
import de.unknowncity.plots.command.SubCommand;
import de.unknowncity.plots.command.argument.DoubleSuggestionProvider;
import de.unknowncity.plots.plot.group.PlotGroup;
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

import static de.unknowncity.plots.command.argument.PlotGroupParser.plotGroupParser;
import static de.unknowncity.plots.command.argument.RegionParserParser.regionParser;
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
                .permission("plots.command.plotadmin")
                .required("price", doubleParser(), DoubleSuggestionProvider.DOUBLE_SUGGESTION_PROVIDER)
                .required("group", plotGroupParser(plotService))
                .senderType(Player.class)
                .handler(this::handleCreateBuy)
                .build()
        );

        commandManager.command(builder.literal("createBuyFromRegionName")
                .permission("plots.command.plotadmin")
                .required("region", regionParser(regionService))
                .required("price", doubleParser(), DoubleSuggestionProvider.DOUBLE_SUGGESTION_PROVIDER)
                .required("group", plotGroupParser(plotService))
                .senderType(Player.class)
                .handler(this::handleCreateBuyName)
                .build()
        );

        commandManager.command(builder.literal("createRentFromRegion")
                .permission("plots.command.plotadmin")
                .required("price", doubleParser(), DoubleSuggestionProvider.DOUBLE_SUGGESTION_PROVIDER)
                .required("group", plotGroupParser(plotService))
                .required("rentInterval", durationParser())
                .senderType(Player.class)
                .handler(this::handleCreateRent)
                .build()
        );

        commandManager.command(builder.literal("createRentFromRegionName")
                .permission("plots.command.plotadmin")
                .required("region", regionParser(regionService))
                .required("price", doubleParser(), DoubleSuggestionProvider.DOUBLE_SUGGESTION_PROVIDER)
                .required("group", plotGroupParser(plotService))
                .required("rentInterval", durationParser())
                .senderType(Player.class)
                .handler(this::handleCreateRentName)
                .build()
        );

    }

    private void handleCreateBuy(CommandContext<Player> commandContext) {
        var player = commandContext.sender();
        var price = (double) commandContext.get("price");
        var group = (PlotGroup) commandContext.get("group");

        var region = regionService.getSuitableRegion(player.getLocation());

        region.ifPresentOrElse(protectedRegion -> {
            var world = player.getWorld();

            if (plotService.existsPlot(protectedRegion)) {
                plugin.messenger().sendMessage(player, NodePath.path("command", "plotadmin", "create", "already-exists"));
                return;
            }

            var plotOpt = plotService.createBuyPlotFromRegion(protectedRegion, world, price, group.name());

            if (plotOpt.isPresent()) {
                plugin.messenger().sendMessage(player, NodePath.path("command", "plotadmin", "create", "success"));
            } else {
                plugin.messenger().sendMessage(player, NodePath.path("command", "plotadmin", "create", "error"));
            }
        }, () -> plugin.messenger().sendMessage(player, NodePath.path("command", "plotadmin", "no-suitable-region")));
    }

    private void handleCreateBuyName(CommandContext<Player> commandContext) {
        var player = commandContext.sender();
        var protectedRegion = (ProtectedRegion) commandContext.get("region");
        var price = (double) commandContext.get("price");
        var group = (PlotGroup) commandContext.get("group");

        var world = player.getWorld();

        if (plotService.existsPlot(protectedRegion)) {
            plugin.messenger().sendMessage(player, NodePath.path("command", "plotadmin", "create", "already-exists"));
            return;
        }

        var plotOpt = plotService.createBuyPlotFromRegion(protectedRegion, world, price, group.name());

        if (plotOpt.isPresent()) {
            plugin.messenger().sendMessage(player, NodePath.path("command", "plotadmin", "create", "success"));
        } else {
            plugin.messenger().sendMessage(player, NodePath.path("command", "plotadmin", "create", "error"));
        }
    }

    private void handleCreateRent(CommandContext<Player> commandContext) {
        var player = commandContext.sender();
        var price = (double) commandContext.get("price");
        var group = (PlotGroup) commandContext.get("group");

        var rentInterval = commandContext.flags().getValue("rentInterval", Duration.ofDays(30));

        var region = regionService.getSuitableRegion(player.getLocation());

        region.ifPresentOrElse(protectedRegion -> {
            var world = player.getWorld();

            if (plotService.existsPlot(protectedRegion)) {
                plugin.messenger().sendMessage(player, NodePath.path("command", "plotadmin", "create", "already-exists"));
                return;
            }

            var plotOpt = plotService.createRentPlotFromRegion(protectedRegion, world, price, group.name(), rentInterval);

            if (plotOpt.isPresent()) {
                plugin.messenger().sendMessage(player, NodePath.path("command", "plotadmin", "create", "success"));
            } else {
                plugin.messenger().sendMessage(player, NodePath.path("command", "plotadmin", "create", "error"));
            }
        }, () -> plugin.messenger().sendMessage(player, NodePath.path("command", "plotadmin", "no-suitable-region")));
    }

    private void handleCreateRentName(CommandContext<Player> commandContext) {
        var player = commandContext.sender();
        var protectedRegion = (ProtectedRegion) commandContext.get("region");

        var price = (double) commandContext.get("price");
        var group = (PlotGroup) commandContext.get("group");

        var rentInterval = commandContext.flags().getValue("rentInterval", Duration.ofDays(30));

        var world = player.getWorld();

        if (plotService.existsPlot(protectedRegion)) {
            plugin.messenger().sendMessage(player, NodePath.path("command", "plotadmin", "create", "already-exists"));
            return;
        }

        var plotOpt = plotService.createRentPlotFromRegion(protectedRegion, world, price, group.name(), rentInterval);

        if (plotOpt.isPresent()) {
            plugin.messenger().sendMessage(player, NodePath.path("command", "plotadmin", "create", "success"));
        } else {
            plugin.messenger().sendMessage(player, NodePath.path("command", "plotadmin", "create", "error"));
        }
    }
}