package de.unknowncity.plots.plot.flag.type.vehicle;

import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import de.unknowncity.plots.plot.flag.WorldGuardFlag;
import org.bukkit.Material;

import java.util.Arrays;
import java.util.List;

public class RideFlag extends WorldGuardFlag<StateFlag.State> {
    public RideFlag() {
        super("ride", StateFlag.State.ALLOW, Material.SADDLE, Flags.RIDE);
    }

    @Override
    public StateFlag.State unmarshall(String input) {
        return StateFlag.State.valueOf(input);
    }

    @Override
    public String marshall(Object input) {
        return input.toString();
    }

    @Override
    public List<StateFlag.State> possibleValues() {
        return Arrays.stream(StateFlag.State.values()).toList();
    }
}
