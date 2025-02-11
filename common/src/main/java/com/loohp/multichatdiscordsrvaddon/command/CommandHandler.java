package com.loohp.multichatdiscordsrvaddon.command;

import com.loohp.multichatdiscordsrvaddon.MultiChatDiscordSrvAddon;
import com.loohp.multichatdiscordsrvaddon.command.subcommand.*;
import com.loohp.multichatdiscordsrvaddon.utils.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.incendo.cloud.annotations.AnnotationParser;
import org.incendo.cloud.bukkit.CloudBukkitCapabilities;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.meta.SimpleCommandMeta;
import org.incendo.cloud.minecraft.extras.MinecraftHelp;
import org.incendo.cloud.paper.LegacyPaperCommandManager;

import static com.loohp.multichatdiscordsrvaddon.utils.ChatUtils.audience;

public class CommandHandler {

    private static AnnotationParser<CommandSender> parser;
    public static LegacyPaperCommandManager<CommandSender> manager;
    public static MinecraftHelp<CommandSender> minecraftHelp;

    public CommandHandler() {
        manager = LegacyPaperCommandManager.createNative(
                MultiChatDiscordSrvAddon.plugin,
                ExecutionCoordinator.asyncCoordinator()
        );

        // As per https://cloud.incendo.org/minecraft/paper/#asynchronous-completions,
        // the following code is used to register asynchronous completions.
        // Natively, brigadier is non-blocking (async) and therefore there is no need to register async completions.
        // This is a simple elif statement to check if the server supports brigadier, and if it does, register it, which will support non-blocking completions.
        // If not, check if the server can support asynchronous completions, and if it does, register it.
        if (manager.hasCapability(CloudBukkitCapabilities.NATIVE_BRIGADIER)) {
            manager.registerBrigadier();
        } else if (manager.hasCapability(CloudBukkitCapabilities.ASYNCHRONOUS_COMPLETION)) {
            manager.registerAsynchronousCompletions();
        }

        minecraftHelp = MinecraftHelp.<CommandSender>builder()
                .commandManager(manager)
                .audienceProvider(audience::sender)
                .commandPrefix("/multichat help")
                .build();

        parser = new AnnotationParser<>(
                manager,
                CommandSender.class,
                params -> SimpleCommandMeta.empty()
        );

        registerCommands();

        ChatUtils.sendMessage("<green>Registered commands.", Bukkit.getConsoleSender());
    }

    private void registerCommands() {
        parser.parse(new MainCommand());
        parser.parse(new StatusCommand());
        parser.parse(new ReloadConfigCommand());
        parser.parse(new ReloadTexturesCommand());
        parser.parse(new CheckUpdateCommand());
        parser.parse(new ImageMapCommand());
        parser.parse(new HelpCommand());
    }
}
