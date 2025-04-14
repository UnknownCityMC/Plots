package de.unknowncity.plots.util.chat;

import de.unknowncity.astralib.paper.api.message.PaperMessenger;
import de.unknowncity.plots.util.AstraArrays;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;
import org.spongepowered.configurate.NodePath;

import java.util.List;

import static org.incendo.cloud.parser.standard.IntegerParser.integerParser;

public abstract class PagedChatMenu {
    private final String id;
    private final int entriesPerPage;
    private final PaperMessenger messenger;
    private final String commandBase;

    public PagedChatMenu(
            String id,
            int entriesPerPage,
            PaperMessenger messenger,
            String commandBase
    ) {
        this.id = id;
        this.entriesPerPage = entriesPerPage;
        this.messenger = messenger;
        this.commandBase = commandBase;
    }

    public PagedChatMenu(
            String id,
            int entriesPerPage,
            PaperMessenger messenger
    ) {
        this.id = id;
        this.entriesPerPage = entriesPerPage;
        this.messenger = messenger;
        this.commandBase = id;
    }

    public void applyCommand(Command.Builder<CommandSender> base, CommandManager<CommandSender> commandManager) {
        commandManager.command(base
                .optional("page", integerParser(1))
                .permission("astralib.pagedmenu." + id)
                .senderType(Player.class)
                .handler(this::handleCommand)
        );
    }

    protected abstract void handleCommand(@NonNull CommandContext<Player> context);

    public void displayMenu(Player player, int page, List<? extends PagedEntry> pagedEntries) {
        displayHeader(player, page, pagedEntries);
        displayEntries(player, page, pagedEntries);
        displayFooter(player, page, pagedEntries);
    }

    private void displayHeader(Player player, int page, List<? extends PagedEntry> entries) {
        var headerPath = NodePath.path("menu", id, "header");
        messenger.sendMessage(player, headerPath, placeholders(player, page, entries));
    }

    private void displayFooter(Player player, int page, List<? extends PagedEntry> entries) {
        var footerPath = NodePath.path("menu", id, "footer");
        messenger.sendMessage(player, footerPath, placeholders(player, page, entries));
    }

    private void displayEntries(Player player, int page, List<? extends PagedEntry> entries) {
        var entryPath = NodePath.path("menu", id, "entry");
        var startIndex = (page - 1) * entriesPerPage;
        int endIndex = Math.min(startIndex + entriesPerPage, entries.size() - 1);
        for (int i = startIndex; i <= endIndex; i++) {
            var entry = entries.get(i);
            messenger.sendMessage(player, entryPath, AstraArrays.merge(entry.tagResolvers(player, messenger), new TagResolver[] { Placeholder.parsed("index", String.valueOf(i + 1)) }));
        }
    }


    private TagResolver[] placeholders(Player player, int page, List<? extends PagedEntry> entries) {
        return new TagResolver[] {
                Placeholder.parsed("max-page", String.valueOf(maxPage(entries))),
                Placeholder.parsed("page", String.valueOf(page)),
                Placeholder.component("next", nextButton(player, page, entries)),
                Placeholder.component("prev", previousButton(player, page))
        };
    }

    private Component nextButton(Player player, int page, List<? extends PagedEntry> entries) {
        var nextPage = page + 1;
        var buttonPath = nextPage <= maxPage(entries) ?
                NodePath.path("menu", id, "next-button") :
                NodePath.path("menu", id, "next-button-no-next");
        return messenger.component(player, buttonPath).clickEvent(
                ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/" + commandBase + " " + nextPage)
        );
    }

    private Component previousButton(Player player, int page) {
        var prevPage = page - 1;
        var buttonPath = prevPage > 0 ?
                NodePath.path("menu", id, "prev-button") :
                NodePath.path("menu", id, "prev-button-no-prev");

        return messenger.component(player, buttonPath).clickEvent(
                ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/" + commandBase + " " + prevPage)
        );
    }

    private int maxPage(List<? extends PagedEntry> entries) {
        return (entries.size() + entriesPerPage - 1) / entriesPerPage;
    }
}