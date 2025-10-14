package de.unknowncity.plots.event;

import de.unknowncity.plots.plot.model.Plot;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class PlotEvent extends Event {
    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final Plot plot;

    public PlotEvent(Plot plot) {
        this.plot = plot;
    }

    public Plot plot() {
        return plot;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLER_LIST;
    }
}
