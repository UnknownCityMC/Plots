package de.unknowncity.plots.util.chat;

import de.unknowncity.plots.PlotsPlugin;
import de.unknowncity.plots.util.AstraArrays;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.entity.Player;
import org.spongepowered.configurate.NodePath;

import java.util.List;

public class PagedChatMenu {
    private final String id;
    private final int entriesPerPage;
    private final PlotsPlugin plugin;
    private final List<? extends PagedEntry> entries;


    public PagedChatMenu(
            String id,
            int entriesPerPage,
            PlotsPlugin plugin,
            List<? extends PagedEntry> entries
    ) {
        this.id = id;
        this.entriesPerPage = entriesPerPage;
        this.plugin = plugin;
        this.entries = entries;
    }

    public void displayPage(Player player, int page) {
        if (page < 1 || page > maxPage()) {
            plugin.messenger().sendMessage(player, NodePath.path("menu", "invalid-page"));
            return;
        }

        displayHeader(player, page);
        displayEntries(player, page);
        displayFooter(player, page);
    }

    private void displayHeader(Player player, int page) {
        var headerPath = NodePath.path("menu", id, "header");
        plugin.messenger().sendMessage(player, headerPath, placeholders(player, page, entries));
    }

    private void displayFooter(Player player, int page) {
        var footerPath = NodePath.path("menu", id, "footer");
        plugin.messenger().sendMessage(player, footerPath, placeholders(player, page, entries));
    }

    private void displayEntries(Player player, int page) {
        var entryPath = NodePath.path("menu", id, "entry");
        var startIndex = (page - 1) * entriesPerPage;
        int endIndex = Math.min(startIndex + entriesPerPage, entries.size() - 1);
        for (int i = startIndex; i <= endIndex; i++) {
            var entry = entries.get(i);
            plugin.messenger().sendMessage(player, entryPath,
                    AstraArrays.merge(
                            entry.tagResolvers(player, plugin.messenger()),
                            new TagResolver[]{Placeholder.parsed("index", String.valueOf(i + 1))})
            );
        }
    }


    private TagResolver[] placeholders(Player player, int page, List<? extends PagedEntry> entries) {
        return new TagResolver[]{
                Placeholder.parsed("max-page", String.valueOf(maxPage())),
                Placeholder.parsed("page", String.valueOf(page)),
                Placeholder.component("next", nextButton(player, page, entries)),
                Placeholder.component("prev", previousButton(player, page))
        };
    }

    private Component nextButton(Player player, int page, List<? extends PagedEntry> entries) {
        var nextPage = page + 1;
        var buttonPath = nextPage <= maxPage() ?
                NodePath.path("menu", "next-button") :
                NodePath.path("menu", "next-button-no-next");
        return plugin.messenger().component(player, buttonPath).clickEvent(
                ClickEvent.callback(audience -> this.displayPage(player, nextPage))
        );
    }

    private Component previousButton(Player player, int page) {
        var prevPage = page - 1;
        var buttonPath = prevPage > 0 ?
                NodePath.path("menu", "prev-button") :
                NodePath.path("menu", "prev-button-no-prev");

        return plugin.messenger().component(player, buttonPath).clickEvent(
                ClickEvent.callback(audience -> this.displayPage(player, prevPage))
        );
    }

    private int maxPage() {
        return (entries.size() + entriesPerPage - 1) / entriesPerPage;
    }
}