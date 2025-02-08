package com.loohp.multichatdiscordsrvaddon.config;

import de.exlll.configlib.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Configuration
public class Config {

    private static final YamlConfigurationProperties properties = YamlConfigurationProperties.newBuilder()
            .setNameFormatter(NameFormatters.IDENTITY)
            .charset(StandardCharsets.UTF_8)
            .build();

    private static Config instance;
    @Setter
    private static File cachedDataFolder;

    public record EmbedDisplay(
            String single,
            String multiple
    ) {}

    public record Frame(
            ItemStack primary,
            ItemStack secondary
    ) {}

    public record Item(
            boolean enabled,
            @Comment("Cooldown of usage in seconds") int cooldown,
            @Comment("If UseTooltipImage is true, should the plugin attach the tooltip image if the target item is just a plain base item,\nwith no information worth showing?") boolean useTooltipImageOnBaseItem,
            EmbedDisplay embedDisplay,
            boolean alternateAirTexture,
            @Comment("\nThe item used for the frame of the Item GUI") Frame frame,
            @Comment("\nThe text to replace the keyword") String text,
            @Comment("Title of the item view embed") String itemTitle,
            @Comment("Generally used for item description in placeholders and discord commands.") String description,
            @Comment("\nPreview shared maps instead of showing the item itself") boolean previewMaps
    ) {}

    public record Inventory(
            boolean enabled,
            @Comment("Cooldown of usage in seconds") int cooldown,
            @Comment("Use the player inventory GUI template instead of the normnal inventory template") boolean usePlayerInventoryView,
            @Comment("Whether to render the items a player is holding.\nOnly effective when UsePlayerInventoryView is true") boolean renderHandHeldItems,
            String embedColor,
            boolean showExperienceLevel,
            @Comment("\nThe text to replace the keyword") String text,
            @Comment("Title of the inventory view") String inventoryTitle,
            @Comment("Generally used for inventory description in placeholders and discord commands.") String description,
            @Comment("\n") Frame frame
    ) {}

    public record EnderChest(
            boolean enabled,
            @Comment("Cooldown of usage in seconds") int cooldown,
            String embedColor,
            @Comment("\nThe text to replace the keyword") String text,
            @Comment("The title of the ender chest inventory view") String inventoryTitle,
            @Comment("Generally used for enderchest description in placeholders and discord commands.") String description
    ) {}

    public record ToolTipSettings(
            boolean showDurability,
            boolean showArmorColor,
            boolean showMapScale,
            boolean showFireworkRocketDetailsInCrossbow,
            boolean showAdvanceDetails
    ) {}

    public record Placeholders(
            @Comment("If your chat plugin supports it, what placeholder does the player use to show their held item in chat? (Regex)") String item,
            @Comment("Same as above but for player inventories") String inventory,
            @Comment("Same as above but for player enderchests") String enderChest
    ) {}

    public record DiscordItemDetailsAndInteractions(
            @Comment("Allow users to select items inside an inventory shared to discord") boolean allowInventorySelection,
            @Comment("Share filled map details as an image") boolean showMaps,
            @Comment("Share book contents in an interactive book on Discord") boolean showBooks,
            @Comment("Show what is inside a container when sharing a container to Discord") boolean showContainers
    ) {}

    public record DeathMessage(
            @Comment("Apply DiscordSRV's death message in %deathmessage% with the localized death message from Minecraft") boolean translateDeathmessage,
            @Comment("Show the weapon inside the death message if there is one") boolean showItems,
            @Comment("Title for the show item embed") String title
    ) {}

    public record Advancements(
            @Comment("Replace the advancement name from DiscordSRV with the actual display name") boolean correctAdvancementName,
            @Comment("Show the icon of the advancement instead of the player head") boolean changeToItemIcon,
            @Comment("Show the description of that advancement") boolean showDescription
    ) {}

    public record HoverEventDisplay(
            @Comment("Whether to show hover event text") boolean enabled,
            @Comment("Whether to show the cursor image") boolean showCursorImage,
            @Comment("Show information in a tooltip image instead of text") boolean useTooltipImage,
            @Comment("Which custom placeholder should have their hover event text ignored?\nThis is a list of CustomPlaceholder keys.") List<String> ignoredPlaceholderKeys
    ) {}

    public record DiscordAttachmentsFormattingHover(
            boolean enabled,
            List<String> hoverText,
            List<String> imageOriginalHover
    ) {}

