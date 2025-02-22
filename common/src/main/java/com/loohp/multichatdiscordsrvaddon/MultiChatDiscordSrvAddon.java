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

import com.github.retrooper.packetevents.PacketEvents;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.loohp.multichatdiscordsrvaddon.bungee.BungeeMessageListener;
import com.loohp.multichatdiscordsrvaddon.command.CommandHandler;
import com.loohp.multichatdiscordsrvaddon.config.Config;
import com.loohp.multichatdiscordsrvaddon.hooks.DynmapHook;
import com.loohp.multichatdiscordsrvaddon.integration.IntegrationManager;
import com.loohp.multichatdiscordsrvaddon.objectholders.*;
import com.loohp.multichatdiscordsrvaddon.registry.MultiChatRegistry;
import com.loohp.multichatdiscordsrvaddon.utils.*;
import me.lucko.helper.plugin.ExtendedJavaPlugin;
import net.kyori.adventure.text.Component;
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

public class MultiChatDiscordSrvAddon extends ExtendedJavaPlugin implements Listener {

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

    public static MultiChatDiscordSrvAddon plugin;
    public static DiscordSRV discordsrv;

    public static boolean isReady = false;
    public String defaultResourceHash = "N/A";
    public List<String> resourceOrder = new ArrayList<>();
    public ItemStack unknownReplaceItem;
    public List<Pattern> additionalRGBFormats;

    public final ReentrantLock resourceReloadLock = new ReentrantLock(true);
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
    public static ICPlaceholder itemPlaceholder = null;
    public static ICPlaceholder inventoryPlaceholder = null;
    public static ICPlaceholder enderChestPlaceholder = null;
    public static Map<UUID, ICPlaceholder> placeholderList = new LinkedHashMap<>();

    public static BungeeMessageListener bungeeMessageListener;

    public ConcurrentCacheHashMap<String, Inventory> itemDisplay;
    public ConcurrentCacheHashMap<String, Inventory> inventoryDisplay;
    public ConcurrentCacheHashMap<String, Inventory> inventoryDisplay1Upper;
    public ConcurrentCacheHashMap<String, Inventory> inventoryDisplay1Lower;
    public ConcurrentCacheHashMap<String, Inventory> enderDisplay;
    public ConcurrentCacheHashMap<String, ItemStack> mapDisplay;
    public Set<Inventory> upperSharedInventory = Collections.synchronizedSet(Collections.newSetFromMap(new WeakHashMap<>()));
    public Set<Inventory> lowerSharedInventory = Collections.synchronizedSet(Collections.newSetFromMap(new WeakHashMap<>()));

    public static int remoteDelay = 500;

    private ResourceManager resourceManager;
    public ModelRenderer modelRenderer;
    public ExecutorService mediaReadingService;
    public static PlaceholderCooldownManager placeholderCooldownManager;

    public InboundToGameEvents inboundToGameEvents;

    public IntegrationManager integrationManager;

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
    public void load() {
        DiscordSRV.api.requireIntent(GatewayIntent.GUILD_MESSAGE_REACTIONS);
        DiscordSRV.api.subscribe(new DiscordCommandEvents());
    }

    @Override
    public void enable() {
        plugin = this;

        PacketEvents.getAPI().init();

        ChatUtils.init(this);
        VersionManager.init();
        discordsrv = DiscordSRV.getPlugin();

        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }

        processConfigs();

        AssetsDownloader.loadLibraries(getDataFolder());

        long itemDisplayTimeout = Config.i().getSettings().timeout() * 60L * 1000L;
        itemDisplay = new ConcurrentCacheHashMap<>(itemDisplayTimeout, 60000);
        inventoryDisplay = new ConcurrentCacheHashMap<>(itemDisplayTimeout, 60000);
        inventoryDisplay1Upper = new ConcurrentCacheHashMap<>(itemDisplayTimeout, 60000);
        inventoryDisplay1Lower = new ConcurrentCacheHashMap<>(itemDisplayTimeout, 60000);
        enderDisplay = new ConcurrentCacheHashMap<>(itemDisplayTimeout, 60000);
        mapDisplay = new ConcurrentCacheHashMap<>(itemDisplayTimeout, 60000);

