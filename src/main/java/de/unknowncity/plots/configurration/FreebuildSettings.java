package de.unknowncity.plots.configurration;

import de.unknowncity.astralib.libs.com.fasterxml.jackson.annotation.JsonProperty;

public class FreebuildSettings {

    @JsonProperty
    public double price = 3.0;

    public FreebuildSettings() {

    }

    public double price() {
        return price;
    }
}
