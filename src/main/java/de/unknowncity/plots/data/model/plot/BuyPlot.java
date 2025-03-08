package de.unknowncity.plots.data.model.plot;

public class BuyPlot extends Plot {

    public BuyPlot(String plotId, String groupName, String regionId, double price, String worldName) {
        super(plotId, groupName, regionId, price, worldName);
    }

    @Override
    public PlotPaymentType plotPayMentType() {
        return PlotPaymentType.BUY;
    }
}
