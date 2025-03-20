package de.unknowncity.plots.command.mod;

import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import de.unknowncity.plots.PlotsPlugin;
import de.unknowncity.plots.command.SubCommand;
import de.unknowncity.plots.service.PlotService;
import de.unknowncity.plots.service.RegionService;
import de.unknowncity.plots.util.PlotId;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;
import org.spongepowered.configurate.NodePath;

import static org.incendo.cloud.parser.standard.IntegerParser.integerParser;
import static org.incendo.cloud.parser.standard.StringParser.stringParser;

public class PlotModCreateCommand extends SubCommand {

    private final PlotService plotService = plugin.serviceRegistry().getRegistered(PlotService.class);
    private final RegionService regionService = plugin.serviceRegistry().getRegistered(RegionService.class);

    public PlotModCreateCommand(PlotsPlugin plugin, Command.Builder<CommandSender> builder) {
        super(plugin, builder);
    }

    @Override
    public void apply(CommandManager<CommandSender> commandManager) {
        commandManager.command(builder.literal("create")
                .permission("ucplots.command.plotmod")
                .required("id", stringParser())
                .required("x1", integerParser())
                .required("z1", integerParser())
                .required("x2", integerParser())
                .required("z2", integerParser())
                .senderType(Player.class)
                .handler(this::handleCreateBuy)
                .build()
        );
    }

    private void handleCreateBuy(CommandContext<Player> commandContext) {
        var player = commandContext.sender();
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

        var x1 = (int) commandContext.get("x1");
        var z1 = (int) commandContext.get("z1");
        var x2 = (int) commandContext.get("x2");
        var z2 = (int) commandContext.get("z2");

        var loc1 = new BlockVector3(x1, -64, z1);
        var loc2 = new BlockVector3(x2, 319, z2);

        var regionExist = regionService.doesRegionExistBetweenLocations(world, loc1, loc2);

        if (regionExist) {
            plugin.messenger().sendMessage(player, NodePath.path("command", "plotmod", "create", "already-exists"));
            return;
        }

        ProtectedRegion protectedRegion = regionService.createRegionFromLocations(world, loc1, loc2, id);

        var price = regionService.calculateAreaSquareMeters(protectedRegion) * plugin.configuration().fb().price();

        if (plotService.createBuyPlotFromRegion(protectedRegion, world, price, groupName)) {
            plugin.messenger().sendMessage(player, NodePath.path("command", "plotmod", "create", "success"));
        } else {
            plugin.messenger().sendMessage(player, NodePath.path("command", "plotmod", "create", "error"));
        }

    }
}