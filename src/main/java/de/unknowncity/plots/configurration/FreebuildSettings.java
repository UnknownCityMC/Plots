package de.unknowncity.plots.configurration;

import de.unknowncity.astralib.libs.com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;

public class FreebuildSettings {

    @JsonProperty
    public double price = 3.0;

    @JsonProperty
    public ArrayList<String> noSchematic = new ArrayList<>();


    public FreebuildSettings() {

    }

    public double price() {
        return price;
    }

    public ArrayList<String> noSchematic() {
        return noSchematic;
    }
}
