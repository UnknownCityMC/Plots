package de.unknowncity.plots.configurration.serializer;

import de.unknowncity.astralib.common.configuration.setting.defaults.DataBaseSetting;
import de.unknowncity.plots.configurration.PlotsConfiguration;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.Type;

public class PlotsConfigSerializer implements TypeSerializer<PlotsConfiguration> {
    @Override
    public PlotsConfiguration deserialize(Type type, ConfigurationNode node) throws SerializationException {
        var databaseSettings = node.node("database").get(DataBaseSetting.class);

        return new PlotsConfiguration(databaseSettings);
    }

    @Override
    public void serialize(Type type, @Nullable PlotsConfiguration config, ConfigurationNode node) throws SerializationException {
        if (config == null) {
            return;
        }

        node.node("database").set(config.dataBaseSetting());
    }
}
