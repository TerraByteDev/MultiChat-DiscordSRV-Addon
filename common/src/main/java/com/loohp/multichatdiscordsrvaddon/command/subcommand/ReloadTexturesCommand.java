package com.loohp.multichatdiscordsrvaddon.command.subcommand;

import com.loohp.multichatdiscordsrvaddon.config.Config;
import com.loohp.multichatdiscordsrvaddon.utils.ChatUtils;
import org.bukkit.command.CommandSender;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.CommandDescription;
import org.incendo.cloud.annotations.Permission;
import org.incendo.cloud.annotations.suggestion.Suggestions;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.loohp.multichatdiscordsrvaddon.MultiChatDiscordSrvAddon.plugin;

@Command("multichat|mc")
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
        List<String> argList = Stream.of(arg1, arg2)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        boolean clean = argList.contains("--reset");
        boolean redownload = argList.contains("--redownload") || clean;

        ChatUtils.sendMessage(Config.i().getMessages().reloadTexture(), sender);
        plugin.reloadTextures(redownload, clean, sender);
    }
}
