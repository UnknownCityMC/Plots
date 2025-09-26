package de.unknowncity.plots.command.user;

import de.unknowncity.plots.PlotsPlugin;
import de.unknowncity.plots.command.SubCommand;
import de.unknowncity.plots.plot.PlotUtil;
import de.unknowncity.plots.plot.location.PlotLocation;
import de.unknowncity.plots.plot.location.PlotPosition;
import de.unknowncity.plots.service.EconomyService;
import de.unknowncity.plots.service.PlotService;
import de.unknowncity.plots.service.RegionService;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;
import org.spongepowered.configurate.NodePath;

import static org.incendo.cloud.parser.standard.EnumParser.enumParser;

public class PlotHomeVisibilityCommand extends SubCommand {
    private final RegionService regionService = plugin.serviceRegistry().getRegistered(RegionService.class);
    private final PlotService plotService = plugin.serviceRegistry().getRegistered(PlotService.class);
    private final EconomyService economyService = plugin.serviceRegistry().getRegistered(EconomyService.class);

    public PlotHomeVisibilityCommand(PlotsPlugin plugin, Command.Builder<CommandSender> builder) {
        super(plugin, builder);
    }

    private enum PlotHomeVisibility {
        PUBLIC, PRIVATE
    }

    @Override
    public void apply(CommandManager<CommandSender> commandManager) {
        commandManager.command(builder.literal("home")
                .literal("setvisibility")
                .required("visibility", enumParser(PlotHomeVisibility.class))
                .permission("plots.command.plot.unclaim")
                .senderType(Player.class)
                .handler(this::handleHomeSet)
                .build());
    }

    private void handleHomeSet(@NonNull CommandContext<Player> context) {
        var sender = context.sender();
        var visibility = (PlotHomeVisibility) context.get("visibility");

        PlotUtil.getPlotIfPresent(sender, plugin).ifPresentOrElse(plot -> {
            if (!PlotUtil.checkPlotSold(sender, plot, plugin)) {
                return;
            }

            if (!PlotUtil.checkPlotOwner(sender, plot, plugin)) {
                return;
            }

            var location = sender.getLocation();
            var oldHome = plot.plotHome();
            var newHome = PlotLocation.fromLocation(plot.id(), oldHome.name(), visibility == PlotHomeVisibility.PUBLIC, location);
            plot.plotHome(newHome);
            plotService.savePlot(plot);

            plugin.messenger().sendMessage(sender, NodePath.path("command", "plot", "home", "setvisibility", "success"), plot.tagResolvers(sender, plugin.messenger()));
        }, () -> plugin.messenger().sendMessage(sender, NodePath.path("command", "plot", "no-plot")));
    }
}