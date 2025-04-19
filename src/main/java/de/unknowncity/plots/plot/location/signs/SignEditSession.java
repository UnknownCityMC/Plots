package de.unknowncity.plots.plot.location.signs;

import de.unknowncity.plots.PlotsPlugin;
import de.unknowncity.plots.plot.Plot;
import de.unknowncity.plots.service.PlotService;
import org.bukkit.Location;

import java.util.LinkedList;
import java.util.List;

import static de.unknowncity.plots.plot.location.signs.SignManager.*;

public class SignEditSession {
    private final PlotsPlugin plugin;
    private Plot plot;
    private final List<PlotSign> plotSigns = new LinkedList<>();
    private final PlotService plotService;


    public SignEditSession(PlotsPlugin plugin) {
        this.plugin = plugin;
        this.plotService = plugin.serviceRegistry().getRegistered(PlotService.class);
    }

    public void finish() {
        plot.signs().forEach(plotSign -> {
            setOutline(plot, plotSign, false);
        });
    }

    public void setPlot(Plot plot) {
        this.plot = plot;
        open();
    }

    public boolean hasPlot() {
        return plot != null;
    }

    private void open() {
        plot.signs().forEach(plotSign -> {
            setOutline(plot, plotSign, true);
        });
    }

    public boolean addSign(Location location) {
        var sign = new PlotSign(location.x(), location.y(), location.z(), location.getYaw(), location.getPitch());

        if (plot.signs().stream().anyMatch(sign::equals)) {
            return false;
        }

        plot.signs().add(sign);
        setOutline(plot, sign, true);

        updateSings(plot, plugin.messenger());
        plotService.savePlot(plot);
        return true;
    }

    public boolean removeSign(Location location) {
        var ignored = new PlotSign(location.x(), location.y(), location.z(), location.getYaw(), location.getPitch());
        if (plot.signs().stream().anyMatch(plotSign -> plotSign.equals(ignored))) {
            setOutline(plot, ignored, false);
            clearSign(location);
        }

        var value = plot.signs().removeIf(plotSign -> plotSign.equals(ignored));
        plotService.savePlot(plot);
        return value;
    }
}