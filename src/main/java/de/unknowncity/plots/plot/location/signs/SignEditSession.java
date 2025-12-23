package de.unknowncity.plots.plot.location.signs;

import de.unknowncity.plots.PlotsPlugin;
import de.unknowncity.plots.plot.model.Plot;
import de.unknowncity.plots.service.plot.SignService;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import static de.unknowncity.plots.plot.location.signs.SignManager.setLineTextEmpty;
import static de.unknowncity.plots.plot.location.signs.SignManager.updateSings;

public class SignEditSession {
    private final PlotsPlugin plugin;
    private Plot plot;
    private final SignService signService;
    private SignOutline outline;
    private final Player player;


    public SignEditSession(PlotsPlugin plugin, Player player) {
        this.plugin = plugin;
        this.signService = plugin.serviceRegistry().getRegistered(SignService.class);
        this.player = player;
    }

    public void finish() {
        if (plot == null) {
            return;
        }
        
        plot.signs().forEach(plotSign -> {
            outline.hideOutline();
        });
    }

    public void setPlot(Plot plot) {
        finish();
        this.plot = plot;
        open();
    }

    public boolean hasPlot() {
        return plot != null;
    }

    private void open() {
        outline = new SignOutline(player, plugin);
        plot.signs().forEach(plotSign -> {
            outline.showOutline(plot, plotSign);
        });
    }

    public boolean addSign(Location location) {

        if (plot.signs().stream().anyMatch(plotSign -> plotSign.isAt(location))) {
            return false;
        }

        var sign = signService.addSign(plot, location);
        outline.showOutline(plot, sign);
        updateSings(plot, plugin.messenger());
        return true;
    }

    public boolean removeSign(Location location) {
        var sign = plot.signs().stream().filter(plotSign -> plotSign.isAt(location)).findFirst();
        if (sign.isEmpty()) {
            return false;
        }

        outline.hideOutline(sign.get());
        setLineTextEmpty(location);

        signService.removeSign(plot, location);
        return true;
    }
}