    public record DiscordAttachmentsFormatting(
            String text,
            String imageOriginal,
            DiscordAttachmentsFormattingHover hover
    ) {}

    public record ImageMapBackground(
            boolean transparent,
            @Comment("Only used when Transparent is set to false") String color
    ) {}

    public record RestrictImageUrl(
            boolean enabled,
            List<String> whitelist
    ) {}

    public record PlaybackBar(
        boolean enabled,
        String filledColor,
        String emptyColor
    ) {}

    public record DiscordAttachments(
            @Comment("Whether to convert discord attachments from disgusting URLs into clickable text in chat") boolean convert,
            DiscordAttachmentsFormatting formatting,
            @Comment("Whether to allow previewing images in game using maps") boolean showImageUsingMaps,
            @Comment("What color should the background be when previewing images in game using maps?\nNote: The color will not be exact as it is limited to the map colors that Minecraft offers") ImageMapBackground imageMapBackground,
            @Comment("When enabled, only websites from the list below are allowed to be shown through in-game maps") RestrictImageUrl restrictImageUrl,
            @Comment("If the attachment is larger than the defined size, it won't be downloaded for preview.\n(In Bytes)") long fileSizeLimit,
            long timeout,
            PlaybackBar playbackBar
    ) {}

    public record DiscordCommandsGlobalSettingsMessages(
            @Comment("Labels must be lowercase") String member,
            String memberDescription,
            String slotLabel,
            String slotDescription
    ) {}

    public record DiscordCommandsGlobalSettings(
            @Comment("Set this to true ONLY if you have multiple servers with MultiChatDiscordSRVAddon, AND\nyou use a DIFFERENT channel for each server (i.e. not one shared channel for all servers)") boolean respondToCommandsInInvalidChannels,
            DiscordCommandsGlobalSettingsMessages messages
    ) {}

    public record DiscordCommandsPlayerInfoFormatting(
            String title,
            String subtitle,
            List<String> whenOnline,
            List<String> whenOffline
    ) {}

    public record DiscordCommandsResourcePack(
            @Comment("Enable the /resourcepack command to show installed resource packs on MultiChatDiscordSRVAddon") boolean enabled,
            @Comment("If you do NOT use a shared channel for multiple servers, this should be set to true.\nIf you do, set this option on ONE of the main servers connected to the discord channel to true.") boolean isMainServer,
            @Comment("The description of this command on Discord") String description
    ) {}

    public record DiscordCommandsPlayerInfo(
            @Comment("Enable the /playerinfo command to show player information") boolean enabled,
            @Comment("If you do NOT use a shared channel for multiple servers, this should be set to true.\nIf you do, set this option on ONE of the main servers connected to the discord channel to true.") boolean isMainServer,
            @Comment("The description of this command on Discord") String description,
            @Comment("The player information to display") DiscordCommandsPlayerInfoFormatting infoFormatting
    ) {}

    public record DiscordCommandsPlayerOrder(
        @Comment(
                """
                        How should players be ordered in the playerlist?\
                        
                        This list is applied from top to bottom.\
                        If two players are tied in an ordering type,\
                        the next one will be applied until the bottom.\
                        If two players are still tied when all ordering types are exhausted,\
                        the player's full tablist text component and (then) UUID string will be used to ensure consistent ordering.\
                        
                        Available Ordering Types:\
                        GROUP:<group,group...> (Permission group ordered by the provided ordering)\
                        PLAYERNAME (Player names ordering from 0 to 9 then A to Z)\
                        PLAYERNAME_REVERSE (Player names ordering from Z to A then 9 to 0)\
                        PLACEHOLDER:<placeholder> (Placeholder ordering from small to large numbers then A to Z)\
                        PLACEHOLDER_REVERSE:<placeholder> (Placeholder ordering from Z to A then large to small numbers)"""
        ) List<String> orderBy
    ) {}

