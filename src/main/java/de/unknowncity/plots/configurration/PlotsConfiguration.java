package de.unknowncity.plots.configurration;


import de.unknowncity.astralib.common.configuration.YamlAstraConfiguration;
import de.unknowncity.astralib.common.configuration.annotation.Config;
import de.unknowncity.astralib.common.configuration.setting.defaults.ModernDataBaseSetting;
import de.unknowncity.astralib.libs.com.fasterxml.jackson.annotation.JsonProperty;

@Config(targetFile = "plugins/UC-Plots/config.yml")
public class PlotsConfiguration extends YamlAstraConfiguration {

    @JsonProperty
    private final ModernDataBaseSetting database = new ModernDataBaseSetting();

    @JsonProperty
    private final EconomySettings economy = new EconomySettings();

    @JsonProperty
    private final FreebuildSettings fb = new FreebuildSettings();

    public PlotsConfiguration() {

    }

    public ModernDataBaseSetting database() {
        return database;
    }

    public EconomySettings economy() {
        return economy;
    }

    public FreebuildSettings fb() {
        return fb;
    }
}
