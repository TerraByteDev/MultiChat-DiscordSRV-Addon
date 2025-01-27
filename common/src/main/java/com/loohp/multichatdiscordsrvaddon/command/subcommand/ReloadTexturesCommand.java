package com.loohp.multichatdiscordsrvaddon.command.subcommand;

import org.bukkit.command.CommandSender;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.CommandDescription;
import org.incendo.cloud.annotations.Permission;
import org.incendo.cloud.annotations.suggestion.Suggestions;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;

import java.util.List;

import static com.loohp.multichatdiscordsrvaddon.InteractiveChatDiscordSrvAddon.plugin;

@Command("multichat")
public class ReloadTexturesCommand {

    @Suggestions("args")
    public List<String> argSuggestions(CommandContext<CommandSender> context, CommandInput input) {
        return List.of("--reset", "--redownload");
    }

    @Command("reloadtextures [arg1] [arg2]")
    @CommandDescription("Reload Minecraft and Pack textures")
    @Permission(value = {"multichatdiscordsrv.reloadtextures"})
    public void execute(
        CommandSender sender,
        @Argument(value = "arg1", suggestions = "args") String arg1,
        @Argument(value = "arg2", suggestions = "args") String arg2
    ) {
        List<String> argList = List.of(arg1, arg2);
        boolean clean = argList.contains("--reset");
        boolean redownload = argList.contains("--redownload") || clean;

        plugin.sendMessage(plugin.reloadTextureMessage, sender);
        plugin.reloadTextures(redownload, clean, sender);
    }
}
