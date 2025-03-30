package de.unknowncity.plots.command.mod;

import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import de.unknowncity.plots.PlotsPlugin;
import de.unknowncity.plots.command.SubCommand;
import de.unknowncity.plots.data.model.plot.PlotLocations;
import de.unknowncity.plots.service.PlotService;
import de.unknowncity.plots.service.RegionService;
import de.unknowncity.plots.util.PlotId;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;
import org.spongepowered.configurate.NodePath;

import static org.incendo.cloud.parser.standard.StringParser.stringParser;

public class PlotModCreateCommand extends SubCommand {

    private final PlotService plotService = plugin.serviceRegistry().getRegistered(PlotService.class);
    private final RegionService regionService = plugin.serviceRegistry().getRegistered(RegionService.class);

    public PlotModCreateCommand(PlotsPlugin plugin, Command.Builder<CommandSender> builder) {
        super(plugin, builder);
    }

    @Override
    public void apply(CommandManager<CommandSender> commandManager) {
        commandManager.command(builder.literal("setLocations")
                .permission("ucplots.command.plotadmin")
                .senderType(Player.class)
                .handler(this::createPlot)
                .build()
        );
        commandManager.command(builder.literal("create")
                .permission("ucplots.command.plotmod")
                .required("id", stringParser())
                .flag(commandManager.flagBuilder("plotGroup").withComponent(stringParser()).build())
                .senderType(Player.class)
                .handler(this::handleCreateBuy)
                .build()
        );
        commandManager.command(builder.literal("expandLocationsVert")
                .permission("ucplots.command.plotmod")
                .senderType(Player.class)
                .handler(this::handleExpandVert)
                .build()
        );
    }

    private void handleExpandVert(CommandContext<Player> commandContext) {
        var player = commandContext.sender();
        var uuid = player.getUniqueId();

        if (!plugin.createPlotPlayers.containsKey(uuid)) {
            plugin.messenger().sendMessage(player, NodePath.path("command", "plotmod", "create", "set-locations"));
            return;
        }

        var plotLocations = plugin.createPlotPlayers.get(uuid);

        if (plotLocations.loc1() == null) {
            plugin.messenger().sendMessage(player, NodePath.path("command", "plotmod", "create", "loc1-empty"));
            return;
        }

        if (plotLocations.loc2() == null) {
            plugin.messenger().sendMessage(player, NodePath.path("command", "plotmod", "create", "loc2-empty"));
            return;
        }

        var plotLoc1 = plotLocations.loc1();
        var plotLoc2 = plotLocations.loc2();

        plotLoc1.setY(-64);
        plotLoc2.setY(320);

        plugin.createPlotPlayers.put(uuid, new PlotLocations(plotLoc1, plotLoc2));
        plugin.messenger().sendMessage(player, NodePath.path("command", "plotmod", "create", "expand"));
    }

    private void createPlot(CommandContext<Player> commandContext) {
        var player = commandContext.sender();
        var uuid = player.getUniqueId();

        if (plugin.createPlotPlayers.containsKey(uuid)) {
            plugin.createPlotPlayers.remove(uuid);
            plugin.messenger().sendMessage(player, NodePath.path("command", "plotmod", "create", "disable"));
            return;
        }

        plugin.createPlotPlayers.put(uuid, new PlotLocations(null, null));
        plugin.messenger().sendMessage(player, NodePath.path("command", "plotmod", "create", "enable"));
    }

    private void handleCreateBuy(CommandContext<Player> commandContext) {
        var player = commandContext.sender();
        var uuid = player.getUniqueId();
        var id = (String) commandContext.get("id");
        var groupName = (String) commandContext.flags().get("plot-group");

        var world = player.getWorld();


        if (regionService.getRegion(id, world) != null) {
            plugin.messenger().sendMessage(player, NodePath.path("command", "plotmod", "create", "id-exists"));
            return;
        }

        if (plotService.existsPlot(PlotId.generate(world, id))) {
            plugin.messenger().sendMessage(player, NodePath.path("command", "plotmod", "create", "id-exists"));
            return;
        }

        if (!plugin.createPlotPlayers.containsKey(uuid)) {
            plugin.messenger().sendMessage(player, NodePath.path("command", "plotmod", "create", "set-locations"));
            return;
        }

        var plotLocations = plugin.createPlotPlayers.get(uuid);

        if (plotLocations.loc1() == null) {
            plugin.messenger().sendMessage(player, NodePath.path("command", "plotmod", "create", "loc1-empty"));
            return;
        }

        if (plotLocations.loc2() == null) {
            plugin.messenger().sendMessage(player, NodePath.path("command", "plotmod", "create", "loc2-empty"));
            return;
        }

        var plotLoc1 = plotLocations.loc1();
        var plotLoc2 = plotLocations.loc2();

        var loc1 = new BlockVector3(plotLoc1.getBlockX(), plotLoc1.getBlockY(), plotLoc1.getBlockZ());
        var loc2 = new BlockVector3(plotLoc2.getBlockX(), plotLoc2.getBlockY(), plotLoc2.getBlockZ());

        var regionExist = regionService.doesRegionExistBetweenLocations(world, loc1, loc2);

        if (regionExist) {
            plugin.messenger().sendMessage(player, NodePath.path("command", "plotmod", "create", "already-exists"));
            return;
        }

        ProtectedRegion protectedRegion = regionService.createRegionFromLocations(world, loc1, loc2, id);

        var price = regionService.calculateAreaSquareMeters(protectedRegion) * plugin.configuration().fb().price();

        if (plotService.createBuyPlotFromRegion(protectedRegion, world, price, groupName)) {
            plugin.createPlotPlayers.remove(uuid);
            plugin.messenger().sendMessage(player, NodePath.path("command", "plotmod", "create", "disable"));
            plugin.messenger().sendMessage(player, NodePath.path("command", "plotmod", "create", "success"));
        } else {
            plugin.messenger().sendMessage(player, NodePath.path("command", "plotmod", "create", "error"));
        }

    }
}