package de.unknowncity.plots.command.user;

import de.unknowncity.plots.PlotsPlugin;
import de.unknowncity.plots.command.SubCommand;
import de.unknowncity.plots.plot.PlotUtil;
import de.unknowncity.plots.service.PlotService;
import de.unknowncity.plots.service.RegionService;
import de.unknowncity.plots.util.AstraArrays;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;
import org.spongepowered.configurate.NodePath;

import static de.unknowncity.plots.command.argument.UcPlayerParser.ucPlayerParser;

public class PlotUnDenyCommand extends SubCommand {
    private final RegionService regionService = plugin.serviceRegistry().getRegistered(RegionService.class);
    private final PlotService plotService = plugin.serviceRegistry().getRegistered(PlotService.class);

    public PlotUnDenyCommand(PlotsPlugin plugin, Command.Builder<CommandSender> builder) {
        super(plugin, builder);
    }

    @Override
    public void apply(CommandManager<CommandSender> commandManager) {
        commandManager.command(builder.literal("undeny")
                .permission("plots.command.plot.deny")
                .required("target", ucPlayerParser())
                .senderType(Player.class)
                .handler(this::handleDeny)
                .build());
    }

    private void handleDeny(@NonNull CommandContext<Player> context) {
        var sender = context.sender();
        var target = (OfflinePlayer) context.get("target");

        PlotUtil.getPlotIfPresent(sender, plugin).ifPresent(plot -> {
            if (!PlotUtil.checkPlotOwner(sender, plot, plugin)) {
                return;
            }

            if (!plot.isDenied(target.getUniqueId())) {
                plugin.messenger().sendMessage(sender, NodePath.path("command", "plot", "undeny", "not-denied"),
                        AstraArrays.merge(plot.tagResolvers(sender, plugin.messenger()), new TagResolver[]{Placeholder.unparsed("target", target.getName())}));
                return;
            }

            plot.deniedPlayers().removeIf(plotPlayer -> plotPlayer.uuid().equals(target.getUniqueId()));

            plotService.savePlot(plot);
            plugin.messenger().sendMessage(sender, NodePath.path("command", "plot", "undeny", "success"),
                    AstraArrays.merge(plot.tagResolvers(sender, plugin.messenger()), new TagResolver[]{Placeholder.unparsed("target", target.getName())}));
        });
    }
}