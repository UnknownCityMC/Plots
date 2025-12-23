package de.unknowncity.plots.event;

import de.unknowncity.plots.plot.model.Plot;
import org.bukkit.entity.Player;

public class PlotPlayerEvent extends PlotEvent {
    private final Player player;

    public PlotPlayerEvent(Plot plot, Player player) {
        super(plot);
        this.player = player;
    }

    public Player player() {
        return player;
    }
}
