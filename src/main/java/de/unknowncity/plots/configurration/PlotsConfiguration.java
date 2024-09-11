package de.unknowncity.plots.configurration;

import de.unknowncity.astralib.common.configuration.ApplicableAstraConfiguration;
import de.unknowncity.astralib.common.configuration.setting.defaults.DataBaseSetting;

public class PlotsConfiguration extends ApplicableAstraConfiguration {

    private DataBaseSetting dataBaseSetting;

    public PlotsConfiguration(DataBaseSetting dataBaseSetting) {
        this.dataBaseSetting = dataBaseSetting;
    }

    public DataBaseSetting dataBaseSetting() {
        return dataBaseSetting;
    }
}
