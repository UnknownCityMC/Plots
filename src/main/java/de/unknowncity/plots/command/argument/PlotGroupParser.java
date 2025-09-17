package de.unknowncity.plots.command.argument;

import de.unknowncity.plots.plot.group.PlotGroup;
import de.unknowncity.plots.service.PlotService;
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

public class PlotGroupParser<C> implements ArgumentParser<C, PlotGroup> {
    private final PlotService plotService;

    public PlotGroupParser(PlotService plotService) {
        this.plotService = plotService;
    }

    public static <C> ParserDescriptor<C, PlotGroup> plotGroupParser(PlotService plotService) {
        return ParserDescriptor.of(new PlotGroupParser<>(plotService), PlotGroup.class);
    }

    @Override
    public @NonNull ArgumentParseResult<@NonNull PlotGroup> parse(@NonNull CommandContext<@NonNull C> commandContext, @NonNull CommandInput commandInput) {
        var token = commandInput.peekString();
        if (plotService.groupCache().containsKey(token)) {
            return ArgumentParseResult.success(plotService.groupCache().get(token));
        }

        return ArgumentParseResult.failure(new PlotGroupParseException(token, commandContext));
    }

    @Override
    public @NonNull SuggestionProvider<C> suggestionProvider() {
        return (context, input) ->
                CompletableFuture.completedFuture(plotService.groupCache().keySet().stream().map(Suggestion::suggestion).toList());
    }

    public static final class PlotGroupParseException extends ParserException {


        private PlotGroupParseException(
                final @NonNull String input,
                final @NonNull CommandContext<?> context
        ) {
            super(
                    PlotGroupParser.class,
                    context,
                    Caption.of("argument.parse.failure.group"),
                    CaptionVariable.of("group", input)
            );
        }
    }
}
