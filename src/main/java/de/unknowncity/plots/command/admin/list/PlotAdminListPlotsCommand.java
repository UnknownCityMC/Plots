package de.unknowncity.plots.command.admin.list;

import de.unknowncity.plots.PlotsPlugin;
import de.unknowncity.plots.service.PlotService;
import de.unknowncity.plots.util.chat.PagedChatMenu;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.context.CommandContext;

public class PlotAdminListPlotsCommand extends PagedChatMenu {
    private final PlotService plotService;
    public PlotAdminListPlotsCommand(PlotsPlugin plugin, String commandBase) {
        super("plotlist", 10, plugin.messenger(), commandBase);
        this.plotService = plugin.serviceRegistry().getRegistered(PlotService.class);
    }

    @Override
    protected void handleCommand(@NonNull CommandContext<Player> context) {
        var page = context.getOrDefault("page", 1);

        var entries = plotService.plotCache().values().stream().map(PlotPagedEntry::new).toList();

        displayMenu(context.sender(), page, entries);
    }
}
