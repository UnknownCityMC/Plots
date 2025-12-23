package de.unknowncity.plots;

import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class Permissions {
    public static final String BACKUP_VIEW_PROGRESS = "plots.backup.view-progress";
    public static final String BYPASS_ENTRY = "plots.bypass.entry";
    public static final String BYPASS_INTERACT = "plots.bypass.interact";
    public static final String COMMAND_PLOT_ADMIN = "plots.command.plotadmin";
    public static final String COMMAND_TELEPORT_OTHERS = "plots.command.plot.teleport.others";
    public static final String NOTIFY_BROKEN_PLOTS = "plots.notify.broken-plots";

    public static final Set<String> ALL_PERMISSIONS = Set.of(
            BYPASS_ENTRY,
            BYPASS_INTERACT,
            BACKUP_VIEW_PROGRESS,
            COMMAND_PLOT_ADMIN,
            COMMAND_TELEPORT_OTHERS,
            NOTIFY_BROKEN_PLOTS
    );
}
