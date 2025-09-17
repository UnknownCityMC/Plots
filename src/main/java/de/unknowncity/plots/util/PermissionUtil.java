package de.unknowncity.plots.util;

import org.bukkit.entity.Player;

public class PermissionUtil {

    public static int getPermValueInt(String perm, Player player) {
        return player.getEffectivePermissions()
                .stream()
                .filter(permission -> permission.getPermission().startsWith(perm))
                .map(permission -> permission.getPermission().replace(perm + ".", ""))
                .mapToInt(Integer::parseInt)
                .max()
                .orElse(0);
    }

    public static String getPermValueString(String perm, Player player) {
        return player.getEffectivePermissions()
                .stream()
                .filter(permission -> permission.getPermission().startsWith(perm))
                .map(permission -> permission.getPermission().replace(perm + ".", ""))
                .findFirst()
                .orElse("");
    }
}