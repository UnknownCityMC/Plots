package de.unknowncity.plots.command.argument;

import de.unknowncity.plots.plot.location.PlotLocation;
import org.bukkit.command.CommandSender;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.parser.ArgumentParseResult;
import org.incendo.cloud.parser.ArgumentParser;

public class PlotHomeParser implements ArgumentParser<CommandSender, PlotLocation> {
    @Override
    public @NonNull ArgumentParseResult<@NonNull PlotLocation> parse(@NonNull CommandContext<@NonNull CommandSender> commandContext, @NonNull CommandInput commandInput) {
        return null;
    }
}
