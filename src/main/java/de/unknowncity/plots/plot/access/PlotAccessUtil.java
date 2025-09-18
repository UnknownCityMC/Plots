package de.unknowncity.plots.plot.access;

import de.unknowncity.plots.plot.model.Plot;
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

        if (plot.isOwner(player.getUniqueId())) {
            return true;
        }

        var plotMemberOpt = plot.findPlotMember(player.getUniqueId());

        if (plotMemberOpt.isEmpty()) {
            return false;
        }

        var member = plotMemberOpt.get();

        return member.role().ordinal() < plotAccessModifier.ordinal();
    }
}
