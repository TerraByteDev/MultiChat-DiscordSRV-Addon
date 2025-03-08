package com.loohp.multichatdiscordsrvaddon.command.subcommand;

import com.loohp.multichatdiscordsrvaddon.command.CommandHandler;
import org.bukkit.command.CommandSender;
import org.incendo.cloud.annotation.specifier.Greedy;
import org.incendo.cloud.annotations.*;
import org.incendo.cloud.annotations.suggestion.Suggestions;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.help.result.CommandEntry;
import org.incendo.cloud.suggestion.Suggestion;

import java.util.List;
import java.util.stream.Collectors;

@Command("multichat|mcd")
public class HelpCommand {

    @Suggestions("helpQuery")
    public List<Suggestion> helpSuggestions(CommandContext<CommandSender> context, CommandInput input) {
        return CommandHandler.manager.createHelpHandler()
                .queryRootIndex(context.sender())
                .entries()
                .stream()
                .map(CommandEntry::syntax)
                .map(Suggestion::suggestion)
                .collect(Collectors.toList());
    }

    @Command("help [query]")
    @CommandDescription("Info on how to use the /multichat command")
    @Permission(value = {"multichatdiscordsrv.help"})
    public void execute(
            CommandSender sender,
            @Default("multichat") @Argument(value = "query", suggestions = "helpQuery", description = "(Optional) Command to get help with") @Greedy String query
    ) {
        CommandHandler.minecraftHelp.queryCommands(query, sender);
    }
}
