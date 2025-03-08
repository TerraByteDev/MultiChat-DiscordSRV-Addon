package com.loohp.multichatdiscordsrvaddon.standalone.cmd;

import com.loohp.multichatdiscordsrvaddon.MultiChatDiscordSrvAddon;
import com.loohp.multichatdiscordsrvaddon.config.Config;
import com.loohp.multichatdiscordsrvaddon.objectholders.ICPlaceholder;
import com.loohp.multichatdiscordsrvaddon.standalone.cmd.impl.StandalonePlayerInfoCommand;
import com.loohp.multichatdiscordsrvaddon.standalone.cmd.impl.StandalonePlayerListCommand;
import com.loohp.multichatdiscordsrvaddon.standalone.cmd.impl.StandaloneResourcePackCommand;
import com.loohp.multichatdiscordsrvaddon.standalone.cmd.impl.StandaloneShareItemCommand;
import com.loohp.multichatdiscordsrvaddon.utils.ChatColorUtils;
import org.incendo.cloud.Command;
import org.incendo.cloud.description.Description;
import org.incendo.cloud.discord.jda5.JDA5CommandManager;
import org.incendo.cloud.discord.jda5.JDAInteraction;
import org.incendo.cloud.discord.jda5.JDAParser;
import org.incendo.cloud.discord.jda5.ReplySetting;
import org.incendo.cloud.discord.slash.CommandScope;
import org.incendo.cloud.discord.slash.DiscordChoices;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.parser.standard.IntegerParser;
import org.incendo.cloud.parser.standard.StringParser;

import java.util.Optional;

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
        Description memberDescription = Description.of(Config.i().getDiscordCommands().globalSettings().messages().memberDescription());

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
                                    memberDescription
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

        if (Config.i().getDiscordCommands().shareItem().isMainServer()) {
            Optional<ICPlaceholder> optItemPlaceholder = MultiChatDiscordSrvAddon.placeholderList.values().stream().filter(each -> each.equals(MultiChatDiscordSrvAddon.itemPlaceholder)).findFirst();
            if (Config.i().getDiscordCommands().shareItem().enabled() && optItemPlaceholder.isPresent()) {
                Description itemDescription = Description.of(ChatColorUtils.stripColor(optItemPlaceholder.get().getDescription()));

                Command.Builder<JDAInteraction> root = commandManager.commandBuilder(ITEM_LABEL, itemDescription);

                root.literal("mainhand", itemDescription)
                        .handler(commandContext -> StandaloneShareItemCommand.shareItemCommand(commandContext.sender()));
                root.literal("offhand", itemDescription)
                        .handler(commandContext -> StandaloneShareItemCommand.shareItemCommand(commandContext.sender()));
                root.literal("hotbar", itemDescription)
                        .required(StandaloneShareItemCommand.slotKey, IntegerParser.integerParser(1, 9), itemDescription)
                        .handler(commandContext -> StandaloneShareItemCommand.shareItemCommand(commandContext.sender()));
                root.literal("inventory", itemDescription)
                        .required(StandaloneShareItemCommand.slotKey, IntegerParser.integerParser(1, 41), itemDescription)
                        .handler(commandContext -> StandaloneShareItemCommand.shareItemCommand(commandContext.sender()));
                root.literal("armor", itemDescription)
                        .required(StandaloneShareItemCommand.armorKey, StringParser.stringParser(), itemDescription, DiscordChoices.strings("head", "chest", "legs", "feet"))
                        .handler(commandContext -> StandaloneShareItemCommand.shareItemCommand(commandContext.sender()));
                root.literal("ender", itemDescription)
                        .required(StandaloneShareItemCommand.slotKey, IntegerParser.integerParser(1, 27), itemDescription)
                        .handler(commandContext -> StandaloneShareItemCommand.shareItemCommand(commandContext.sender()));
                commandManager.command(root);

                if (Config.i().getDiscordCommands().shareItem().allowAsOthers()) {
                    Command.Builder<JDAInteraction> otherRoot = commandManager.commandBuilder(ITEM_LABEL, itemDescription);

                    otherRoot.literal("mainhand", itemDescription)
                            .required(StandalonePlayerInfoCommand.USER, JDAParser.userParser(), memberDescription)
                            .handler(commandContext -> StandaloneShareItemCommand.shareItemCommand(commandContext.sender()));
                    otherRoot.literal("offhand", itemDescription)
                            .required(StandalonePlayerInfoCommand.USER, JDAParser.userParser(), memberDescription)
                            .handler(commandContext -> StandaloneShareItemCommand.shareItemCommand(commandContext.sender()));
                    otherRoot.literal("hotbar", itemDescription)
                            .required(StandaloneShareItemCommand.slotKey, IntegerParser.integerParser(1, 9), itemDescription)
                            .required(StandalonePlayerInfoCommand.USER, JDAParser.userParser(), memberDescription)
                            .handler(commandContext -> StandaloneShareItemCommand.shareItemCommand(commandContext.sender()));
                    otherRoot.literal("inventory", itemDescription)
                            .required(StandaloneShareItemCommand.slotKey, IntegerParser.integerParser(1, 41), itemDescription)
                            .required(StandalonePlayerInfoCommand.USER, JDAParser.userParser(), memberDescription)
                            .handler(commandContext -> StandaloneShareItemCommand.shareItemCommand(commandContext.sender()));
                    otherRoot.literal("armor", itemDescription)
                            .required(StandaloneShareItemCommand.armorKey, StringParser.stringParser(), itemDescription, DiscordChoices.strings("head", "chest", "legs", "feet"))
                            .required(StandalonePlayerInfoCommand.USER, JDAParser.userParser(), memberDescription)
                            .handler(commandContext -> StandaloneShareItemCommand.shareItemCommand(commandContext.sender()));
                    otherRoot.literal("ender", itemDescription)
                            .required(StandaloneShareItemCommand.slotKey, IntegerParser.integerParser(1, 27), itemDescription)
                            .required(StandalonePlayerInfoCommand.USER, JDAParser.userParser(), memberDescription)
                            .handler(commandContext -> StandaloneShareItemCommand.shareItemCommand(commandContext.sender()));
                    commandManager.command(otherRoot);
                }
            }
        }
    }
}
