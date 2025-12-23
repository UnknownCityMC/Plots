package de.unknowncity.plots.command.admin.list;

import de.unknowncity.plots.PlotsPlugin;
import de.unknowncity.plots.command.SubCommand;
import de.unknowncity.plots.service.PlotService;
import de.unknowncity.plots.util.chat.PagedChatMenu;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;

import static org.incendo.cloud.parser.standard.IntegerParser.integerParser;

public class PlotAdminListPlotsCommand extends SubCommand {
    private final PlotService plotService = plugin.serviceRegistry().getRegistered(PlotService.class);

    public PlotAdminListPlotsCommand(PlotsPlugin plugin, Command.Builder<CommandSender> builder) {
        super(plugin, builder);
    }


    @Override
    public void apply(CommandManager<CommandSender> commandManager) {
        commandManager.command(builder.literal("list")
                .optional("page", integerParser(1))
                .senderType(Player.class)
                .handler(this::handle));
    }


    private void handle(@NonNull CommandContext<Player> context) {
        var page = context.getOrDefault("page", 1);

        var entries = plotService.plotCache().asMap().values().stream().map(PlotPagedEntry::new).toList();
        var menu = new PagedChatMenu("plotlist", 10, plugin, entries);

        menu.displayPage(context.sender(), page);
    }
}
