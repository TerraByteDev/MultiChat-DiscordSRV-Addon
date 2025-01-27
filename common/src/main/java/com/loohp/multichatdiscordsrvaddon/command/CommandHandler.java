package com.loohp.multichatdiscordsrvaddon.command;

import com.loohp.multichatdiscordsrvaddon.InteractiveChatDiscordSrvAddon;
import com.loohp.multichatdiscordsrvaddon.command.subcommand.StatusCommand;
import com.loohp.multichatdiscordsrvaddon.utils.ICLogger;
import org.bukkit.command.CommandSender;
import org.incendo.cloud.annotations.AnnotationParser;
import org.incendo.cloud.bukkit.CloudBukkitCapabilities;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.meta.SimpleCommandMeta;
import org.incendo.cloud.paper.LegacyPaperCommandManager;

public class CommandHandler {

    private final LegacyPaperCommandManager<CommandSender> manager;
    private final AnnotationParser<CommandSender> parser;

    public CommandHandler() {
        this.manager = LegacyPaperCommandManager.createNative(
                InteractiveChatDiscordSrvAddon.plugin,
                ExecutionCoordinator.asyncCoordinator()
        );

        // As per https://cloud.incendo.org/minecraft/paper/#asynchronous-completions,
        // the following code is used to register asynchronous completions.
        // Natively, brigadier is non-blocking (async) and therefore there is no need to register async completions.
        // This is a simple elif statement to check if the server supports brigadier, and if it does, register it, which will support non-blocking completions.
        // If not, check if the server can support asynchronous completions, and if it does, register it.
        if (this.manager.hasCapability(CloudBukkitCapabilities.NATIVE_BRIGADIER)) {
            this.manager.registerBrigadier();
        } else if (this.manager.hasCapability(CloudBukkitCapabilities.ASYNCHRONOUS_COMPLETION)) {
            this.manager.registerAsynchronousCompletions();
        }

        this.parser = new AnnotationParser<>(
                manager,
                CommandSender.class,
                params -> SimpleCommandMeta.empty()
        );

        ICLogger.info("<green>Registered commands.");
    }

    private void registerCommands() {
        parser.parse(new StatusCommand());
    }
}
