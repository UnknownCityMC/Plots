package de.unknowncity.plots.command.user;

import de.unknowncity.astralib.paper.api.command.PaperCommand;
import de.unknowncity.plots.PlotsPlugin;
import de.unknowncity.plots.gui.ViewPlotsGUI;
import de.unknowncity.plots.service.PlotService;
import de.unknowncity.plots.service.RegionService;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;

import static de.unknowncity.plots.command.argument.UcPlayerParser.ucPlayerParser;

public class PlotsCommand extends PaperCommand<PlotsPlugin> {
    private final RegionService regionService = plugin.serviceRegistry().getRegistered(RegionService.class);
    private final PlotService plotService = plugin.serviceRegistry().getRegistered(PlotService.class);

    public PlotsCommand(PlotsPlugin plugin) {
        super(plugin);
    }

    @Override
    public void apply(CommandManager<CommandSender> commandManager) {

        commandManager.command(commandManager.commandBuilder("plots")
                .permission("plots.command.plots")
                .senderType(Player.class)
                .handler(this::handle)
        );

        commandManager.command(commandManager.commandBuilder("plots")
                .permission("plots.command.plots.other")
                .required("target", ucPlayerParser())
                .senderType(Player.class)
                .handler(this::handleOther)
        );
    }

    private void handle(@NonNull CommandContext<Player> context) {
        var sender = context.sender();

        ViewPlotsGUI.openSelf(sender, plugin);
    }

    private void handleOther(@NonNull CommandContext<Player> context) {
        var sender = context.sender();
        var target = (OfflinePlayer) context.get("target");

        ViewPlotsGUI.openForOther(sender, target, plugin);
    }
}
