package de.unknowncity.plots.plot.access;

import de.unknowncity.plots.plot.Plot;
import de.unknowncity.plots.plot.access.type.PlotAccessModifier;
import org.bukkit.entity.Player;

public class PlotAccessUtil {

    public static boolean hasAccess(Player player, PlotAccessModifier plotAccessModifier, Plot plot) {
        if (plotAccessModifier == PlotAccessModifier.NOBODY) {
            return false;
        }

        if (plotAccessModifier == PlotAccessModifier.EVERYBODY) {
            return true;
        }

        var plotMemberOpt = plot.findPlotMember(player.getUniqueId());

        if (plot.owner().equals(player.getUniqueId())) {
            return true;
        }

        return plotMemberOpt.filter(plotMember -> plotMember.plotMemberRole().ordinal() + 1 >= plotAccessModifier.ordinal()).isPresent();
    }
}
