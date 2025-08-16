package de.unknowncity.plots.gui;

import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import xyz.xenondevs.invui.gui.PagedGui;

public class GuiPlaceholders {

    public static TagResolver[] paged(PagedGui<?> pagedGui) {
        var currentPage = pagedGui.getPage();
        return new TagResolver[]{
                Placeholder.parsed("next-page", String.valueOf(currentPage + 2)),
                Placeholder.parsed("prev-page", String.valueOf(currentPage)),
                Placeholder.parsed("curr-page", String.valueOf(currentPage + 1)),
                Placeholder.parsed("max-page", String.valueOf(pagedGui.getPageCount()))
        };
    }
}
