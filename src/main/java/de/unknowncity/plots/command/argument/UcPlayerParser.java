package de.unknowncity.plots.command.argument;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
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

import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class UcPlayerParser<C> implements ArgumentParser<C, OfflinePlayer> {

    public static <C> ParserDescriptor<C, OfflinePlayer> ucPlayerParser() {
        return ParserDescriptor.of(new UcPlayerParser<>(), OfflinePlayer.class);
    }

    @Override
    public @NonNull ArgumentParseResult<@NonNull OfflinePlayer> parse(@NonNull CommandContext<@NonNull C> commandContext, @NonNull CommandInput commandInput) {
        var player = commandInput.readString();
        var playerOpt = Bukkit.getOfflinePlayerIfCached(player);

        if (playerOpt == null) {
            return ArgumentParseResult.failure(new PlayerParseException(player, commandContext));
        }

        return ArgumentParseResult.success(Bukkit.getOfflinePlayer(playerOpt.getUniqueId()));
    }

    @Override
    public @NonNull SuggestionProvider<C> suggestionProvider() {
        return (context, input) ->
                CompletableFuture.completedFuture(Arrays.stream(Bukkit.getOfflinePlayers()).map(OfflinePlayer::getName).filter(Objects::nonNull).map(Suggestion::suggestion).toList());
    }

    public static final class PlayerParseException extends ParserException {


        private PlayerParseException(
                final @NonNull String input,
                final @NonNull CommandContext<?> context
        ) {
            super(
                    UcPlayerParser.class,
                    context,
                    Caption.of("argument.parse.failure.player"),
                    CaptionVariable.of("player", input)
            );
        }
    }
}