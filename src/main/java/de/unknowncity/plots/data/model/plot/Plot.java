package de.unknowncity.plots.data.model.plot;

import de.chojo.sadu.mapper.annotation.MappingProvider;
import de.chojo.sadu.mapper.reader.StandardReader;
import de.chojo.sadu.mapper.rowmapper.RowMapping;
import de.unknowncity.plots.data.model.plot.flag.PlotFlag;

import java.util.ArrayList;
import java.util.List;

public abstract class Plot {
    private final String plotId;
    private final String regionId;
    private String groupName;
    private double price;
    private String worldName;

    private List<PlotMember> members = new ArrayList<>();
    private List<PlotFlag> flags = new ArrayList<>();
    private List<RelativePlotLocation> locations = new ArrayList<>();

    public Plot(String plotId, String groupName, String regionId, double price, String worldName) {
        this.plotId = plotId;
        this.groupName = groupName;
        this.regionId = regionId;
        this.price = price;
        this.worldName = worldName;
    }

    public List<PlotFlag> flags() {
        return flags;
    }

    public List<PlotMember> members() {
        return members;
    }

    public String id() {
        return plotId;
    }

    public String regionId() {
        return regionId;
    }

    public String groupName() {
        return groupName;
    }

    public List<RelativePlotLocation> locations() {
        return locations;
    }

    public void flags(List<PlotFlag> plotFlags) {
        this.flags = plotFlags;
    }

    public void members(List<PlotMember> plotMembers) {
        this.members = plotMembers;
    }

    public void locations(List<RelativePlotLocation> locations) {
        this.locations = locations;
    }

    public void groupName(String groupName) {
        this.groupName = groupName;
    }

    public abstract PlotPaymentType plotPayMentType();


    @MappingProvider({"test"})
    public static RowMapping<? extends Plot> map() {
        return row -> {
            var paymentType = row.getEnum("payment_type", PlotPaymentType.class);
            var plotId = row.getString("id");
            if (paymentType == PlotPaymentType.BUY) {
                return new BuyPlot(
                        plotId,
                        row.getString("group_name"),
                        row.getString("region_id"),
                        row.getDouble("price"),
                        row.getString("world")
                );
            } else {
                return new RentPlot(
                        plotId,
                        row.getString("group_name"),
                        row.getString("region_id"),
                        row.getDouble("price"),
                        row.getString("world"),
                        row.get("last_rent_paid", StandardReader.LOCAL_DATE_TIME),
                        row.getLong("rent_interval")
                );
            }
        };
    }
}