        integrationManager = new IntegrationManager();
        if (Config.i().getHook().shouldHook()) integrationManager.load(Config.i().getHook().selected());

        metrics = new Metrics(this, BSTATS_PLUGIN_ID);
        Charts.setup(metrics);

        getServer().getMessenger().registerOutgoingPluginChannel(this, "interchat:main");
        getServer().getMessenger().registerIncomingPluginChannel(this, "interchat:main", bungeeMessageListener = new BungeeMessageListener());

        DiscordSRV.api.subscribe(new DiscordReadyEvents());
        DiscordSRV.api.subscribe(new LegacyDiscordCommandEvents());
        DiscordSRV.api.subscribe(new OutboundToDiscordEvents());

        inboundToGameEvents = new InboundToGameEvents();
        DiscordSRV.api.subscribe(inboundToGameEvents);

        placeholderCooldownManager = new PlaceholderCooldownManager();

        getServer().getPluginManager().registerEvents(this, this);
        getServer().getPluginManager().registerEvents(new InboundToGameEvents(), this);
        getServer().getPluginManager().registerEvents(new OutboundToDiscordEvents(), this);
        getServer().getPluginManager().registerEvents(new ICPlayerEvents(), this);
        getServer().getPluginManager().registerEvents(new Updater(), this);

        new CommandHandler();

