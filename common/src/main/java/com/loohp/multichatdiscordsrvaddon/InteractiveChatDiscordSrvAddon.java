/*
 * This file is part of InteractiveChatDiscordSrvAddon.
 *
 * Copyright (C) 2020 - 2025. LoohpJames <jamesloohp@gmail.com>
 * Copyright (C) 2020 - 2025. Contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package com.loohp.multichatdiscordsrvaddon;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.loohp.multichatdiscordsrvaddon.config.Config;
import com.loohp.multichatdiscordsrvaddon.objectholders.*;
import com.loohp.multichatdiscordsrvaddon.utils.MCVersion;
import com.loohp.multichatdiscordsrvaddon.utils.VersionManager;
import com.loohp.multichatdiscordsrvaddon.utils.*;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import com.loohp.multichatdiscordsrvaddon.registry.Registry;
import com.loohp.multichatdiscordsrvaddon.AssetsDownloader.ServerResourcePackDownloadResult;
import com.loohp.multichatdiscordsrvaddon.api.events.ResourceManagerInitializeEvent;
import com.loohp.multichatdiscordsrvaddon.debug.Debug;
import com.loohp.multichatdiscordsrvaddon.graphics.ImageGeneration;
import com.loohp.multichatdiscordsrvaddon.graphics.ImageUtils;
import com.loohp.multichatdiscordsrvaddon.listeners.DiscordCommandEvents;
import com.loohp.multichatdiscordsrvaddon.listeners.DiscordInteractionEvents;
import com.loohp.multichatdiscordsrvaddon.listeners.DiscordReadyEvents;
import com.loohp.multichatdiscordsrvaddon.listeners.ICPlayerEvents;
import com.loohp.multichatdiscordsrvaddon.listeners.InboundToGameEvents;
import com.loohp.multichatdiscordsrvaddon.listeners.LegacyDiscordCommandEvents;
import com.loohp.multichatdiscordsrvaddon.listeners.OutboundToDiscordEvents;
import com.loohp.multichatdiscordsrvaddon.metrics.Charts;
import com.loohp.multichatdiscordsrvaddon.metrics.Metrics;
import com.loohp.multichatdiscordsrvaddon.registry.InteractiveChatRegistry;
import com.loohp.multichatdiscordsrvaddon.registry.ResourceRegistry;
import com.loohp.multichatdiscordsrvaddon.resources.CustomItemTextureRegistry;
import com.loohp.multichatdiscordsrvaddon.resources.ICacheManager;
import com.loohp.multichatdiscordsrvaddon.resources.ModelRenderer;
import com.loohp.multichatdiscordsrvaddon.resources.PackFormat;
import com.loohp.multichatdiscordsrvaddon.resources.ResourceLoadingException;
import com.loohp.multichatdiscordsrvaddon.resources.ResourceManager;
import com.loohp.multichatdiscordsrvaddon.resources.ResourceManager.ModManagerSupplier;
import com.loohp.multichatdiscordsrvaddon.resources.ResourcePackInfo;
import com.loohp.multichatdiscordsrvaddon.resources.ResourcePackType;
import com.loohp.multichatdiscordsrvaddon.resources.fonts.FontManager;
import com.loohp.multichatdiscordsrvaddon.resources.fonts.FontTextureResource;
import com.loohp.multichatdiscordsrvaddon.resources.mods.ModManager;
import com.loohp.multichatdiscordsrvaddon.resources.mods.chime.ChimeManager;
import com.loohp.multichatdiscordsrvaddon.resources.mods.optifine.OptifineManager;
import com.loohp.multichatdiscordsrvaddon.updater.Updater;
import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.api.ListenerPriority;
import github.scarsz.discordsrv.dependencies.jda.api.Permission;
import github.scarsz.discordsrv.dependencies.jda.api.requests.GatewayIntent;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiFunction;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class InteractiveChatDiscordSrvAddon extends JavaPlugin implements Listener {

    public static final int BSTATS_PLUGIN_ID = 8863;
    public static final String CONFIG_ID = "multichatdiscordsrvaddon_config";

    public static final List<Permission> requiredPermissions = Collections.unmodifiableList(Arrays.asList(
        Permission.MESSAGE_READ,
        Permission.MESSAGE_WRITE,
        Permission.MESSAGE_MANAGE,
        Permission.MESSAGE_EMBED_LINKS,
        Permission.MESSAGE_ATTACH_FILES,
        Permission.MANAGE_WEBHOOKS
    ));

    public static InteractiveChatDiscordSrvAddon plugin;
    public static DiscordSRV discordsrv;
    public BukkitAudiences audience;

    public static boolean itemsAdderHook = false;

    public static boolean isReady = false;

    public static boolean debug = false;
    public boolean pluginMessagePacketVerbose = false;

    protected final ReentrantLock resourceReloadLock = new ReentrantLock(true);
    public Metrics metrics;
    public AtomicLong messagesCounter = new AtomicLong(0);
    public AtomicLong imageCounter = new AtomicLong(0);
    public AtomicLong inventoryImageCounter = new AtomicLong(0);
    public AtomicLong attachmentCounter = new AtomicLong(0);
    public AtomicLong attachmentImageCounter = new AtomicLong(0);
    public AtomicLong imagesViewedCounter = new AtomicLong(0);
    public Queue<Integer> playerModelRenderingTimes = new ConcurrentLinkedQueue<>();
    public ListenerPriority gameToDiscordPriority = ListenerPriority.HIGHEST;
    public ListenerPriority ventureChatToDiscordPriority = ListenerPriority.HIGHEST;
    public ListenerPriority discordToGamePriority = ListenerPriority.HIGH;
    public boolean itemImage = true;
    public boolean invImage = true;
    public boolean enderImage = true;
    public boolean usePlayerInvView = true;
    public boolean renderHandHeldItems = true;
    public String itemDisplaySingle = "";
    public String itemDisplayMultiple = "";
    public Color invColor = Color.black;
    public Color enderColor = Color.black;
    public boolean itemUseTooltipImageOnBaseItem = false;
    public boolean itemAltAir = true;
    public String itemTitle = "%player_name%'s Item";
    public String inventoryTitle = "%player_name%'s Inventory";
    public String enderTitle;
    public long itemDisplayTimeout = 300000;
    public boolean hideLodestoneCompassPos = true;
    public boolean invShowLevel = true;
    public boolean hoverEnabled = true;
    public boolean hoverImage = true;
    public Set<String> hoverIgnore = new HashSet<>();
    public boolean hoverUseTooltipImage = true;
    public String reloadConfigMessage;
    public String reloadTextureMessage;
    public String linkExpired;
    public String interactionExpire;
    public String previewLoading;
    public String accountNotLinked;
    public String unableToRetrieveData;
    public String invalidDiscordChannel;
    public String trueLabel;
    public String falseLabel;
    public String defaultResourceHashLang;
    public String fontsActiveLang;
    public String loadedResourcesLang;
    public boolean convertDiscordAttachments = true;
    public String discordAttachmentsFormattingText;
    public boolean discordAttachmentsFormattingHoverEnabled = true;
    public String discordAttachmentsFormattingHoverText;
    public boolean discordAttachmentsImagesUseMaps = true;
    public long discordAttachmentsPreviewLimit = 0;
    public int discordAttachmentTimeout = 0;
    public String discordAttachmentsFormattingImageAppend;
    public String discordAttachmentsFormattingImageAppendHover;
    public Color discordAttachmentsMapBackgroundColor = null;
    public boolean imageWhitelistEnabled = false;
    public List<String> whitelistedImageUrls = new ArrayList<>();
    public boolean translateMentions = true;
    public boolean suppressDiscordPings = false;
    public String mentionHighlight = "";
    public boolean deathMessageItem = true;
    public boolean deathMessageTranslated = true;
    public String deathMessageTitle = "";
    public boolean advancementName = true;
    public boolean advancementItem = true;
    public boolean advancementDescription = true;
    public boolean updaterEnabled = true;
    public int cacheTimeout = 1200;
    public boolean escapePlaceholdersFromDiscord = true;
    public boolean escapeDiscordMarkdownInItems = true;
    public boolean reducedAssetsDownloadInfo = false;
    public boolean playbackBarEnabled = true;
    public Color playbackBarFilledColor;
    public Color playbackBarEmptyColor;
    public String language = "en_us";
    public boolean respondToCommandsInInvalidChannels = true;
    public String discordMemberLabel = "";
    public String discordMemberDescription = "";
    public String discordSlotLabel = "";
    public String discordSlotDescription = "";
    public boolean resourcepackCommandEnabled = true;
    public String resourcepackCommandDescription = "";
    public boolean resourcepackCommandIsMainServer = true;
    public boolean playerinfoCommandEnabled = true;
    public String playerinfoCommandDescription = "";
    public boolean playerinfoCommandIsMainServer = true;
    public String playerinfoCommandFormatTitle = "";
    public String playerinfoCommandFormatSubTitle = "";
    public List<String> playerinfoCommandFormatOnline = new ArrayList<>();
    public List<String> playerinfoCommandFormatOffline = new ArrayList<>();
    public boolean playerlistCommandEnabled = true;
    public String playerlistCommandDescription = "";
    public boolean playerlistCommandIsMainServer = true;
    public boolean playerlistCommandBungeecord = true;
    public int playerlistCommandDeleteAfter = 10;
    public String playerlistCommandPlayerFormat = "";
    public boolean playerlistCommandAvatar = true;
    public boolean playerlistCommandPing = true;
    public String playerlistCommandHeader = "";
    public String playerlistCommandFooter = "";
    public boolean playerlistCommandParsePlayerNamesWithMiniMessage = false;
    public String playerlistCommandEmptyServer = "";
    public Color playerlistCommandColor = new Color(153, 153, 153);
    public int playerlistCommandMinWidth = 0;
    public int playerlistMaxPlayers = 80;
    public List<String> playerlistOrderingTypes = new ArrayList<>();
    public boolean shareItemCommandEnabled = true;
    public boolean shareItemCommandAsOthers = true;
    public boolean shareItemCommandIsMainServer = true;
    public String shareItemCommandInGameMessageText = "";
    public String shareItemCommandTitle = "";
    public boolean shareInvCommandEnabled = true;
    public boolean shareInvCommandAsOthers = true;
    public boolean shareInvCommandIsMainServer = true;
    public String shareInvCommandInGameMessageText = "";
    public String shareInvCommandInGameMessageHover = "";
    public String shareInvCommandTitle = "";
    public String shareInvCommandSkullName = "";
    public boolean shareEnderCommandEnabled = true;
    public boolean shareEnderCommandAsOthers = true;
    public boolean shareEnderCommandIsMainServer = true;
    public String shareEnderCommandInGameMessageText = "";
    public String shareEnderCommandInGameMessageHover = "";
    public String shareEnderCommandTitle = "";
    public String defaultResourceHash = "N/A";
    public List<String> resourceOrder = new ArrayList<>();
    public boolean forceUnicode = false;
    public boolean includeServerResourcePack = true;
    public boolean itemsAdderPackAsServerResourcePack = true;
    public String alternateResourcePackURL = "";
    public String alternateResourcePackHash = "";
    public boolean optifineCustomTextures = true;
    public boolean chimeOverrideModels = true;
    public int embedDeleteAfter = 0;
    public boolean showDurability = true;
    public boolean showArmorColor = true;
    public boolean showMapScale = true;
    public boolean showFireworkRocketDetailsInCrossbow = true;
    public boolean showAdvanceDetails = true;
    public boolean allowSlotSelection = true;
    public boolean showMaps = true;
    public boolean showBooks = true;
    public boolean showContainers = true;
    public int rendererThreads = -1;
    public ItemStack unknownReplaceItem;
    public boolean rgbTags = true;
    public List<Pattern> additionalRGBFormats = new ArrayList<>();
    public boolean useBungeecord = false;
    public boolean parsePAPIOnMainThread = false;
    public boolean chatTabCompletionsEnabled = true;
    public boolean useTooltipOnTab = true;
    public String tabTooltip = "";
    public long universalCooldown = 0;
    public static ICPlaceholder itemPlaceholder = null;
    public static ICPlaceholder inventoryPlaceholder = null;
    public static ICPlaceholder enderChestPlaceholder = null;
    public ItemStack invFrame1 = null;
    public ItemStack invFrame2 = null;
    public ItemStack itemFrame1;
    public ItemStack itemFrame2;
    public static Map<UUID, ICPlaceholder> placeholderList = new LinkedHashMap<>();

    public int itemTagMaxLength = 32767;
    public boolean sendOriginalIfTooLong = false;

    public ConcurrentCacheHashMap<String, Inventory> itemDisplay = new ConcurrentCacheHashMap<>(itemDisplayTimeout, 60000);
    public ConcurrentCacheHashMap<String, Inventory> inventoryDisplay = new ConcurrentCacheHashMap<>(itemDisplayTimeout, 60000);
    public ConcurrentCacheHashMap<String, Inventory> inventoryDisplay1Upper = new ConcurrentCacheHashMap<>(itemDisplayTimeout, 60000);
    public ConcurrentCacheHashMap<String, Inventory> inventoryDisplay1Lower = new ConcurrentCacheHashMap<>(itemDisplayTimeout, 60000);
    public ConcurrentCacheHashMap<String, Inventory> enderDisplay = new ConcurrentCacheHashMap<>(itemDisplayTimeout, 60000);
    public ConcurrentCacheHashMap<String, ItemStack> mapDisplay = new ConcurrentCacheHashMap<>(itemDisplayTimeout, 60000);
    public Set<Inventory> upperSharedInventory = Collections.synchronizedSet(Collections.newSetFromMap(new WeakHashMap<>()));
    public Set<Inventory> lowerSharedInventory = Collections.synchronizedSet(Collections.newSetFromMap(new WeakHashMap<>()));

    public static int remoteDelay = 500;

    public boolean previewMaps = true;

    private ResourceManager resourceManager;
    public ModelRenderer modelRenderer;
    public ExecutorService mediaReadingService;
    public static PlaceholderCooldownManager placeholderCooldownManager;

    protected Map<String, byte[]> extras = new ConcurrentHashMap<>();

    public static Map<Plugin, ValuePairs<Integer, BiFunction<ItemStack, UUID, ItemStack>>> itemStackTransformFunctions = new ConcurrentHashMap<>();

    public ResourceManager getResourceManager() {
        if (resourceManager == null) {
            throw new ResourceLoadingException("Resources are still being loaded, please wait!");
        }
        return resourceManager;
    }

    public boolean isResourceManagerReady() {
        return resourceManager != null;
    }

    @Override
    public void onLoad() {
        DiscordSRV.api.requireIntent(GatewayIntent.GUILD_MESSAGE_REACTIONS);
        DiscordSRV.api.subscribe(new DiscordCommandEvents());
    }

    @Override
    public void onEnable() {
        plugin = this;
        audience = BukkitAudiences.create(this);
        VersionManager.init();
        discordsrv = DiscordSRV.getPlugin();

        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }

        AssetsDownloader.loadLibraries(getDataFolder());

        try {
            Config.loadConfig(CONFIG_ID, new File(getDataFolder(), "config.yml"), getClass().getClassLoader().getResourceAsStream("config.yml"), getClass().getClassLoader().getResourceAsStream("config.yml"), true);
        } catch (IOException e) {
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        reloadConfig();

        metrics = new Metrics(this, BSTATS_PLUGIN_ID);
        Charts.setup(metrics);

        DiscordSRV.api.subscribe(new DiscordReadyEvents());
        DiscordSRV.api.subscribe(new LegacyDiscordCommandEvents());
        DiscordSRV.api.subscribe(new OutboundToDiscordEvents());
        DiscordSRV.api.subscribe(new InboundToGameEvents());

        placeholderCooldownManager = new PlaceholderCooldownManager();

        getServer().getPluginManager().registerEvents(this, this);
        getServer().getPluginManager().registerEvents(new InboundToGameEvents(), this);
        getServer().getPluginManager().registerEvents(new OutboundToDiscordEvents(), this);
        getServer().getPluginManager().registerEvents(new ICPlayerEvents(), this);
        getServer().getPluginManager().registerEvents(new Debug(), this);
        getServer().getPluginManager().registerEvents(new Updater(), this);
        getCommand("multichatdiscordsrv").setExecutor(new Commands());

        File resourcepacks = new File(getDataFolder(), "resourcepacks");
        if (!resourcepacks.exists()) {
            File resources = new File(getDataFolder(), "resources");
            if (resources.exists() && resources.isDirectory()) {
                try {
                    Files.move(resources.toPath(), resourcepacks.toPath(), StandardCopyOption.ATOMIC_MOVE);
                } catch (IOException e) {
                    getServer().getConsoleSender().sendMessage("<red>Unable to move folder, are any files opened?");
                    e.printStackTrace();
                    getServer().getPluginManager().disablePlugin(this);
                    return;
                }
            } else {
                resourcepacks.mkdirs();
            }
        }
        File serverResourcePack = new File(getDataFolder(), "server-resource-packs");
        if (!serverResourcePack.exists()) {
            serverResourcePack.mkdirs();
        }

        if (Bukkit.getServer().getPluginManager().isPluginEnabled("ItemsAdder")) {
            getServer().getConsoleSender().sendMessage("<aqua>MultiChat DiscordSRV Addon has hooked into ItemsAdder!");
            itemsAdderHook = true;
        }

        if (Bukkit.getServer().getPluginManager().isPluginEnabled("InteractiveChat") && !compatible()) {
            for (int i = 0; i < 10; i++) {
                getServer().getConsoleSender().sendMessage("<red>VERSION NOT COMPATIBLE WITH INSTALLED INTERACTIVECHAT VERSION, PLEASE UPDATE BOTH TO LATEST!!!!");
            }
            getServer().getPluginManager().disablePlugin(this);
            return;
        } else {
            getServer().getConsoleSender().sendMessage("<green>MultiChat DiscordSRV Addon has been Enabled!");
        }

        reloadTextures(false, false);
        modelRenderer = new ModelRenderer(str -> new ThreadFactoryBuilder().setNameFormat(str).build(), () -> InteractiveChatDiscordSrvAddon.plugin.cacheTimeout, () -> {
            if (rendererThreads > 0) {
                return rendererThreads;
            }
            return Runtime.getRuntime().availableProcessors() + rendererThreads;
        });

        ThreadFactory factory = new ThreadFactoryBuilder().setNameFormat("MultiChatDiscordSrvAddon Async Media Reading Thread #%d").build();
        mediaReadingService = Executors.newFixedThreadPool(4, factory);

        Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> {
            for (OfflinePlayer player : Bukkit.getOnlinePlayers()) {
                cachePlayerSkin(player);
            }
            AssetsDownloader.loadExtras();
        }, 600, 6000);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Bukkit.getScheduler().runTaskLaterAsynchronously(this, () -> cachePlayerSkin(event.getPlayer()), 40);
    }

    private void cachePlayerSkin(OfflinePlayer player) {
        Debug.debug("Caching skin for player " + player.getName() + " (" + player.getUniqueId() + ")");
        if (Bukkit.getPlayer(player.getUniqueId()) != null) {
            try {
                UUID uuid = player.getUniqueId();
                JSONObject json = (JSONObject) new JSONParser().parse(SkinUtils.getSkinJsonFromProfile(player.getPlayer()));
                String value = (String) ((JSONObject) ((JSONObject) json.get("textures")).get("SKIN")).get("url");
                BufferedImage skin = ImageUtils.downloadImage(value);
                resourceManager.getResourceRegistry(ICacheManager.IDENTIFIER, ICacheManager.class).putCache(uuid + value + ImageGeneration.PLAYER_SKIN_CACHE_KEY, skin);
            } catch (Exception e) {
            }
        } else {
            try {
                UUID uuid = player.getUniqueId();
                String value = SkinUtils.getSkinURLFromUUID(uuid);
                BufferedImage skin = ImageUtils.downloadImage(value);
                resourceManager.getResourceRegistry(ICacheManager.IDENTIFIER, ICacheManager.class).putCache(uuid + "null" + ImageGeneration.PLAYER_SKIN_CACHE_KEY, skin);
            } catch (Exception e) {
            }
        }
    }

    @Override
    public void onDisable() {
        DiscordInteractionEvents.unregisterAll();
        modelRenderer.close();
        mediaReadingService.shutdown();
        if (resourceManager != null) {
            resourceManager.close();
        }
        getServer().getConsoleSender().sendMessage("<red>MultiChat DiscordSRV Addon has been Disabled!");
    }

    public boolean compatible() {
        try {
            return Registry.class.getField("MULTICHAT_DISCORD_SRV_ADDON_COMPATIBLE_VERSION").getInt(null) == InteractiveChatRegistry.class.getField("MULTICHAT_DISCORD_SRV_ADDON_COMPATIBLE_VERSION").getInt(null);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void reloadConfig() {
        Config config = Config.getConfig(CONFIG_ID);
        config.reload();

        reloadConfigMessage = ChatColorUtils.translateAlternateColorCodes('&', config.getConfiguration().getString("Messages.ReloadConfig"));
        reloadTextureMessage = ChatColorUtils.translateAlternateColorCodes('&', config.getConfiguration().getString("Messages.ReloadTexture"));
        linkExpired = ChatColorUtils.translateAlternateColorCodes('&', config.getConfiguration().getString("Messages.LinkExpired"));
        previewLoading = ChatColorUtils.translateAlternateColorCodes('&', config.getConfiguration().getString("Messages.PreviewLoading"));
        accountNotLinked = ChatColorUtils.translateAlternateColorCodes('&', config.getConfiguration().getString("Messages.AccountNotLinked"));
        unableToRetrieveData = ChatColorUtils.translateAlternateColorCodes('&', config.getConfiguration().getString("Messages.UnableToRetrieveData"));
        invalidDiscordChannel = ChatColorUtils.translateAlternateColorCodes('&', config.getConfiguration().getString("Messages.InvalidDiscordChannel"));
        interactionExpire = ChatColorUtils.translateAlternateColorCodes('&', config.getConfiguration().getString("Messages.InteractionExpired"));
        trueLabel = ChatColorUtils.translateAlternateColorCodes('&', config.getConfiguration().getString("Messages.TrueLabel"));
        falseLabel = ChatColorUtils.translateAlternateColorCodes('&', config.getConfiguration().getString("Messages.FalseLabel"));

        defaultResourceHashLang = ChatColorUtils.translateAlternateColorCodes('&', config.getConfiguration().getString("Messages.StatusCommand.DefaultResourceHash"));
        fontsActiveLang = ChatColorUtils.translateAlternateColorCodes('&', config.getConfiguration().getString("Messages.StatusCommand.FontsActive"));
        loadedResourcesLang = ChatColorUtils.translateAlternateColorCodes('&', config.getConfiguration().getString("Messages.StatusCommand.LoadedResources"));

        debug = config.getConfiguration().getBoolean("Debug.PrintInfoToConsole");
        pluginMessagePacketVerbose = config.getConfiguration().getBoolean("Debug.PluginMessagePacketVerbose");

        resourceOrder.clear();
        List<String> order = config.getConfiguration().getStringList("Resources.Order");
        ListIterator<String> itr = order.listIterator(order.size());
        while (itr.hasPrevious()) {
            String pack = itr.previous();
            resourceOrder.add(pack);
        }

        includeServerResourcePack = config.getConfiguration().getBoolean("Resources.IncludeServerResourcePack");
        itemsAdderPackAsServerResourcePack = config.getConfiguration().getBoolean("Resources.ItemsAdderPackAsServerResourcePack");
        alternateResourcePackURL = config.getConfiguration().getString("Resources.AlternateServerResourcePack.URL");
        alternateResourcePackHash = config.getConfiguration().getString("Resources.AlternateServerResourcePack.Hash");
        optifineCustomTextures = config.getConfiguration().getBoolean("Resources.OptifineCustomTextures");
        chimeOverrideModels = config.getConfiguration().getBoolean("Resources.ChimeOverrideModels");

        itemImage = config.getConfiguration().getBoolean("InventoryImage.Item.Enabled");
        invImage = config.getConfiguration().getBoolean("InventoryImage.Inventory.Enabled");
        enderImage = config.getConfiguration().getBoolean("InventoryImage.EnderChest.Enabled");

        usePlayerInvView = config.getConfiguration().getBoolean("InventoryImage.Inventory.UsePlayerInventoryView");
        renderHandHeldItems = config.getConfiguration().getBoolean("InventoryImage.Inventory.RenderHandHeldItems");

        itemUseTooltipImageOnBaseItem = config.getConfiguration().getBoolean("InventoryImage.Item.UseTooltipImageOnBaseItem");
        itemAltAir = config.getConfiguration().getBoolean("InventoryImage.Item.AlternateAirTexture");
        itemTitle = ChatColorUtils.translateAlternateColorCodes('&', config.getConfiguration().getString("InventoryImage.Item.ItemTitle"));
        inventoryTitle = ChatColorUtils.translateAlternateColorCodes('&', config.getConfiguration().getString("InventoryImage.Inventory.InventoryTitle"));
        enderTitle = ChatColorUtils.translateAlternateColorCodes('&', config.getConfiguration().getString("InventoryImage.EnderChest.Text"));
        itemDisplayTimeout = config.getConfiguration().getLong("Settings.Timeout") * 60 * 1000;
        hideLodestoneCompassPos = config.getConfiguration().getBoolean("Settings.HideLodestoneCompassPos");

        invShowLevel = config.getConfiguration().getBoolean("InventoryImage.Inventory.ShowExperienceLevel");

        hoverEnabled = config.getConfiguration().getBoolean("HoverEventDisplay.Enabled");
        hoverImage = config.getConfiguration().getBoolean("HoverEventDisplay.ShowCursorImage");
        hoverIgnore.clear();
        hoverIgnore = new HashSet<>(config.getConfiguration().getStringList("HoverEventDisplay.IgnoredPlaceholderKeys"));

        hoverUseTooltipImage = config.getConfiguration().getBoolean("HoverEventDisplay.UseTooltipImage");

        convertDiscordAttachments = config.getConfiguration().getBoolean("DiscordAttachments.Convert");
        discordAttachmentsFormattingText = ChatColorUtils.translateAlternateColorCodes('&', config.getConfiguration().getString("DiscordAttachments.Formatting.Text"));
        discordAttachmentsFormattingHoverEnabled = config.getConfiguration().getBoolean("DiscordAttachments.Formatting.Hover.Enabled");
        discordAttachmentsFormattingHoverText = ChatColorUtils.translateAlternateColorCodes('&', String.join("\n", config.getConfiguration().getStringList("DiscordAttachments.Formatting.Hover.HoverText")));
        discordAttachmentsImagesUseMaps = config.getConfiguration().getBoolean("DiscordAttachments.ShowImageUsingMaps");
        discordAttachmentsPreviewLimit = config.getConfiguration().getLong("DiscordAttachments.FileSizeLimit");
        discordAttachmentTimeout = config.getConfiguration().getInt("DiscordAttachments.Timeout") * 20;
        discordAttachmentsFormattingImageAppend = ChatColorUtils.translateAlternateColorCodes('&', config.getConfiguration().getString("DiscordAttachments.Formatting.ImageOriginal"));
        discordAttachmentsFormattingImageAppendHover = ChatColorUtils.translateAlternateColorCodes('&', String.join("\n", config.getConfiguration().getStringList("DiscordAttachments.Formatting.Hover.ImageOriginalHover")));

        boolean transparent = config.getConfiguration().getBoolean("DiscordAttachments.ImageMapBackground.Transparent");
        if (transparent) {
            discordAttachmentsMapBackgroundColor = null;
        } else {
            discordAttachmentsMapBackgroundColor = ColorUtils.hex2Rgb(config.getConfiguration().getString("DiscordAttachments.ImageMapBackground.Color"));
        }

        imageWhitelistEnabled = config.getConfiguration().getBoolean("DiscordAttachments.RestrictImageUrl.Enabled");
        whitelistedImageUrls = config.getConfiguration().getStringList("DiscordAttachments.RestrictImageUrl.Whitelist");

        updaterEnabled = config.getConfiguration().getBoolean("Options.UpdaterEnabled");

        cacheTimeout = config.getConfiguration().getInt("Settings.CacheTimeout") * 20;

        escapePlaceholdersFromDiscord = config.getConfiguration().getBoolean("Settings.EscapePlaceholdersSentFromDiscord");
        escapeDiscordMarkdownInItems = config.getConfiguration().getBoolean("Settings.EscapeDiscordMarkdownFormattingInItems");
        reducedAssetsDownloadInfo = config.getConfiguration().getBoolean("Settings.ReducedAssetsDownloadInfo");

        embedDeleteAfter = config.getConfiguration().getInt("Settings.EmbedDeleteAfter");

        gameToDiscordPriority = ListenerPriority.valueOf(config.getConfiguration().getString("Settings.ListenerPriorities.GameToDiscord").toUpperCase());
        ventureChatToDiscordPriority = ListenerPriority.valueOf(config.getConfiguration().getString("Settings.ListenerPriorities.VentureChatToDiscord").toUpperCase());
        discordToGamePriority = ListenerPriority.valueOf(config.getConfiguration().getString("Settings.ListenerPriorities.DiscordToGame").toUpperCase());

        itemDisplaySingle = config.getConfiguration().getString("InventoryImage.Item.EmbedDisplay.Single");
        itemDisplayMultiple = config.getConfiguration().getString("InventoryImage.Item.EmbedDisplay.Multiple");
        invColor = ColorUtils.hex2Rgb(config.getConfiguration().getString("InventoryImage.Inventory.EmbedColor"));
        enderColor = ColorUtils.hex2Rgb(config.getConfiguration().getString("InventoryImage.EnderChest.EmbedColor"));

        deathMessageItem = config.getConfiguration().getBoolean("DeathMessage.ShowItems");
        deathMessageTranslated = config.getConfiguration().getBoolean("DeathMessage.TranslatedDeathMessage");
        deathMessageTitle = config.getConfiguration().getString("DeathMessage.Title");

        advancementName = config.getConfiguration().getBoolean("Advancements.CorrectAdvancementName");
        advancementItem = config.getConfiguration().getBoolean("Advancements.ChangeToItemIcon");
        advancementDescription = config.getConfiguration().getBoolean("Advancements.ShowDescription");

        translateMentions = config.getConfiguration().getBoolean("DiscordMention.TranslateMentions");
        suppressDiscordPings = config.getConfiguration().getBoolean("DiscordMention.SuppressDiscordPings");
        mentionHighlight = ChatColorUtils.translateAlternateColorCodes('&', config.getConfiguration().getString("DiscordMention.MentionHighlight"));

        playbackBarEnabled = config.getConfiguration().getBoolean("DiscordAttachments.PlaybackBar.Enabled");
        playbackBarFilledColor = ColorUtils.hex2Rgb(config.getConfiguration().getString("DiscordAttachments.PlaybackBar.FilledColor"));
        playbackBarEmptyColor = ColorUtils.hex2Rgb(config.getConfiguration().getString("DiscordAttachments.PlaybackBar.EmptyColor"));

        respondToCommandsInInvalidChannels = config.getConfiguration().getBoolean("DiscordCommands.GlobalSettings.RespondToCommandsInInvalidChannels");

        discordMemberLabel = config.getConfiguration().getString("DiscordCommands.GlobalSettings.Messages.MemberLabel").toLowerCase();
        discordMemberDescription = config.getConfiguration().getString("DiscordCommands.GlobalSettings.Messages.MemberDescription");
        discordSlotLabel = config.getConfiguration().getString("DiscordCommands.GlobalSettings.Messages.SlotLabel").toLowerCase();
        discordSlotDescription = config.getConfiguration().getString("DiscordCommands.GlobalSettings.Messages.SlotDescription");

        resourcepackCommandEnabled = config.getConfiguration().getBoolean("DiscordCommands.ResourcePack.Enabled");
        resourcepackCommandDescription = ChatColorUtils.translateAlternateColorCodes('&', config.getConfiguration().getString("DiscordCommands.ResourcePack.Description"));
        resourcepackCommandIsMainServer = config.getConfiguration().getBoolean("DiscordCommands.ResourcePack.IsMainServer");

        playerinfoCommandEnabled = config.getConfiguration().getBoolean("DiscordCommands.PlayerInfo.Enabled");
        playerinfoCommandDescription = ChatColorUtils.translateAlternateColorCodes('&', config.getConfiguration().getString("DiscordCommands.PlayerInfo.Description"));
        playerinfoCommandIsMainServer = config.getConfiguration().getBoolean("DiscordCommands.PlayerInfo.IsMainServer");
        playerinfoCommandFormatTitle = config.getConfiguration().getString("DiscordCommands.PlayerInfo.InfoFormatting.Title");
        playerinfoCommandFormatSubTitle = config.getConfiguration().getString("DiscordCommands.PlayerInfo.InfoFormatting.SubTitle");
        playerinfoCommandFormatOnline = config.getConfiguration().getStringList("DiscordCommands.PlayerInfo.InfoFormatting.WhenOnline");
        playerinfoCommandFormatOffline = config.getConfiguration().getStringList("DiscordCommands.PlayerInfo.InfoFormatting.WhenOffline");

        playerlistCommandEnabled = config.getConfiguration().getBoolean("DiscordCommands.PlayerList.Enabled");
        playerlistCommandDescription = ChatColorUtils.translateAlternateColorCodes('&', config.getConfiguration().getString("DiscordCommands.PlayerList.Description"));
        playerlistCommandIsMainServer = config.getConfiguration().getBoolean("DiscordCommands.PlayerList.IsMainServer");
        playerlistCommandBungeecord = config.getConfiguration().getBoolean("DiscordCommands.PlayerList.ListBungeecordPlayers");
        playerlistCommandDeleteAfter = config.getConfiguration().getInt("DiscordCommands.PlayerList.DeleteAfter");
        playerlistCommandPlayerFormat = ChatColorUtils.translateAlternateColorCodes('&', config.getConfiguration().getString("DiscordCommands.PlayerList.TablistOptions.PlayerFormat"));
        playerlistCommandAvatar = config.getConfiguration().getBoolean("DiscordCommands.PlayerList.TablistOptions.ShowPlayerAvatar");
        playerlistCommandPing = config.getConfiguration().getBoolean("DiscordCommands.PlayerList.TablistOptions.ShowPlayerPing");
        playerlistCommandHeader = ChatColorUtils.translateAlternateColorCodes('&', String.join("\n", config.getConfiguration().getStringList("DiscordCommands.PlayerList.TablistOptions.HeaderText")));
        playerlistCommandFooter = ChatColorUtils.translateAlternateColorCodes('&', String.join("\n", config.getConfiguration().getStringList("DiscordCommands.PlayerList.TablistOptions.FooterText")));
        playerlistCommandParsePlayerNamesWithMiniMessage = config.getConfiguration().getBoolean("DiscordCommands.PlayerList.TablistOptions.ParsePlayerNamesWithMiniMessage");
        playerlistCommandEmptyServer = ChatColorUtils.translateAlternateColorCodes('&', config.getConfiguration().getString("DiscordCommands.PlayerList.EmptyServer"));
        playerlistCommandColor = ColorUtils.hex2Rgb(config.getConfiguration().getString("DiscordCommands.PlayerList.TablistOptions.SidebarColor"));
        playerlistCommandMinWidth = config.getConfiguration().getInt("DiscordCommands.PlayerList.TablistOptions.PlayerMinWidth");
        playerlistMaxPlayers = config.getConfiguration().getInt("DiscordCommands.PlayerList.TablistOptions.MaxPlayersDisplayable");
        playerlistOrderingTypes = config.getConfiguration().getStringList("DiscordCommands.PlayerList.TablistOptions.PlayerOrder.OrderBy");

        shareItemCommandEnabled = config.getConfiguration().getBoolean("DiscordCommands.ShareItem.Enabled");
        shareItemCommandAsOthers = config.getConfiguration().getBoolean("DiscordCommands.ShareItem.AllowAsOthers");
        shareItemCommandIsMainServer = config.getConfiguration().getBoolean("DiscordCommands.ShareItem.IsMainServer");
        shareItemCommandInGameMessageText = ChatColorUtils.translateAlternateColorCodes('&', config.getConfiguration().getString("DiscordCommands.ShareItem.InGameMessage.Text"));
        shareItemCommandTitle = ChatColorUtils.translateAlternateColorCodes('&', config.getConfiguration().getString("DiscordCommands.ShareItem.InventoryTitle"));

        shareInvCommandEnabled = config.getConfiguration().getBoolean("DiscordCommands.ShareInventory.Enabled");
        shareInvCommandAsOthers = config.getConfiguration().getBoolean("DiscordCommands.ShareInventory.AllowAsOthers");
        shareInvCommandIsMainServer = config.getConfiguration().getBoolean("DiscordCommands.ShareInventory.IsMainServer");
        shareInvCommandInGameMessageText = ChatColorUtils.translateAlternateColorCodes('&', config.getConfiguration().getString("DiscordCommands.ShareInventory.InGameMessage.Text"));
        shareInvCommandInGameMessageHover = ChatColorUtils.translateAlternateColorCodes('&', String.join("\n", config.getConfiguration().getStringList("DiscordCommands.ShareInventory.InGameMessage.Hover")));
        shareInvCommandTitle = ChatColorUtils.translateAlternateColorCodes('&', config.getConfiguration().getString("DiscordCommands.ShareInventory.InventoryTitle"));
        shareInvCommandSkullName = ChatColorUtils.translateAlternateColorCodes('&', config.getConfiguration().getString("DiscordCommands.ShareInventory.SkullDisplayName"));

        shareEnderCommandEnabled = config.getConfiguration().getBoolean("DiscordCommands.ShareEnderChest.Enabled");
        shareEnderCommandAsOthers = config.getConfiguration().getBoolean("DiscordCommands.ShareEnderChest.AllowAsOthers");
        shareEnderCommandIsMainServer = config.getConfiguration().getBoolean("DiscordCommands.ShareEnderChest.IsMainServer");
        shareEnderCommandInGameMessageText = ChatColorUtils.translateAlternateColorCodes('&', config.getConfiguration().getString("DiscordCommands.ShareEnderChest.InGameMessage.Text"));
        shareEnderCommandInGameMessageHover = ChatColorUtils.translateAlternateColorCodes('&', String.join("\n", config.getConfiguration().getStringList("DiscordCommands.ShareEnderChest.InGameMessage.Hover")));
        shareEnderCommandTitle = ChatColorUtils.translateAlternateColorCodes('&', config.getConfiguration().getString("DiscordCommands.ShareEnderChest.InventoryTitle"));

        showDurability = config.getConfiguration().getBoolean("ToolTipSettings.ShowDurability");
        showArmorColor = config.getConfiguration().getBoolean("ToolTipSettings.ShowArmorColor");
        showMapScale = config.getConfiguration().getBoolean("ToolTipSettings.ShowMapScale");
        showFireworkRocketDetailsInCrossbow = config.getConfiguration().getBoolean("ToolTipSettings.ShowFireworkRocketDetailsInCrossbow");
        showAdvanceDetails = config.getConfiguration().getBoolean("ToolTipSettings.ShowAdvanceDetails");

        allowSlotSelection = config.getConfiguration().getBoolean("DiscordItemDetailsAndInteractions.AllowInventorySelection");
        showMaps = config.getConfiguration().getBoolean("DiscordItemDetailsAndInteractions.ShowMaps");
        showBooks = config.getConfiguration().getBoolean("DiscordItemDetailsAndInteractions.ShowBooks");
        showContainers = config.getConfiguration().getBoolean("DiscordItemDetailsAndInteractions.ShowContainers");

        rendererThreads = config.getConfiguration().getInt("Settings.RendererSettings.RendererThreads");
        useBungeecord = config.getConfiguration().getBoolean("Settings.Bungeecord");
        parsePAPIOnMainThread = config.getConfiguration().getBoolean("Settings.parsePAPIOnMainThread");

        chatTabCompletionsEnabled = config.getConfiguration().getBoolean("TabCompletion.ChatTabCompletion.Enabled");
        useTooltipOnTab = config.getConfiguration().getBoolean("TabCompletion.PlayerNameTooltip.Enabled");
        tabTooltip = ChatColorUtils.translateAlternateColorCodes('&', config.getConfiguration().getString("TabCompletion.PlayerNameTooltip.Tooltip"));

        previewMaps = config.getConfiguration().getBoolean("InventoryImage.Item.PreviewMaps");

        try {
            ItemStack unknown = new ItemStack(Material.valueOf(getConfig().getString("Settings.UnknownItem.ReplaceItem").toUpperCase()));
            ItemMeta meta = unknown.getItemMeta();
            meta.setDisplayName(ChatColorUtils.translateAlternateColorCodes('&', getConfig().getString("Settings.UnknownItem.DisplayName")));
            meta.setLore(getConfig().getStringList("Settings.UnknownItem.Lore").stream().map(each -> ChatColorUtils.translateAlternateColorCodes('&', each)).collect(Collectors.toList()));
            unknown.setItemMeta(meta);
            this.unknownReplaceItem = unknown;
        } catch (Exception e) {
            ItemStack unknown = ICMaterial.from(getConfig().getString("Settings.UnknownItem.ReplaceItem")).parseItem();
            unknown.setAmount(1);
            ItemMeta meta = unknown.getItemMeta();
            meta.setDisplayName(ChatColorUtils.translateAlternateColorCodes('&', getConfig().getString("Settings.UnknownItem.DisplayName")));
            meta.setLore(getConfig().getStringList("Settings.UnknownItem.Lore").stream().map(each -> ChatColorUtils.translateAlternateColorCodes('&', each)).collect(Collectors.toList()));
            unknown.setItemMeta(meta);
            this.unknownReplaceItem = unknown;
        }

        rgbTags = config.getConfiguration().getBoolean("Settings.FormattingTags.AllowRGBTags");
        additionalRGBFormats = config.getConfiguration().getStringList("Settings.FormattingTags.AdditionalRGBFormats").stream().map(each -> Pattern.compile(each)).collect(Collectors.toList());;

        language = config.getConfiguration().getString("Resources.Language");
        LanguageUtils.loadTranslations(language);
        forceUnicode = config.getConfiguration().getBoolean("Resources.ForceUnicodeFont");

        itemTagMaxLength = config.getConfiguration().getInt("Settings.ItemTagMaxLength");
        sendOriginalIfTooLong = config.getConfiguration().getBoolean("Settings.SendOriginalMessageIfExceedLengthLimit");

        Pattern itemPlaceholderPattern = Pattern.compile(config.getConfiguration().getString("Placeholders.Item"));
        Pattern inventoryPlaceholderPattern = Pattern.compile(config.getConfiguration().getString("Placeholders.Inventory"));
        Pattern enderChestPlaceholderPattern = Pattern.compile(config.getConfiguration().getString("Placeholders.EnderChest"));

        if (invImage) {
            itemPlaceholder = new BuiltInPlaceholder(itemPlaceholderPattern, itemTitle, "", "", config.getConfiguration().getLong("InventoryImage.Item.Cooldown") * 1000);
            placeholderList.put(itemPlaceholder.getInternalId(), itemPlaceholder);
        }
        if (invImage) {
            inventoryPlaceholder = new BuiltInPlaceholder(inventoryPlaceholderPattern, "", "", "", config.getConfiguration().getLong("InventoryImage.Inventory.Cooldown") * 1000);
            placeholderList.put(inventoryPlaceholder.getInternalId(), inventoryPlaceholder);
        }
        if (enderImage) {
            enderChestPlaceholder = new BuiltInPlaceholder(enderChestPlaceholderPattern, "", "", "", config.getConfiguration().getLong("InventoryImage.EnderChest.Cooldown") * 1000);
            placeholderList.put(enderChestPlaceholder.getInternalId(), enderChestPlaceholder);
        }

        invFrame1 = new ItemStack(Material.valueOf(getConfig().getString("InventoryImage.Inventory.Frame.Primary")), 1);
        invFrame2 = new ItemStack(Material.valueOf(getConfig().getString("InventoryImage.Inventory.Frame.Secondary")), 1);

        itemFrame1 = new ItemStack(Material.valueOf(getConfig().getString("InventoryImage.Item.Frame.Primary")), 1);
        itemFrame2 = new ItemStack(Material.valueOf(getConfig().getString("InventoryImage.Item.Frame.Secondary")), 1);

        universalCooldown = config.getConfiguration().getLong("Settings.UniversalCooldown") * 1000;

        FontTextureResource.setCacheTime(cacheTimeout);

        discordsrv.reloadRegexes();
    }

    public byte[] getExtras(String str) {
        return extras.get(str);
    }

    public void reloadTextures(boolean redownload, boolean clean, CommandSender... receivers) {
        CommandSender[] senders;
        if (Arrays.stream(receivers).noneMatch(each -> each.equals(Bukkit.getConsoleSender()))) {
            senders = Arrays.copyOf(receivers, receivers.length + 1);
            senders[senders.length - 1] = Bukkit.getConsoleSender();
        } else {
            senders = receivers;
        }

        Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
            try {
                if (!resourceReloadLock.tryLock(0, TimeUnit.MILLISECONDS)) {
                    sendMessage("<yellow>Resource reloading already in progress!", senders);
                    return;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                return;
            }
            try {
                isReady = false;
                if (InteractiveChatDiscordSrvAddon.plugin.isResourceManagerReady()) {
                    Bukkit.getScheduler().callSyncMethod(plugin, () -> {
                        InteractiveChatDiscordSrvAddon.plugin.getResourceManager().close();
                        return null;
                    }).get();
                }
                try {
                    AssetsDownloader.loadAssets(getDataFolder(), redownload, clean, receivers);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                List<String> resourceList = new ArrayList<>();
                resourceList.add("Default");
                resourceList.addAll(resourceOrder);

                File serverResourcePackFolder = new File(getDataFolder(), "server-resource-packs");
                File serverResourcePack = null;
                if (includeServerResourcePack) {
                    Bukkit.getConsoleSender().sendMessage("<gray>Checking for server resource pack...");
                    ServerResourcePackDownloadResult result = AssetsDownloader.downloadServerResourcePack(serverResourcePackFolder);
                    serverResourcePack = result.getResourcePackFile();
                    if (result.getError() != null) {
                        result.getError().printStackTrace();
                    }
                    switch (result.getType()) {
                        case SUCCESS_NO_CHANGES:
                            sendMessage("<green>Server resource pack found with verification hash: No changes", senders);
                            resourceList.add(serverResourcePack.getName());
                            break;
                        case SUCCESS_WITH_HASH:
                            sendMessage("<green>Server resource pack found with verification hash: Hash changed, downloaded", senders);
                            resourceList.add(serverResourcePack.getName());
                            break;
                        case SUCCESS_NO_HASH:
                            sendMessage("<green>Server resource pack found without verification hash: Downloaded", senders);
                            resourceList.add(serverResourcePack.getName());
                            break;
                        case FAILURE_WRONG_HASH:
                            sendMessage("<red>Server resource pack had wrong hash (expected " + result.getExpectedHash() + ", found " + result.getPackHash() + ")", senders);
                            sendMessage("<red>Server resource pack will not be applied: Hash check failure", senders);
                            break;
                        case FAILURE_DOWNLOAD:
                            sendMessage("<red>Failed to download server resource pack", senders);
                            break;
                        case NO_PACK:
                            Bukkit.getConsoleSender().sendMessage("<gray>No server resource pack found");
                            break;
                    }
                }

                sendMessage("<aqua>Reloading ResourceManager: <yellow>" + String.join(", ", resourceList), senders);

                List<ModManagerSupplier<?>> mods = new ArrayList<>();
                if (chimeOverrideModels) {
                    mods.add(manager -> new ChimeManager(manager));
                }
                if (optifineCustomTextures) {
                    mods.add(manager -> new OptifineManager(manager));
                }

                Bukkit.getPluginManager().callEvent(new ResourceManagerInitializeEvent(mods));

                @SuppressWarnings("resource")
                ResourceManager resourceManager = new ResourceManager(
                        ResourcePackUtils.getServerResourcePackVersion(),
                        mods,
                        Arrays.asList(CustomItemTextureRegistry.getDefaultSupplier(), ICacheManager.getDefaultSupplier(new File(getDataFolder(), "cache"))),
                        (resourcePackFile, type) -> new ResourceManager.DefaultResourcePackInfo(
                                Component.translatable(TranslationKeyUtils.getResourcePackVanillaName()),
                                PackFormat.version(ResourcePackUtils.getServerResourcePackVersion()),
                                Component.translatable(TranslationKeyUtils.getResourcePackVanillaDescription()).append(Component.text(" (Modified by LOOHP)"))
                        ),
                        ResourceManager.Flag.build(
                                VersionManager.version.isLegacy(),
                                false,
                                VersionManager.version.isOlderOrEqualTo(MCVersion.V1_21_3)
                        )
                );

                for (Entry<String, ModManager> entry : resourceManager.getModManagers().entrySet()) {
                    Bukkit.getConsoleSender().sendMessage("<gray>Registered ModManager \"" + entry.getKey() + "\" of class \"" + entry.getValue().getClass().getName() + "\"");
                }

                resourceManager.getFontManager().setDefaultKey(forceUnicode ? FontManager.UNIFORM_FONT : FontManager.DEFAULT_FONT);
                resourceManager.getLanguageManager().setTranslateFunction((translateKey, fallback, language) -> LanguageUtils.getTranslation(translateKey, language).getResultOrFallback(fallback));
                resourceManager.getLanguageManager().setAvailableLanguagesSupplier(() -> LanguageUtils.getLoadedLanguages());
                resourceManager.getLanguageManager().registerReloadListener(e -> {
                    LanguageUtils.clearPluginTranslations(InteractiveChatDiscordSrvAddon.plugin);
                    for (Entry<String, Map<String, String>> entry : e.getTranslations().entrySet()) {
                        LanguageUtils.loadPluginTranslations(InteractiveChatDiscordSrvAddon.plugin, entry.getKey(), entry.getValue());
                    }
                });

                Bukkit.getConsoleSender().sendMessage("<aqua>Loading \"Default\" resources...");
                resourceManager.loadResources(new File(getDataFolder() + "/built-in", "Default"), ResourcePackType.BUILT_IN, true);
                for (String resourceName : resourceOrder) {
                    try {
                        Bukkit.getConsoleSender().sendMessage("<aqua>Loading \"" + resourceName + "\" resources...");
                        File resourcePackFile = new File(getDataFolder(), "resourcepacks/" + resourceName);
                        ResourcePackInfo info = resourceManager.loadResources(resourcePackFile, ResourcePackType.LOCAL);
                        if (info.getStatus()) {
                            if (info.compareServerPackFormat(ResourceRegistry.RESOURCE_PACK_VERSION) > 0) {
                                sendMessage("<yellow>Warning: \"" + resourceName + "\" was made for a newer version of Minecraft!", senders);
                            } else if (info.compareServerPackFormat(ResourceRegistry.RESOURCE_PACK_VERSION) < 0) {
                                sendMessage("<yellow>Warning: \"" + resourceName + "\" was made for an older version of Minecraft!", senders);
                            }
                        } else {
                            if (info.getRejectedReason() == null) {
                                sendMessage("<red>Unable to load \"" + resourceName + "\"", senders);
                            } else {
                                sendMessage("<red>Unable to load \"" + resourceName + "\", Reason: " + info.getRejectedReason(), senders);
                            }
                        }
                    } catch (Exception e) {
                        sendMessage("<red>Unable to load \"" + resourceName + "\"", senders);
                        e.printStackTrace();
                    }
                }
                if (includeServerResourcePack && serverResourcePack != null && serverResourcePack.exists()) {
                    String resourceName = serverResourcePack.getName();
                    try {
                        Bukkit.getConsoleSender().sendMessage("<aqua>Loading \"" + resourceName + "\" resources...");
                        ResourcePackInfo info = resourceManager.loadResources(serverResourcePack, ResourcePackType.SERVER);
                        if (info.getStatus()) {
                            if (info.compareServerPackFormat(ResourceRegistry.RESOURCE_PACK_VERSION) > 0) {
                                sendMessage("<yellow>Warning: \"" + resourceName + "\" was made for a newer version of Minecraft!", senders);
                            } else if (info.compareServerPackFormat(ResourceRegistry.RESOURCE_PACK_VERSION) < 0) {
                                sendMessage("<yellow>Warning: \"" + resourceName + "\" was made for an older version of Minecraft!", senders);
                            }
                        } else {
                            if (info.getRejectedReason() == null) {
                                sendMessage("<red>Unable to load \"" + resourceName + "\"", senders);
                            } else {
                                sendMessage("<red>Unable to load \"" + resourceName + "\", Reason: " + info.getRejectedReason(), senders);
                            }
                        }
                    } catch (Exception e) {
                        sendMessage("<red>Unable to load \"" + resourceName + "\"", senders);
                        e.printStackTrace();
                    }
                }

                Bukkit.getScheduler().callSyncMethod(plugin, () -> {
                    InteractiveChatDiscordSrvAddon.plugin.resourceManager = resourceManager;

                    if (resourceManager.getResourcePackInfo().stream().allMatch(each -> each.getStatus())) {
                        sendMessage("<aqua>Loaded all resources!", senders);
                        isReady = true;
                    } else {
                        sendMessage("<red>There is a problem while loading resources.", senders);
                    }
                    return null;
                }).get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            } finally {
                resourceReloadLock.unlock();
            }
        });
    }

    public void sendMessage(Object message, CommandSender... senders) {
        for (CommandSender sender : senders) {
            audience.sender(sender).sendMessage(message instanceof Component ? MiniMessage.miniMessage().deserialize(ICLogger.PREFIX).append(Component.text(" ")).append((Component) message) : MiniMessage.miniMessage().deserialize(ICLogger.PREFIX + " " + message));
        }
    }

}
