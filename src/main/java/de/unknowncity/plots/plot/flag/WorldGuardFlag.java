package de.unknowncity.plots.plot.flag;

import com.sk89q.worldguard.protection.flags.Flag;
import org.bukkit.Material;

public abstract class WorldGuardFlag<T> extends PlotFlag<T> {
    private final Flag<T> worldGuardFlag;

    public WorldGuardFlag(String flagId, T defaultValue, Material displayMaterial, Flag<T> worldGuardFlag) {
        super(flagId, defaultValue, displayMaterial);
        this.worldGuardFlag = worldGuardFlag;
    }

    public Flag<T> worldGuardFlag() {
        return worldGuardFlag;
    }
}
