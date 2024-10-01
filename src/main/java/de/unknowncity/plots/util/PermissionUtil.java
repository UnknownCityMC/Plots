package de.unknowncity.plots.util;

import de.unknowncity.plots.data.model.plot.group.PlotGroup;
import org.bukkit.entity.Player;

public class PermissionUtil {
    private static final String BASE_PERMISSION = "ucplots.maxplots.";

    public static int getMaxPlots(PlotGroup plotGroup, Player player) {
        var groupBasePermission = BASE_PERMISSION + plotGroup.name();
        return player.getEffectivePermissions()
                .stream()
                .filter(permission -> permission.getPermission().startsWith(groupBasePermission))
                .map(permission -> permission.getPermission().replace(groupBasePermission, ""))
                .mapToInt(Integer::parseInt)
                .findFirst()
                .orElse(0);
    }
}