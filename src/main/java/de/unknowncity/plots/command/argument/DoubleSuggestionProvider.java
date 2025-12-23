package de.unknowncity.plots.command.argument;

import org.bukkit.command.CommandSender;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.suggestion.Suggestion;
import org.incendo.cloud.suggestion.SuggestionProvider;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class DoubleSuggestionProvider implements SuggestionProvider<CommandSender> {
    private static final List<String> NUMBERS = List.of("0", "1", "2", "3", "4", "5", "6", "7", "8", "9");
    private static final List<String> DECIMALS = List.of(".", "0", "1", "2", "3", "4", "5", "6", "7", "8", "9");
    private static final List<String> NEGATIVES = List.of( "0", "1", "2", "3", "4", "5", "6", "7", "8", "9");
    public static final DoubleSuggestionProvider DOUBLE_SUGGESTION_PROVIDER = new DoubleSuggestionProvider();

    @Override
    public @NonNull CompletableFuture<? extends @NonNull Iterable<? extends @NonNull Suggestion>> suggestionsFuture(@NonNull CommandContext<CommandSender> context, @NonNull CommandInput input) {
        var token = input.peekString();

        if (token.isEmpty()) {
            return CompletableFuture.completedFuture(NEGATIVES.stream().map(s -> token + s).map(Suggestion::suggestion).toList());
        }
        if (token.endsWith("-") || token.contains(".")) {
            return CompletableFuture.completedFuture(NUMBERS.stream().map(s -> token + s).map(Suggestion::suggestion).toList());
        } else {
            return CompletableFuture.completedFuture(DECIMALS.stream().map(s -> token + s).map(Suggestion::suggestion).toList());
        }
    }
}
