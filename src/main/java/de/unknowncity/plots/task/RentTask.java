package de.unknowncity.plots.task;

import de.unknowncity.plots.PlotsPlugin;
import de.unknowncity.plots.plot.model.RentPlot;
import de.unknowncity.plots.plot.access.PlotState;
import de.unknowncity.plots.plot.location.signs.SignManager;
import de.unknowncity.plots.service.EconomyService;
import de.unknowncity.plots.service.PlotService;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.scheduler.BukkitTask;
import org.spongepowered.configurate.NodePath;

import java.time.LocalDateTime;

public class RentTask {
    private final PlotsPlugin plugin;
    private final PlotService plotService;
    private final EconomyService economyService;

    private BukkitTask task;


    public RentTask(PlotsPlugin plugin, PlotService plotService, EconomyService economyService) {
        this.plugin = plugin;
        this.plotService = plotService;
        this.economyService = economyService;
    }


    public void start() {
        this.task = plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            plotService.plotCache().asMap().values().stream()
                    .filter(plot -> plot.state() == PlotState.SOLD && plot instanceof RentPlot && plot.lastRentPayed().plusMinutes(((RentPlot) plot).rentIntervalInMin()).isAfter(LocalDateTime.now()))
                    .forEach(plot -> {
                        var price = plot.price();
                        var owner = plot.owner();
                        var player = plugin.getServer().getPlayer(owner.uuid());

                        if (!economyService.hasEnoughFunds(owner.uuid(), price)) {
                            if (player != null) {
                                if (plotService.backup(plot, owner.uuid())) {
                                    plotService.unClaimPlot(plot);
                                    plot.state(PlotState.UNAVAILABLE);
                                } else {
                                    plot.owner(null);
                                    plot.members(null);
                                    plot.state(PlotState.UNAVAILABLE);
                                }

                                plotService.savePlot(plot);
                                SignManager.updateSings(plot, plugin.messenger());

                                plugin.messenger().sendMessage(player, NodePath.path("task", "rent", "not-enough-money"), Placeholder.parsed("id", plot.id()));
                                return;
                            }
                        }

                        economyService.withdraw(owner.uuid(), price);

                        ((RentPlot) plot).lastRentPayed(LocalDateTime.now());
                        plotService.savePlot(plot);

                        if (player != null) {
                            plugin.messenger().sendMessage(player, NodePath.path("task", "rent", "success"), Placeholder.parsed("id", plot.id()), Placeholder.parsed("price", String.valueOf(price)));
                        }
                    });
        }, 20 * 60, 20 * 60 * 5);
    }

    public void restart() {
        task.cancel();
        start();
    }

    public void cancel() {
        task.cancel();
    }
}