        File resourcepacks = new File(getDataFolder(), "resourcepacks");
        if (!resourcepacks.exists()) {
            File resources = new File(getDataFolder(), "resources");
            if (resources.exists() && resources.isDirectory()) {
                try {
                    Files.move(resources.toPath(), resourcepacks.toPath(), StandardCopyOption.ATOMIC_MOVE);
                } catch (IOException e) {
                    ChatUtils.sendMessage("<red>Unable to move folder, are any files opened?", Bukkit.getConsoleSender());
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

        if (Config.i().getHook().dynmap().filter() && Bukkit.getPluginManager().isPluginEnabled("dynmap")) {
            ChatUtils.sendMessage("<yellow>Hooking into Dynmap...");
            new DynmapHook().init();
            ChatUtils.sendMessage("<green>Hooked into Dynmap!");
        }

        if (Bukkit.getServer().getPluginManager().isPluginEnabled("InteractiveChat") && !compatible()) {
            for (int i = 0; i < 10; i++) {
                ChatUtils.sendMessage("<red>VERSION NOT COMPATIBLE WITH INSTALLED INTERACTIVECHAT VERSION, PLEASE UPDATE BOTH TO LATEST!!!!", Bukkit.getConsoleSender());
            }
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        ChatUtils.sendMessage("<green>MultiChat DiscordSRV Addon has been enabled.");

        reloadTextures(false, false);
        modelRenderer = new ModelRenderer(str -> new ThreadFactoryBuilder().setNameFormat(str).build(), () -> Config.i().getSettings().cacheTimeout() * 20L, () -> {
            if (Config.i().getSettings().rendererSettings().rendererThreads() > 0) {
                return Config.i().getSettings().rendererSettings().rendererThreads();
            }
            return Runtime.getRuntime().availableProcessors() + Config.i().getSettings().rendererSettings().rendererThreads();
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
            } catch (Exception ignored) {
            }
        } else {
            try {
                UUID uuid = player.getUniqueId();
                String value = SkinUtils.getSkinURLFromUUID(uuid);
                BufferedImage skin = ImageUtils.downloadImage(value);
                resourceManager.getResourceRegistry(ICacheManager.IDENTIFIER, ICacheManager.class).putCache(uuid + "null" + ImageGeneration.PLAYER_SKIN_CACHE_KEY, skin);
            } catch (Exception ignored) {
            }
        }
    }

    @Override
    public void disable() {
        PacketEvents.getAPI().terminate();
        DiscordInteractionEvents.unregisterAll();

        if (modelRenderer != null) modelRenderer.close();
        if (mediaReadingService != null) mediaReadingService.shutdown();
        if (resourceManager != null) resourceManager.close();

        ChatUtils.sendMessage("<red>MultiChat DiscordSRV Addon has been disabled.", Bukkit.getConsoleSender());
    }

    public boolean compatible() {
        try {
            return Registry.class.getField("MULTICHAT_DISCORD_SRV_ADDON_COMPATIBLE_VERSION").getInt(null) == MultiChatRegistry.class.getField("MULTICHAT_DISCORD_SRV_ADDON_COMPATIBLE_VERSION").getInt(null);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void processConfigs() {
        resourceOrder.clear();
        List<String> order = Config.i().getResources().order();
        ListIterator<String> itr = order.listIterator(order.size());
        while (itr.hasPrevious()) {
            String pack = itr.previous();
            resourceOrder.add(pack);
        }

        additionalRGBFormats = Config.i().getSettings().formattingTags().additionalRGBFormats().stream().map(Pattern::compile).collect(Collectors.toList());

        try {
            ItemStack unknown = new ItemStack(Material.valueOf(Config.i().getSettings().unknownItem().replaceItem().toUpperCase()));
            ItemMeta meta = unknown.getItemMeta();
            meta.setDisplayName(ChatColorUtils.translateAlternateColorCodes('&', Config.i().getSettings().unknownItem().displayName()));
            meta.setLore(Config.i().getSettings().unknownItem().lore().stream().map(each -> ChatColorUtils.translateAlternateColorCodes('&', each)).collect(Collectors.toList()));
            unknown.setItemMeta(meta);
            this.unknownReplaceItem = unknown;
        } catch (Exception e) {
            ItemStack unknown = ICMaterial.from(Config.i().getSettings().unknownItem().replaceItem()).parseItem();
            unknown.setAmount(1);
            ItemMeta meta = unknown.getItemMeta();
            meta.setDisplayName(ChatColorUtils.translateAlternateColorCodes('&', Config.i().getSettings().unknownItem().displayName()));
            meta.setLore(Config.i().getSettings().unknownItem().lore().stream().map(each -> ChatColorUtils.translateAlternateColorCodes('&', each)).collect(Collectors.toList()));
            unknown.setItemMeta(meta);
            this.unknownReplaceItem = unknown;
        }

        LanguageUtils.loadTranslations(Config.i().getResources().language());

        Pattern itemPlaceholderPattern = Pattern.compile(Config.i().getPlaceholders().item());
        Pattern inventoryPlaceholderPattern = Pattern.compile(Config.i().getPlaceholders().inventory());
        Pattern enderChestPlaceholderPattern = Pattern.compile(Config.i().getPlaceholders().enderChest());

        if (Config.i().getInventoryImage().item().enabled()) {
            String description = ChatColorUtils.translateAlternateColorCodes('&', Config.i().getInventoryImage().item().description());
            itemPlaceholder = new BuiltInPlaceholder(itemPlaceholderPattern, Config.i().getInventoryImage().item().itemTitle(), description, "", Config.i().getInventoryImage().item().cooldown() * 1000L);
            placeholderList.put(itemPlaceholder.getInternalId(), itemPlaceholder);
        }
        if (Config.i().getInventoryImage().inventory().enabled()) {
            String description = ChatColorUtils.translateAlternateColorCodes('&', Config.i().getInventoryImage().inventory().description());
            inventoryPlaceholder = new BuiltInPlaceholder(inventoryPlaceholderPattern, Config.i().getInventoryImage().inventory().inventoryTitle(), description, "", Config.i().getInventoryImage().inventory().cooldown() * 1000L);
            placeholderList.put(inventoryPlaceholder.getInternalId(), inventoryPlaceholder);
        }
        if (Config.i().getInventoryImage().enderChest().enabled()) {
            String description = ChatColorUtils.translateAlternateColorCodes('&', Config.i().getInventoryImage().enderChest().description());
            enderChestPlaceholder = new BuiltInPlaceholder(enderChestPlaceholderPattern, Config.i().getInventoryImage().enderChest().inventoryTitle(), description, "", Config.i().getInventoryImage().enderChest().cooldown() * 1000L);
            placeholderList.put(enderChestPlaceholder.getInternalId(), enderChestPlaceholder);
        }

        FontTextureResource.setCacheTime(Config.i().getSettings().cacheTimeout() * 20L);

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
                    ChatUtils.sendMessage("<yellow>Resource reloading already in progress!", senders);
                    return;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                return;
            }
            try {
                isReady = false;
                if (MultiChatDiscordSrvAddon.plugin.isResourceManagerReady()) {
                    Bukkit.getScheduler().callSyncMethod(plugin, () -> {
                        MultiChatDiscordSrvAddon.plugin.getResourceManager().close();
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
                if (Config.i().getResources().includeServerResourcePack()) {
                    ChatUtils.sendMessage("<aqua>Checking for server resource pack...", senders);
                    ServerResourcePackDownloadResult result = AssetsDownloader.downloadServerResourcePack(serverResourcePackFolder);
                    serverResourcePack = result.getResourcePackFile();
                    if (result.getError() != null) {
                        result.getError().printStackTrace();
                    }
                    switch (result.getType()) {
                        case SUCCESS_NO_CHANGES:
                            ChatUtils.sendMessage("<green>Server resource pack found with verification hash: No changes", senders);
                            resourceList.add(serverResourcePack.getName());
                            break;
                        case SUCCESS_WITH_HASH:
                            ChatUtils.sendMessage("<green>Server resource pack found with verification hash: Hash changed, downloaded", senders);
                            resourceList.add(serverResourcePack.getName());
                            break;
                        case SUCCESS_NO_HASH:
                            ChatUtils.sendMessage("<green>Server resource pack found without verification hash: Downloaded", senders);
                            resourceList.add(serverResourcePack.getName());
                            break;
                        case FAILURE_WRONG_HASH:
                            ChatUtils.sendMessage("<red>Server resource pack had wrong hash (expected " + result.getExpectedHash() + ", found " + result.getPackHash() + ")", senders);
                            ChatUtils.sendMessage("<red>Server resource pack will not be applied: Hash check failure", senders);
                            break;
                        case FAILURE_DOWNLOAD:
                            ChatUtils.sendMessage("<red>Failed to download server resource pack", senders);
                            break;
                        case NO_PACK:
                            ChatUtils.sendMessage("<red>No server resource pack found.", senders);
                            break;
                    }
                }

                ChatUtils.sendMessage("<aqua>Reloading ResourceManager: <yellow>" + String.join(", ", resourceList), senders);

                List<ModManagerSupplier<?>> mods = new ArrayList<>();
                if (Config.i().getResources().chimeOverrideModels()) {
                    mods.add(ChimeManager::new);
                }
                if (Config.i().getResources().optifineCustomTextures()) {
                    mods.add(OptifineManager::new);
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
                                Component.translatable(TranslationKeyUtils.getResourcePackVanillaDescription())
                        ),
                        ResourceManager.Flag.build(
                                VersionManager.version.isLegacy(),
                                false,
                                VersionManager.version.isOlderOrEqualTo(MCVersion.V1_21_3)
                        )
                );

                for (Entry<String, ModManager> entry : resourceManager.getModManagers().entrySet()) {
                    ChatUtils.sendMessage("<gray>Registered ModManager \"" + entry.getKey() + "\" of class \"" + entry.getValue().getClass().getName() + "\"", senders);
                }

                resourceManager.getFontManager().setDefaultKey(Config.i().getResources().forceUnicodeFont() ? FontManager.UNIFORM_FONT : FontManager.DEFAULT_FONT);
                resourceManager.getLanguageManager().setTranslateFunction((translateKey, fallback, language) -> LanguageUtils.getTranslation(translateKey, language).getResultOrFallback(fallback));
                resourceManager.getLanguageManager().setAvailableLanguagesSupplier(() -> LanguageUtils.getLoadedLanguages());
                resourceManager.getLanguageManager().registerReloadListener(e -> {
                    LanguageUtils.clearPluginTranslations(MultiChatDiscordSrvAddon.plugin);
                    for (Entry<String, Map<String, String>> entry : e.getTranslations().entrySet()) {
                        LanguageUtils.loadPluginTranslations(MultiChatDiscordSrvAddon.plugin, entry.getKey(), entry.getValue());
                    }
                });

                ChatUtils.sendMessage("<yellow>Loading \"Default\" resources...", senders);
                resourceManager.loadResources(new File(getDataFolder() + "/built-in", "Default"), ResourcePackType.BUILT_IN, true);
                for (String resourceName : resourceOrder) {
                    try {
                        ChatUtils.sendMessage("<yellow>Loading \"" + resourceName + "\" resources...", senders);
                        File resourcePackFile = new File(getDataFolder(), "resourcepacks/" + resourceName);
                        if (resourceName.startsWith("path:")) {
                            resourcePackFile = getDataFolder().toPath().resolve(resourceName.replace("path:", ""))
                                    .normalize()
                                    .toFile();
                        }


                        ResourcePackInfo info = resourceManager.loadResources(resourcePackFile, ResourcePackType.LOCAL);
                        if (info.getStatus()) {
                            if (info.compareServerPackFormat(ResourceRegistry.RESOURCE_PACK_VERSION) > 0) {
                                ChatUtils.sendMessage("<yellow>Warning: \"" + resourceName + "\" was made for a newer version of Minecraft!", senders);
                            } else if (info.compareServerPackFormat(ResourceRegistry.RESOURCE_PACK_VERSION) < 0) {
                                ChatUtils.sendMessage("<yellow>Warning: \"" + resourceName + "\" was made for an older version of Minecraft!", senders);
                            }
                        } else {
                            if (info.getRejectedReason() == null) {
                                ChatUtils.sendMessage("<red>Unable to load \"" + resourceName + "\"", senders);
                            } else {
                                ChatUtils.sendMessage("<red>Unable to load \"" + resourceName + "\", Reason: " + info.getRejectedReason(), senders);
                            }
                        }
                    } catch (Exception e) {
                        ChatUtils.sendMessage("<red>Unable to load \"" + resourceName + "\"", senders);
                        e.printStackTrace();
                    }
                }
                if (Config.i().getResources().includeServerResourcePack() && serverResourcePack != null && serverResourcePack.exists()) {
                    String resourceName = serverResourcePack.getName();
                    try {
                        ChatUtils.sendMessage("<yellow>Loading \"" + resourceName + "\" resources...", senders);
                        ResourcePackInfo info = resourceManager.loadResources(serverResourcePack, ResourcePackType.SERVER);
                        if (info.getStatus()) {
                            if (info.compareServerPackFormat(ResourceRegistry.RESOURCE_PACK_VERSION) > 0) {
                                ChatUtils.sendMessage("<yellow>Warning: \"" + resourceName + "\" was made for a newer version of Minecraft!", senders);
                            } else if (info.compareServerPackFormat(ResourceRegistry.RESOURCE_PACK_VERSION) < 0) {
                                ChatUtils.sendMessage("<yellow>Warning: \"" + resourceName + "\" was made for an older version of Minecraft!", senders);
                            }
                        } else {
                            if (info.getRejectedReason() == null) {
                                ChatUtils.sendMessage("<red>Unable to load \"" + resourceName + "\"", senders);
                            } else {
                                ChatUtils.sendMessage("<red>Unable to load \"" + resourceName + "\", Reason: " + info.getRejectedReason(), senders);
                            }
                        }
                    } catch (Exception e) {
                        ChatUtils.sendMessage("<red>Unable to load \"" + resourceName + "\"", senders);
                        e.printStackTrace();
                    }
                }

                Bukkit.getScheduler().callSyncMethod(plugin, () -> {
                    MultiChatDiscordSrvAddon.plugin.resourceManager = resourceManager;

                    if (resourceManager.getResourcePackInfo().stream().allMatch(each -> each.getStatus())) {
                        ChatUtils.sendMessage("<aqua>Loaded all resources!", senders);
                        isReady = true;
                    } else {
                        ChatUtils.sendMessage("<red>There is a problem while loading resources.", senders);
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

}
