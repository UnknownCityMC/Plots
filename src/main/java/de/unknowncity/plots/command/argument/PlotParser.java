package de.unknowncity.plots.command.argument;

import de.unknowncity.plots.plot.model.Plot;
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

public class PlotParser<C> implements ArgumentParser<C, Plot> {
    private final PlotService plotService;

    public PlotParser(PlotService plotService) {
        this.plotService = plotService;
    }

    public static <C> ParserDescriptor<C, Plot> plotParser(PlotService plotService) {
        return ParserDescriptor.of(new PlotParser<>(plotService), Plot.class);
    }

    @Override
    public @NonNull ArgumentParseResult<@NonNull Plot> parse(@NonNull CommandContext<@NonNull C> commandContext, @NonNull CommandInput commandInput) {
        var token = commandInput.readString();

        var plotOpt = plotService.getPlot(token);
        return plotOpt.map(ArgumentParseResult::success)
                .orElseGet(() -> ArgumentParseResult.failure(new PlotParseException(token, commandContext)));

    }

    @Override
    public @NonNull SuggestionProvider<C> suggestionProvider() {
        return (context, input) ->
                CompletableFuture.completedFuture(plotService.plotGroupCache().asMap().keySet().stream().map(Suggestion::suggestion).toList());
    }

    public static final class PlotParseException extends ParserException {


        private PlotParseException(
                final @NonNull String input,
                final @NonNull CommandContext<?> context
        ) {
            super(
                    PlotGroupParser.class,
                    context,
                    Caption.of("argument.parse.failure.plot"),
                    CaptionVariable.of("plot", input)
            );
        }
    }
}
