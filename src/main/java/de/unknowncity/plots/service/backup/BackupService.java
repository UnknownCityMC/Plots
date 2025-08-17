package de.unknowncity.plots.service.backup;

import de.unknowncity.astralib.common.service.Service;
import de.unknowncity.plots.PlotsPlugin;
import de.unknowncity.plots.plot.Plot;
import de.unknowncity.plots.plot.SchematicManager;
import de.unknowncity.plots.service.PlotService;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

public class BackupService extends Service<PlotsPlugin> {
    private final SchematicManager schematicManager;
    private final PlotService plotService;
    private final SchematicQueue schematicQueue;

    public BackupService(SchematicManager schematicManager, PlotService plotService) {
        this.schematicManager = schematicManager;
        this.plotService = plotService;
        this.schematicQueue = new SchematicQueue(plotService.plugin(), schematicManager);
    }

    public void backupAllPlots() {
        var viewers = Bukkit.getOnlinePlayers().stream().filter(player ->
                player.hasPermission("plots.backup.view-progress")).toList();
        var finishedPlots = 0;
        var plots = plotService.plotCache().values();
        var bossbar = BossBar.bossBar(getProgressNameCreate(finishedPlots, plots.size()), (float) finishedPlots / plots.size(), BossBar.Color.RED, BossBar.Overlay.PROGRESS );
        viewers.forEach(player -> player.showBossBar(bossbar));

        for (Plot plot : plots) {
            schematicManager.createSchematicBackup(plot);
            finishedPlots++;
            bossbar.name(getProgressNameCreate(finishedPlots, plots.size()));
            bossbar.progress((float) finishedPlots / plots.size());
        }

        viewers.forEach(player -> player.hideBossBar(bossbar));
    }

    public Component getProgressNameCreate(int finishedPlots, int totalPlots) {
        return MiniMessage.miniMessage().deserialize("<green>Plot backup in progress: <gray>(<yellow>" + finishedPlots + "<gray>/<yellow>" + totalPlots + "<gray>)");
    }

    public Component getProgressNameLoad(int finishedPlots, int totalPlots) {
        return MiniMessage.miniMessage().deserialize("<green>Loading Plot backup: <gray>(<yellow>" + finishedPlots + "<gray>/<yellow>" + totalPlots + "<gray>)");
    }

    public CompletableFuture<Integer> loadAllBackups() {
        var future = new CompletableFuture<Integer>();
        var viewers = Bukkit.getOnlinePlayers().stream().filter(player ->
                player.hasPermission("plots.backup.view-progress")).toList();
        AtomicInteger finishedPlots = new AtomicInteger();
        var plots = plotService.plotCache().values();
        var bossbar = BossBar.bossBar(getProgressNameLoad(finishedPlots.get(), plots.size()), (float) finishedPlots.get() / plots.size(), BossBar.Color.RED, BossBar.Overlay.PROGRESS );
        viewers.forEach(player -> player.showBossBar(bossbar));

        for (Plot plot : plots) {
            var operation = schematicManager.loadWorldEditSchematic(plot, schematicManager.getBackupGenericSchematicPath(plot));
            operation.ifPresent(schematicQueue::addSchematic);
        }

        schematicQueue.startPasting(1,() -> {
            finishedPlots.getAndIncrement();
            bossbar.name(getProgressNameCreate(finishedPlots.get(), plots.size()));
            bossbar.progress((float) finishedPlots.get() / plots.size());
        }).whenComplete((result, throwable) -> {
            future.complete(finishedPlots.get());
            viewers.forEach(player -> player.hideBossBar(bossbar));
        });
        return future;
    }
}
