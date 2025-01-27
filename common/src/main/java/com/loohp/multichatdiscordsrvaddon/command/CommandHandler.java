package com.loohp.multichatdiscordsrvaddon.command;

import com.loohp.multichatdiscordsrvaddon.InteractiveChatDiscordSrvAddon;
import com.loohp.multichatdiscordsrvaddon.command.subcommand.StatusCommand;
import com.loohp.multichatdiscordsrvaddon.utils.ICLogger;
import org.bukkit.command.CommandSender;
import org.incendo.cloud.annotations.AnnotationParser;
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
