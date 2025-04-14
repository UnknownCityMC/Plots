package de.unknowncity.plots.util.chat;

import de.unknowncity.astralib.paper.api.message.PaperMessenger;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.entity.Player;

public abstract class PagedEntry {

    public abstract TagResolver[] tagResolvers(Player player, PaperMessenger messenger);
}
