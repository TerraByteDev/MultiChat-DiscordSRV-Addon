package com.loohp.multichatdiscordsrvaddon.command.subcommand;

import com.loohp.multichatdiscordsrvaddon.command.CommandHandler;
import org.bukkit.command.CommandSender;
import org.incendo.cloud.annotation.specifier.Greedy;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.CommandDescription;
import org.incendo.cloud.annotations.Permission;
import org.incendo.cloud.annotations.suggestion.Suggestions;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.help.result.CommandEntry;
import org.incendo.cloud.suggestion.Suggestion;

import java.util.List;
import java.util.stream.Collectors;

@Command("multichat")
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

    @Command("help <query>")
    @CommandDescription("Info on how to use the /multichat command")
    @Permission(value = {"multichatdiscordsrv.help"})
    public void execute(
            CommandSender sender,
            @Argument(value = "query", suggestions = "helpQuery") @Greedy String query
    ) {
        CommandHandler.minecraftHelp.queryCommands(query, sender);
    }
}