    public record DiscordCommandsPlayerListOptions(
            @Comment("The Vanilla Minecraft client caps the maximum players visible\non the tablist to the first 80 players.\nYou can configure this here, set it to 0 for unlimited.") int maxPlayersDisplayable,
            @Comment("The minimum width of the player name section of the playerlist.\nThis is measured in pixels") int playerMinWidth,
            String sidebarColor,
            @Comment("The formatting of each player name") String playerFormat,
            @Comment("Whether to show the player avatar") boolean showPlayerAvatar,
            @Comment("Whether to show player ping") boolean showPlayerPing,
            @Comment("PlaceholderAPI placeholders in the header are parsed as the first player in the playerlist!\nLeave this variable as a single blank line to disable.") List<String> headerText,
            @Comment("PlaceholderAPI placeholders in the footer are parsed as the first player in the playerlist!\nLeave this variable as a single blank line to disable.") List<String> footerText,
            @Comment("Instead of parsing colour codes for each playername,\nparse MiniMessage tags instead.") boolean parsePlayerNamesWithMiniMessage,
            DiscordCommandsPlayerOrder playerOrder
    ) {}

    public record DiscordCommandsPlayerList(
            @Comment("Enable the /playerlist command to show all online players in a tablist style image") boolean enabled,
            @Comment("The description of this command on Discord") String description,
            @Comment("If you do NOT use a shared channel for multiple servers, this should be set to true.\nIf you do, set this option on ONE of the main servers connected to the discord channel to true.") boolean isMainServer,
            @Comment("List other players on the same network if connected to Bungeecord / Velocity") boolean listBungeecordPlayers,
            @Comment("Message to display when no-one is online") String emptyServer,
            @Comment("Delete the tablist message after X seconds (Set to 0 to not have it automatically deleted)") int deleteAfter,
            DiscordCommandsPlayerListOptions tablistOptions
    ) {}

    public record DiscordCommandsShareItemInGameMessage(
            String text
    ) {}

    public record DiscordCommandsShareInventoryECInGameMessage(
            String text,
            List<String> hover
    ) {}

    public record DiscordCommandsShareItem(
            @Comment("Enable the /item command on Discord") boolean enabled,
            @Comment("Allow the /itemasuser associated command on Discord") boolean allowAsOthers,
            @Comment("If you do NOT use a shared channel for multiple servers, this should be set to true.\nIf you do, set this option on ONE of the main servers connected to the discord channel to true.") boolean isMainServer,
            DiscordCommandsShareItemInGameMessage inGameMessage,
            String inventoryTitle
    ) {}

    public record DiscordCommandsShareInventory(
            @Comment("Enable the /inv command on Discord") boolean enabled,
            @Comment("Allow the /invasuser associate command on Discord") boolean allowAsOthers,
            @Comment("If you do NOT use a shared channel for multiple servers, this should be set to true.\nIf you do, set this option on ONE of the main servers connected to the discord channel to true.") boolean isMainServer,
            DiscordCommandsShareInventoryECInGameMessage inGameMessage,
            String inventoryTitle,
            String skullDisplayName
    ) {}

    public record DiscordCommandsShareEnderChest(
            @Comment("Enable the /ender command on Discord") boolean enabled,
            @Comment("Allow the /enderasuser associated command on Discord") boolean allowAsOthers,
            @Comment("If you do NOT use a shared channel for multiple servers, this should be set to true.\nIf you do, set this option on ONE of the main servers connected to the discord channel to true.") boolean isMainServer,
            DiscordCommandsShareInventoryECInGameMessage inGameMessage,
            String inventoryTitle
    ) {}

    public record DiscordCommands(
            @Comment("These settings apply to all discord commands below") DiscordCommandsGlobalSettings globalSettings,
            @Comment("Settings for /playerinfo") DiscordCommandsPlayerInfo playerInfo,
            @Comment("Settings for /resourcepack") DiscordCommandsResourcePack resourcePack,
            @Comment("Settings for /playerlist") DiscordCommandsPlayerList playerList,
            @Comment("Settings for /item") DiscordCommandsShareItem shareItem,
            @Comment("Settings for /inv") DiscordCommandsShareInventory shareInventory,
            @Comment("Settings for /ender") DiscordCommandsShareEnderChest shareEnderChest
    ) {}

    public record DiscordMention(
            @Comment("Link mentions through DiscordSRV Linked Accounts") boolean translateMentions,
            @Comment("\nSuppress pings on Discord side") boolean suppressDiscordPings,
            @Comment("\nThe sound to play when a player is mentioned.\nSee https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Sound.html") String mentionedSound,
            @Comment("\nThe title to show when mentioned") String mentionedTitle,
            @Comment("\nHow long should the title be shown for when mentioned?\nDuration in seconds.") double mentionedTitleDuration,
            @Comment("\nThe subtitle and action bar to show when mentioned." +
                    "Note that the actionbar might not work if your version of Minecraft does not support it." +
                    "Use {DiscordUser} for the pinger's discordname." +
                    "Use {TextChannel} for the pinged channel name." +
                    "Use {Guild} for the pinged guild name."
            ) String discordMentionSubtitle,
            String discordMentionActionbar,
            @Comment("\nHighlight the mentioned playername for the player mentioned.\nUse {DiscordMention} for the highlighted player name.") String mentionHighlight
    ) {}

