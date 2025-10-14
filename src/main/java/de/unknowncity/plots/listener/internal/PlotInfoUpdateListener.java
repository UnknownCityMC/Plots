package de.unknowncity.plots.listener.internal;

import de.unknowncity.plots.PlotsPlugin;
import de.unknowncity.plots.event.PlotInfoUpdateEvent;
import de.unknowncity.plots.plot.location.signs.SignManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class PlotInfoUpdateListener implements Listener {
    private final PlotsPlugin plugin;

    public PlotInfoUpdateListener(PlotsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlotInfoUpdate(PlotInfoUpdateEvent event) {
        SignManager.updateSings(event.plot(), plugin.messenger());
    }
}
