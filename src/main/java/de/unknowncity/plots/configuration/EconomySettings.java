package de.unknowncity.plots.configuration;

import de.unknowncity.astralib.libs.com.fasterxml.jackson.annotation.JsonProperty;

public class EconomySettings {

    @JsonProperty
    public String currency = "money";

    public EconomySettings() {

    }

    public String currency() {
        return currency;
    }
}