    public record ResourcesAlternateServerResourcePack(
            String URL,
            String Hash
    ) {}

    public record Resources(
            @Comment("Which language should be used in places like the item tooltip?\nVisit this page for all the languages that Minecraft offers:\nhttps://minecraft.fandom.com/wiki/Language") String language,
            @Comment("Apply the force unicode setting to languages and fonts") boolean forceUnicodeFont,
            @Comment("Put the server resource pack from server.properties\nat the top of the resource pack ordering list (if available).") boolean includeServerResourcePack,
            @Comment("If ItemsAdder is present and has a resource pack hosted, use its resource pack as the server resource pack.\nIncludeServerResourcePack must be true for this setting to work.") boolean itemsAdderAsServerResourcePack,
            @Comment("Set this option if you want a separate URL as the server resource pack.\nIf the URL is empty, the URL and hash from server.properties will be used.\nIf the hash is empty but URL is not, the resource pack hash will not be checked") ResourcesAlternateServerResourcePack alternateServerResourcePack,
            @Comment("Set which resource pack should be installed and in what order.\nVisit this page for more info:\nhttps://github.com/LOOHP/InteractiveChat-DiscordSRV-Addon/wiki/Resource-Pack") List<String> order,
            @Comment("Enable (Partial) Optifine Custom Texture CIT support") boolean optifineCustomTextures,
            @Comment("Enable (Partial) Chime Custom Model Override support (1.16+)") boolean chimeOverrideModels
    ) {}

    public record InventoryImage(
            @Comment("Whether to generate images for the item placeholder") Item item,
            @Comment("\nWhether to generate images for the inventory placeholder") Inventory inventory,
            @Comment("\nWhether to generate images for the enderchest placeholder") EnderChest enderChest
    ) {}

    public record ChatTabCompletion(
            boolean enabled
    ) {}

    public record TabCompletionPlayerNameTooltip(
            boolean enabled,
            String toolTip
    ) {}

    public record TabCompletion(
            ChatTabCompletion chatTabCompletion,
            TabCompletionPlayerNameTooltip playerNameTooltip
    ) {}

    public record StatusCommandMessages(
            String defaultResourceHash,
            String fontsActive,
            String loadedResources
    ) {}

    public record Messages(
            String reloadConfig,
            String reloadTexture,
            String linkExpired,
            String previewLoading,
            String accountNotLinked,
            String unableToRetrieveData,
            String invalidDiscordChannel,
            String interactionExpired,
            String trueLabel,
            String falseLabel,
            StatusCommandMessages statusCommand
    ) {}

    public record ListenerPriorities(
            @Comment("Defaults to HIGHEST") String gameToDiscord,
            @Comment("Defaults to HIGHEST") String ventureChatToDiscord,
            @Comment("Defaults to HIGH") String discordToGame
    ) {}

    public record RendererSettings(
            @Comment(
                    """
                            Threads used to resolve and render block models.\
                            A positive number sets the absolute number of threads to be used.\
                            A negative number or 0 sets the number of threads to be used as the logical processor count of your system minus N.\
                            Defaults to 2.
                            """
            ) int rendererThreads
    ) {}

    public record FormattingTags(
            @Comment("Allow the use of \"[COLOR=#123456]\" rgb TAGS (1.16+)") boolean allowRGBTags,
            @Comment(
                    """
                            Define custom RGB formats using Regex.\
                            Matching group 1-6 should be EACH o the characters in the RGB hex code.\
                            For example, the regex for "#123456" would be:\
                            "#([0-9a-fA-F])([0-9a-fA-F])([0-9a-fA-F])([0-9a-fA-F])([0-9a-fA-F])([0-9a-fA-F])"
                            """
            ) List<String> additionalRGBFormats
    ) {}

    public record UnknownItem(
            String replaceItem,
            String displayName,
            List<String> lore
    ) {}

