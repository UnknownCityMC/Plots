package de.unknowncity.plots.configuration;

import de.unknowncity.astralib.libs.com.fasterxml.jackson.annotation.JsonProperty;

public class GuiSettings {

    @JsonProperty
    private final String buttonModelReturn = "unknowncity:gui_button_return";

    @JsonProperty
    private final String buttonModelNext = "unknowncity:gui_button_next";

    @JsonProperty
    private final String buttonModelPrev = "unknowncity:gui_button_prev";

    @JsonProperty
    private final String buttonModelPlus = "unknowncity:gui_button_plus";

    public GuiSettings() {

    }

    public String buttonModelNext() {
        return buttonModelNext;
    }

    public String buttonModelPlus() {
        return buttonModelPlus;
    }

    public String buttonModelPrev() {
        return buttonModelPrev;
    }

    public String buttonModelReturn() {
        return buttonModelReturn;
    }
}
