package de.unknowncity.plots.command.admin.list;

import de.unknowncity.astralib.paper.api.message.PaperMessenger;
import de.unknowncity.plots.plot.Plot;
import de.unknowncity.plots.util.chat.PagedEntry;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.entity.Player;

public class PlotPagedEntry extends PagedEntry {
    private final Plot plot;

    public PlotPagedEntry(Plot plot) {
        this.plot = plot;
    }

    @Override
    public TagResolver[] tagResolvers(Player player, PaperMessenger messenger) {
        return plot.tagResolvers(player, messenger);
    }
}
