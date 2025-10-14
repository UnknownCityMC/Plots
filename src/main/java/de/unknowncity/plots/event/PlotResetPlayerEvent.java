package de.unknowncity.plots.event;

import de.unknowncity.plots.plot.model.Plot;
import org.bukkit.entity.Player;

public class PlotResetPlayerEvent extends PlotPlayerEvent {
    public PlotResetPlayerEvent(Plot plot, Player player) {
        super(plot, player);
    }
}