    public record Settings(
            @Comment("THIS IS A WORK IN PROGRESS AND NOT FUNCTIONAL AS OF RIGHT NOW!\nUses Bungeecord / Velocity to request the playerlist, etc.") boolean bungeecord,
            @Comment("\nWhether to parse PlaceholderAPI placeholders on the main thread.") boolean parsePAPIOnMainThread,
            @Comment("\nHow long should an item display be stored in memory after its creation?\n(In minutes) - Default 5m") int timeout,
            @Comment("\nSet the cooldown that is shared between all placeholders in this plugin (in seconds).\nPlaceholders will not be parsed in discord messages when a player is in cooldown.") int universalCooldown,
            @Comment("\nMax tag length for items (otherwise it will not be processed).\nGenerally you can leave this as default (30767)") long itemTagMaxLength,
            @Comment("Send the original message if the item exceeds the length limit.") boolean sendOriginalMessageIfExceedLengthLimit,
            @Comment("\nHide Lodestone compass position when sharing them") boolean hideLodestoneCompassPos,
            @Comment("\nIn seconds.") int cacheTimeout,
            boolean escapePlaceholdersSentFromDiscord,
            boolean escapeDiscordMarkdownFormattingInItems,
            boolean reducedAssetsDownloadInfo,
            @Comment("Delete the embed messages sent by MultiChatDiscordSRVAddon after X seconds.\nSet this to 0 (default) to not delete it at all.\nThis does not include ones that have their own config option (e.g. /playerlist)") int embedDeleteAfter,
            @Comment(
                    """
                            You might want to adjust this option depending on whether you have other plugins listening to DiscordSRV's events,\
                            assuming it is interfering with MultiChatDiscordSRVAddon's ability to modify & read game / discord messagaes.\
                            Valid options are:
                            LOWEST, LOW, NORMAL, HIGH, HIGHEST, MONITOR
                            """
            ) ListenerPriorities listenerPriorities,
            RendererSettings rendererSettings,
            FormattingTags formattingTags,
            UnknownItem unknownItem,
            @Comment("Toggle the update checker") boolean updater
    ) {}

    public record DynmapHook(
            @Comment("Whether to filter Dynmap chats through the chat plugin - this will also send them to Discord.") boolean filter,
            @Comment("Fallback if the dynmap chat username is not an online player.") String fallbackName
    ) {}

    public record Hook(
            @Comment("""
                    Chat plugin to hook into.
                    Supported: "ZelChat", "ChatControl" (v11+), "ChatControlRed" (Legacy), "CarbonChat", "InteractiveChat"
                    CASE SENSITIVE! Must be shown as above.
                    """) String selected,
            @Comment("\nWhether to only use channels system if supported in the chat plugin.") boolean useChannels,
            @Comment("\nIf your chat plugin supports \"channels\", you can blacklist certain channels here (CASE SENSITIVE).\nNOTE: ZelChat has two types of channels (as of this time): \"STAFF\" and \"EVERYONE\"") List<String> ignoredChannels,
            @Comment("\nPriority to use for events: LOWEST, LOW, NORMAL, HIGH, HIGHEST, MONITOR\nNOTE: ZelChat does not support the MONITOR priority.") String priority,
            @Comment("\n") DynmapHook dynmap
    ) {}

    public record Debug(
            boolean printInfoToConsole,
            boolean pluginMessagePacketVerbose
    ) {}

    InventoryImage inventoryImage = new InventoryImage(
            new Item(
                    true,
                    0,
                    true,
                    new EmbedDisplay("{Item}", "{Item} x{Amount}"),
                    true,
                    new Frame(
                            new ItemStack(Material.BLACK_STAINED_GLASS_PANE, 1),
                            new ItemStack(Material.WHITE_STAINED_GLASS_PANE)
                    ),
                    "&f[&d%player_name%'s Item&f]",
                    "%player_name%'s Item",
                    "&aShow the Item you are holding in the chat!",
                    true
            ),
            new Inventory(
                    true,
                    0,
                    true,
                    true,
                    "#55FFFF",
                    true,
                    "&f[&d%player_name%'s Inventory&f]",
                    "%player_name%'s Inventory",
                    "&aShow your Inventory in the chat!",
                    new Frame(
                            new ItemStack(Material.BLACK_STAINED_GLASS_PANE, 1),
                            new ItemStack(Material.WHITE_STAINED_GLASS_PANE)
                    )
            ),
            new EnderChest(
                    true,
                    0,
                    "##FF55FF",
                    "&f[&d%player_name%'s Ender Chest&f]",
                    "%player_name%'s Ender Chest",
                    "&dShow your Ender Chest in the chat!"
            )
    );

