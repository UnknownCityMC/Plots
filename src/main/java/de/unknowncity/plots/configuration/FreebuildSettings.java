package de.unknowncity.plots.configuration;

import de.unknowncity.astralib.libs.com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;

public class FreebuildSettings {

    @JsonProperty
    private final double price = 3.0;

    @JsonProperty
    private final ArrayList<String> noSchematic = new ArrayList<>();
    @JsonProperty
    private final String freeBuildGroup = "freebuild";


    public FreebuildSettings() {

    }

    public double price() {
        return price;
    }

    public ArrayList<String> noSchematic() {
        return noSchematic;
    }

    public String freeBuildGroup() {
        return freeBuildGroup;
    }
}
