package de.unknowncity.plots.configurration;


import de.unknowncity.astralib.common.configuration.YamlAstraConfiguration;
import de.unknowncity.astralib.common.configuration.annotation.Config;
import de.unknowncity.astralib.common.configuration.setting.defaults.ModernDataBaseSetting;

@Config(targetFile = "plugins/UC-Plots/config.yml")
public class PlotsConfiguration extends YamlAstraConfiguration {

    private ModernDataBaseSetting database;

    public PlotsConfiguration() {
        database = new ModernDataBaseSetting();
    }

    public ModernDataBaseSetting database() {
        return database;
    }
}
