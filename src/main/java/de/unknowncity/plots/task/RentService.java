package de.unknowncity.plots.task;

import de.unknowncity.astralib.common.service.Service;
import de.unknowncity.plots.PlotsPlugin;
import de.unknowncity.plots.plot.model.RentPlot;
import de.unknowncity.plots.plot.access.PlotState;
import de.unknowncity.plots.service.EconomyService;
import de.unknowncity.plots.service.PlotService;
import de.unknowncity.plots.service.backup.BackupService;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;
import org.spongepowered.configurate.NodePath;

import java.time.LocalDateTime;

public class RentService extends Service<PlotsPlugin> {
    private final PlotsPlugin plugin;
    private final PlotService plotService;
    private final EconomyService economyService;

    private BukkitTask task;
    private static final int INTERVAL = 20 * 60;


    public RentService(PlotsPlugin plugin) {
        this.plugin = plugin;
        this.plotService = plugin.serviceRegistry().getRegistered(PlotService.class);
        this.economyService = plugin.serviceRegistry().getRegistered(EconomyService.class);
    }

    @Override
    public void startup() {
        this.task = plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            plotService.plotCache().asMap().values().stream()
                    .filter(plot -> plot.state() == PlotState.SOLD && plot instanceof RentPlot)
                    .map(plot -> (RentPlot) plot)
                    .filter(plot -> plot.lastRentPayed().plusMinutes(plot.rentIntervalInMin()).isBefore(LocalDateTime.now()))
                    .forEach(plot -> {
                        var price = plot.price();
                        var owner = plot.owner();

                        var player = Bukkit.getPlayer(owner.uuid());

                        if (!economyService.hasEnoughFunds(owner.uuid(), price)) {
                            var backupService = plugin.serviceRegistry().getRegistered(BackupService.class);
                            if (backupService.backupBoundToPlayer(plot, owner.uuid())) {
                                Bukkit.getScheduler().runTask(plugin, () -> plotService.resetPlot(plot));
                            } else {
                                plot.owner(null);
                                plot.members().clear();
                                plot.state(PlotState.UNAVAILABLE);
                            }
                            if (player != null) {
                                plugin.messenger().sendMessage(player, NodePath.path("task", "rent", "overdue"),
                                        Placeholder.parsed("id", plot.id()), Placeholder.parsed("price", String.valueOf(price))
                                );
                            }
                        } else {
                            economyService.withdraw(owner.uuid(), price);

                            plot.lastRentPayed(LocalDateTime.now());
                            plotService.savePlot(plot);
                            if (player != null) {
                                plugin.messenger().sendMessage(player, NodePath.path("task", "rent", "success"),
                                        Placeholder.parsed("id", plot.id()), Placeholder.parsed("price", String.valueOf(price))
                                );
                            }
                        }
                    });
        }, INTERVAL, INTERVAL);
    }

    @Override
    public void shutdown() {
        task.cancel();
    }
}
