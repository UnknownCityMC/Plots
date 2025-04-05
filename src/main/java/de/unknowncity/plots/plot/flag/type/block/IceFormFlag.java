package de.unknowncity.plots.plot.flag.type.block;

import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import de.unknowncity.plots.plot.flag.WorldGuardFlag;
import org.bukkit.Material;

import java.util.Arrays;
import java.util.List;

public class IceFormFlag extends WorldGuardFlag<StateFlag.State> {
    public IceFormFlag() {
        super("ice-form", StateFlag.State.ALLOW, Material.ICE, Flags.ICE_FORM);
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
