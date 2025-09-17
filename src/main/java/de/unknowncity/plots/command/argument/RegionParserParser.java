package de.unknowncity.plots.command.argument;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import de.unknowncity.plots.service.RegionService;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.caption.Caption;
import org.incendo.cloud.caption.CaptionVariable;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.exception.parsing.ParserException;
import org.incendo.cloud.parser.ArgumentParseResult;
import org.incendo.cloud.parser.ArgumentParser;
import org.incendo.cloud.parser.ParserDescriptor;
import org.incendo.cloud.suggestion.Suggestion;
import org.incendo.cloud.suggestion.SuggestionProvider;

import java.util.concurrent.CompletableFuture;

public class RegionParserParser<C> implements ArgumentParser<C, ProtectedRegion> {
    private final RegionService regionService;

    public RegionParserParser(RegionService regionService) {
        this.regionService = regionService;
    }

    public static <C> ParserDescriptor<C, ProtectedRegion> regionParser(RegionService regionService) {
        return ParserDescriptor.of(new RegionParserParser<>(regionService), ProtectedRegion.class);
    }

    @Override
    public @NonNull ArgumentParseResult<@NonNull ProtectedRegion> parse(@NonNull CommandContext<@NonNull C> commandContext, @NonNull CommandInput commandInput) {
        var token = commandInput.readString();

        var player = (Player) commandContext.sender();

        if (regionService.getRegions(player.getWorld()).containsKey(token)) {
            return ArgumentParseResult.success(regionService.getRegions(player.getWorld()).get(token));
        }

        return ArgumentParseResult.failure(new RegionParseException(token, commandContext));
    }

    @Override
    public @NonNull SuggestionProvider<C> suggestionProvider() {
        return (context, input) -> {
            var player = (Player) context.sender();

            return CompletableFuture.completedFuture(regionService.getRegions(player.getWorld()).keySet().stream().map(Suggestion::suggestion).toList());
        };
    }

    public static final class RegionParseException extends ParserException {


        private RegionParseException(
                final @NonNull String input,
                final @NonNull CommandContext<?> context
        ) {
            super(
                    RegionParserParser.class,
                    context,
                    Caption.of("argument.parse.failure.plot"),
                    CaptionVariable.of("region", input)
            );
        }
    }
}
