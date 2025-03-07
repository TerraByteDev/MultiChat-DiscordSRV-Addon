package com.loohp.multichatdiscordsrvaddon.standalone.cmd;

import com.loohp.multichatdiscordsrvaddon.MultiChatDiscordSrvAddon;
import com.loohp.multichatdiscordsrvaddon.config.Config;
import com.loohp.multichatdiscordsrvaddon.standalone.cmd.impl.StandalonePlayerInfoCommand;
import com.loohp.multichatdiscordsrvaddon.standalone.cmd.impl.StandalonePlayerListCommand;
import com.loohp.multichatdiscordsrvaddon.standalone.cmd.impl.StandaloneResourcePackCommand;
import com.loohp.multichatdiscordsrvaddon.utils.ChatColorUtils;
import net.dv8tion.jda.api.entities.User;
import org.incendo.cloud.description.Description;
import org.incendo.cloud.discord.jda5.JDA5CommandManager;
import org.incendo.cloud.discord.jda5.JDAInteraction;
import org.incendo.cloud.discord.jda5.JDAParser;
import org.incendo.cloud.discord.jda5.ReplySetting;
import org.incendo.cloud.discord.slash.CommandScope;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.key.CloudKey;
import org.incendo.cloud.parser.ParserDescriptor;

import static com.loohp.multichatdiscordsrvaddon.utils.DiscordCommandUtils.*;

public class StandaloneCommandManager {

    private JDA5CommandManager<JDAInteraction> commandManager;

    public void initialise() {
        this.commandManager = new JDA5CommandManager<>(
                ExecutionCoordinator.asyncCoordinator(),
                JDAInteraction.InteractionMapper.identity()
        );

        MultiChatDiscordSrvAddon.plugin.standaloneManager.getJda().addEventListener(commandManager.createListener());
        registerCommands();
    }

    private void registerCommands() {
        if (Config.i().getDiscordCommands().resourcePack().enabled() && Config.i().getDiscordCommands().resourcePack().isMainServer()) {
            commandManager.command(
                    commandManager.commandBuilder(RESOURCEPACK_LABEL, Description.of(ChatColorUtils.stripColor(Config.i().getDiscordCommands().resourcePack().description())))
                            .apply(CommandScope.guilds())
                            .apply(ReplySetting.defer(true))
                            .handler(commandContext -> StandaloneResourcePackCommand.resourcePackCommand(commandContext.sender()))
            );

        }

        if (Config.i().getDiscordCommands().playerInfo().enabled() && Config.i().getDiscordCommands().playerList().isMainServer()) {
            commandManager.command(
                    commandManager.commandBuilder(PLAYERINFO_LABEL, Description.of(ChatColorUtils.stripColor(Config.i().getDiscordCommands().playerInfo().description())))
                            .apply(CommandScope.guilds())
                            .optional(
                                    StandalonePlayerInfoCommand.USER,
                                    JDAParser.userParser(),
                                    Description.of(Config.i().getDiscordCommands().globalSettings().messages().memberDescription())
                            )
                            .apply(ReplySetting.defer(false))
                            .handler(commandContext -> StandalonePlayerInfoCommand.playerInfoCommand(commandContext.sender(), commandContext.optional(StandalonePlayerInfoCommand.USER)))
            );
        }

        if (Config.i().getDiscordCommands().playerList().enabled() && Config.i().getDiscordCommands().playerList().isMainServer()) {
            commandManager.command(
                    commandManager.commandBuilder(PLAYERLIST_LABEL, Description.of(ChatColorUtils.stripColor(Config.i().getDiscordCommands().playerList().description())))
                            .apply(CommandScope.guilds())
                            .handler(commandContext -> StandalonePlayerListCommand.playerListCommand(commandContext.sender()))
            );
        }
    }
}
