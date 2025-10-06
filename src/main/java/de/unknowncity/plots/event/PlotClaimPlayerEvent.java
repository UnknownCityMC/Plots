package de.unknowncity.plots.event;

import de.unknowncity.plots.plot.model.Plot;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

public class PlotClaimPlayerEvent extends PlotPlayerEvent implements Cancellable {

    public PlotClaimPlayerEvent(Plot plot, Player player) {
        super(plot, player);
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public void setCancelled(boolean cancel) {

    }
}
