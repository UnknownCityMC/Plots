package de.unknowncity.plots.plot.model;

import de.unknowncity.astralib.common.util.DurationFormatter;
import de.unknowncity.astralib.paper.api.message.PaperMessenger;
import de.unknowncity.plots.plot.access.PlotState;
import de.unknowncity.plots.plot.economy.PlotPaymentType;
import de.unknowncity.plots.util.AstraArrays;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.entity.Player;
import org.spongepowered.configurate.NodePath;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class RentPlot extends Plot {
    private LocalDateTime lastRentPayed;
    private long rentIntervalInMin;

    public RentPlot(String plotId, PlotPlayer owner, String groupName, String regionId, double price, String worldName,
                    PlotState state, LocalDateTime claimed, LocalDateTime lastRentPayed, long rentIntervalInMin
    ) {
        super(plotId, groupName, owner, regionId, price, worldName, state, claimed);
        this.lastRentPayed = lastRentPayed;
        this.rentIntervalInMin = rentIntervalInMin;
    }

    public void lastRentPayed(LocalDateTime lastRentPayed) {
        this.lastRentPayed = lastRentPayed;
    }

    public void rentIntervalInMin(long rentIntervalInMin) {
        this.rentIntervalInMin = rentIntervalInMin;
    }

    @Override
    public LocalDateTime lastRentPayed() {
        return lastRentPayed;
    }

    @Override
    public long rentIntervalInMin() {
        return rentIntervalInMin;
    }

    @Override
    public PlotPaymentType paymentType() {
        return PlotPaymentType.RENT;
    }

    @Override
    public TagResolver[] tagResolvers(Player player, PaperMessenger messenger) {
        return AstraArrays.merge(super.tagResolvers(player, messenger), new TagResolver[]{
                Placeholder.component("rented-until", lastRentPayed == null  ? messenger.component(player, NodePath.path("plot", "info", "not-rented")) :
                        Component.text(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm").format(lastRentPayed.plusMinutes(rentIntervalInMin)))),
                Placeholder.unparsed("rent-interval", DurationFormatter.formatDuration(Duration.ofMinutes(rentIntervalInMin)))
        });
    }
}