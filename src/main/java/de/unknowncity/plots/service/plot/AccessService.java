package de.unknowncity.plots.service.plot;

import de.chojo.sadu.queries.api.configuration.ConnectedQueryConfiguration;
import de.chojo.sadu.queries.api.configuration.QueryConfiguration;
import de.unknowncity.astralib.common.service.Service;
import de.unknowncity.plots.Permissions;
import de.unknowncity.plots.PlotsPlugin;
import de.unknowncity.plots.event.PlotInfoUpdateEvent;
import de.unknowncity.plots.data.dao.PlotDeniedPlayerDao;
import de.unknowncity.plots.data.dao.PlotMemberDao;
import de.unknowncity.plots.plot.access.type.PlotMemberRole;
import de.unknowncity.plots.plot.model.Plot;
import de.unknowncity.plots.plot.model.PlotMember;
import de.unknowncity.plots.plot.model.PlotPlayer;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AccessService extends Service<PlotsPlugin> {
    private final PlotDeniedPlayerDao deniedPlayerDao;
    private final QueryConfiguration queryConfiguration;
    private final Logger logger;
    private final PlotMemberDao memberDao;

    public AccessService(QueryConfiguration queryConfiguration, PlotMemberDao plotMemberDao, PlotDeniedPlayerDao plotDeniedPlayerDao, Logger logger) {
        this.queryConfiguration = queryConfiguration;
        this.deniedPlayerDao = plotDeniedPlayerDao;
        this.memberDao = plotMemberDao;
        this.logger = logger;
    }

    public void denyPlayer(Plot plot, OfflinePlayer offlinePlayer) {
        plot.removeMember(offlinePlayer.getUniqueId());
        var denied = plot.addDeniedPlayer(offlinePlayer);
        CompletableFuture.runAsync(() -> {
            try (ConnectedQueryConfiguration connection = queryConfiguration.withSingleTransaction()) {
                memberDao.delete(connection, plot.id(), offlinePlayer.getUniqueId());
                deniedPlayerDao.write(connection, plot.id(), denied);
            } catch (Exception e) {
               logger.log(Level.SEVERE, "Could not save denied player to database", e);
            }
        });
        new PlotInfoUpdateEvent(plot).callEvent();
    }

    public void unDenyPlayer(Plot plot, UUID uuid) {
        plot.removeDeniedPlayer(uuid);
        new PlotInfoUpdateEvent(plot).callEvent();
        CompletableFuture.runAsync(() -> deniedPlayerDao.delete(plot.id(), uuid));
    }

    public void clearDeniedPlayers(Plot plot) {
        plot.deniedPlayers().clear();
        CompletableFuture.runAsync(() -> deniedPlayerDao.deleteAll(plot.id()));
        new PlotInfoUpdateEvent(plot).callEvent();
    }

    public void addMember(Plot plot, OfflinePlayer offlinePlayer, PlotMemberRole role) {
        plot.removeDeniedPlayer(offlinePlayer.getUniqueId());
        var member = plot.addMember(offlinePlayer, role);
        CompletableFuture.runAsync(() -> {
            try (ConnectedQueryConfiguration configuration = queryConfiguration.withSingleTransaction()) {
                memberDao.write(configuration, plot.id(), member);

                deniedPlayerDao.delete(configuration, plot.id(), offlinePlayer.getUniqueId());
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Could not save member to database", e);
            }
        }).whenComplete((unused, throwable) -> {
            if (throwable != null) {
                JavaPlugin.getPlugin(PlotsPlugin.class).getLogger().log(Level.SEVERE, "Error while saving plot data: ", throwable);
            }
        });
        new PlotInfoUpdateEvent(plot).callEvent();
    }

    public void removeMember(Plot plot, UUID uuid) {
        plot.removeMember(uuid);
        CompletableFuture.runAsync(() -> memberDao.delete(plot.id(), uuid)).whenComplete((unused, throwable) -> {
            if (throwable != null) {
                JavaPlugin.getPlugin(PlotsPlugin.class).getLogger().log(Level.SEVERE, "Error while saving plot data: ", throwable);
            }
        });
        new PlotInfoUpdateEvent(plot).callEvent();

    }

    public void setMemberRole(Plot plot, UUID uuid, PlotMemberRole role) {
        Optional<PlotMember> member = plot.findPlotMember(uuid);
        if (member.isPresent()) {
            var plotMember = member.get();
            plotMember.role(role);
            CompletableFuture.runAsync(() -> memberDao.update(plot.id(), plotMember)).whenComplete((unused, throwable) -> {
                if (throwable != null) {
                    JavaPlugin.getPlugin(PlotsPlugin.class).getLogger().log(Level.SEVERE, "Error while saving plot data: ", throwable);
                }
            });
        }
        new PlotInfoUpdateEvent(plot).callEvent();
    }

    public void clearMembers(Plot plot) {
        plot.members().clear();
        CompletableFuture.runAsync(() -> memberDao.deleteAll(plot.id())).whenComplete((unused, throwable) -> {
            if (throwable != null) {
                JavaPlugin.getPlugin(PlotsPlugin.class).getLogger().log(Level.SEVERE, "Error while saving plot data: ", throwable);
            }
        });
        new PlotInfoUpdateEvent(plot).callEvent();
    }

    public void kickPlayer(Plot plot, Player player) {
        if (player.hasPermission(Permissions.BYPASS_ENTRY)) {
            return;
        }
        player.teleport(plot.world().getSpawnLocation());
    }
}