    @Comment("\nChoose what to include in item tooltips")
    ToolTipSettings toolTipSettings = new ToolTipSettings(
            true,
            true,
            true,
            true,
            true
    );

    @Comment("\nConfigure placeholders used in this plugin")
    Placeholders placeholders = new Placeholders(
            "(?i)\\[item\\]",
            "(?i)\\[inventory\\]",
            "(?i)\\[enderchest\\]"
    );

    @Comment("\nChoose what to include when sharing items and inventories to Discord")
    DiscordItemDetailsAndInteractions discordItemDetailsAndInteractions = new DiscordItemDetailsAndInteractions(
            true,
            true,
            true,
            true
    );

    @Comment("\nConfigure player death handling")
    DeathMessage deathMessage = new DeathMessage(
            true,
            true,
            "Item Weapon"
    );

    @Comment("\nConfigure advancements handling")
    Advancements advancements = new Advancements(
            true,
            true,
            true
    );

    @Comment("\nConfigure hover event displaying")
    HoverEventDisplay hoverEventDisplay = new HoverEventDisplay(
            true,
            true,
            true,
            new ArrayList<>()
    );

    @Comment("\nWIP, NOT FUNCTIONAL")
    DiscordAttachments discordAttachments = new DiscordAttachments(
            true,
            new DiscordAttachmentsFormatting(
                    "&e[&b{FileName}&e]",
                    "&a(Link)",
                    new DiscordAttachmentsFormattingHover(
                            true,
                            List.of("&bClick to Preview"),
                            List.of("&eOpen Original")
                    )
            ),
            true,
            new ImageMapBackground(
                    false,
                    "#36393F"
            ),
            new RestrictImageUrl(
                    false,
                    List.of(
                            "https://tenor.com/",
                            "https://i.imgur.com/",
                            "http://i.imgur.com/",
                            "https://storage.googleapis.com/",
                            "http://storage.googleapis.com/",
                            "https://cdn.discordapp.com/",
                            "http://cdn.discordapp.com/",
                            "https://media.discordapp.net/",
                            "http://media.discordapp.net/",
                            "https://textures.minecraft.net/",
                            "http://textures.minecraft.net/"
                    )
            ),
            8388608,
            86400,
            new PlaybackBar(
                    true,
                    "#FF0000",
                    "#938BB86"
            )
    );

    @Comment("\nConfigure discord commands")
    DiscordCommands discordCommands = new DiscordCommands(
            new DiscordCommandsGlobalSettings(
                    true,
                    new DiscordCommandsGlobalSettingsMessages(
                            "member",
                            "Member to set",
                            "slot",
                            "Slot to choose"
                    )
            ),
            new DiscordCommandsPlayerInfo(
                    true,
                    true,
                    "Show player information on Discord!",
                    new DiscordCommandsPlayerInfoFormatting(
                            "%player_name%'s Player Info",
                            "%discordsrv_user_tag%",
                            List.of(
                                    "%vault_prefix%%player_name%",
                                    "&7Status: &cOnline"
                            ),
                            List.of(
                                    "%vault_prefix%%player_name%",
                                    "&7Status: &Offline"
                            )
                    )
            ),
            new DiscordCommandsResourcePack(
                    true,
                    true,
                    "Show applied resource packs on Discord!"
            ),
            new DiscordCommandsPlayerList(
                    true,
                    "Show all online players!",
                    true,
                    true,
                    "There are no players online.",
                    0,
                    new DiscordCommandsPlayerListOptions(
                            80,
                            0,
                            "#999999",
                            "%vault_prefix%%player_name%",
                            true,
                            true,
                            List.of("&aOnline Players ({OnlinePlayers}/100}"),
                            List.of(""),
                            false,
                            new DiscordCommandsPlayerOrder(
                                    List.of(
                                            "GROUP:owner,admin,member,default",
                                            "PLACEHOLDER_REVERSE:%luckperms_meta_weight%",
                                            "PLAYERNAME"
                                    )
                            )
                    )
            ),
            new DiscordCommandsShareItem(
                    true,
                    true,
                    true,
                    new DiscordCommandsShareItemInGameMessage(
                            "&6{Player} shared an Item: &f{ItemTag}"
                    ),
                    "{Player}'s Item"
            ),
            new DiscordCommandsShareInventory(
                    true,
                    true,
                    true,
                    new DiscordCommandsShareInventoryECInGameMessage(
                            "&6{Player} shared their inventory: &f[&b{Player}'s Inventory&f]",
                            List.of("&bClick to view!")
                    ),
                    "{Player}'s Inventory",
                    "&e{Player}"
            ),
            new DiscordCommandsShareEnderChest(
                    true,
                    true,
                    true,
                    new DiscordCommandsShareInventoryECInGameMessage(
                            "&6{Player} shared their Ender Chest: &f[&d{Player}'s Ender Chest&f]",
                            List.of("&bClick to view!")
                    ),
                    "{Player}'s Ender Chest"
            )
    );

    @Comment("\nConfigure the mentioning of discord players and vice versa.")
    DiscordMention discordMention = new DiscordMention(
            true,
            false,
            "ENTITY_EXPERIENCE_ORB_PICKUP",
            "&c&lMentioned",
            1.5,
            "&9{DiscordUser} &fmentioned you in &9{TextChannel}!",
            "",
            "&9{DiscordMention}"
    );

    @Comment("\nConfigure the resource handling system")
    Resources resources = new Resources(
            "en_us",
            false,
            true,
            true,
            new ResourcesAlternateServerResourcePack("", ""),
            List.of(),
            true,
            true
    );

    @Comment("\nConfigure tab completion")
    TabCompletion tabCompletion = new TabCompletion(
            new ChatTabCompletion(true),
            new TabCompletionPlayerNameTooltip(
                    true,
                    "%vault_prefix%%player_name%"
            )
    );

    @Comment("\nConfigure in-game and Discord messages")
    Messages messages = new Messages(
            "<green>Config has been reloaded!",
            "<yellow>Reloading textures... <grey>(See console for progress)",
            "<red>This link has expired! To view this link, please head over to the linked Discord channel!",
            "<yellow>Preview is being loaded! Please wait...",
            "A Minecraft account must be linked to this Discord account in order to use this command!",
            "<red>Unable to retrieve data.",
            "<You cannot do that in this channel.",
            "This interaction has expired.",
            "<green>True",
            "<red>False",
            new StatusCommandMessages(
                    "<blue>Default Resource Hash: %s",
                    "<blue>Fonts loaded: %s",
                    "<green>Loaded Resources!"
            )
    );

    @Comment("\nGeneral settings concerning this plugin")
    Settings settings = new Settings(
            false,
            false,
            5,
            0,
            30767,
            true,
            true,
            300,
            true,
            true,
            true,
            0,
            new ListenerPriorities(
                    "HIGHEST",
                    "HIGHEST",
                    "HIGH"
            ),
            new RendererSettings(-1),
            new FormattingTags(
                    true,
                    List.of("#([0-9a-fA-F])([0-9a-fA-F])([0-9a-fA-F])([0-9a-fA-F])([0-9a-fA-F])([0-9a-fA-F])")
            ),
            new UnknownItem(
                    "BARRIER",
                    "&cUnknown Item {Type}",
                    List.of(
                            "&7Unable to parse item!",
                            "&7This is likely to be an item that",
                            "&7does not exist in this version of Minecraft."
                    )
            ),
            true
    );

    @Comment("\nPlugin hook configs")
    Hook hook = new Hook(
            "ZelChat",
            true,
            List.of(
                    "Staff",
                    "SomeChannelName"
            ),
            "HIGH",
            new DynmapHook(
                    true,
                    "Dynmap"
            )
    );

    @Comment("\nDebug configurations (useful for bug diagnosis)")
    Debug debug = new Debug(false, false);

    static {
        cachedDataFolder = Objects.requireNonNull(Bukkit.getPluginManager().getPlugin("MultiChatDiscordSrvAddon")).getDataFolder();
    }

    public static Config i() {
        return Objects.requireNonNullElseGet(instance, () -> YamlConfigurations.update(new File(cachedDataFolder, "config.yml").toPath(), Config.class, properties));
    }

    public void saveConfig() {
        YamlConfigurations.save(new File(cachedDataFolder, "config.yml").toPath(), Config.class, this);
    }

    public static void saveConfig(File dataFolder) {
        YamlConfigurations.save(new File(dataFolder, "config.yml").toPath(), Config.class, new Config());
    }

    public void reload(File dataFolder) {
        instance = YamlConfigurations.load(new File(dataFolder, "config.yml").toPath(), Config.class, properties);
    }
}
