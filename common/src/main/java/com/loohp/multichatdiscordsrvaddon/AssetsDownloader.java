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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import com.loohp.multichatdiscordsrvaddon.libs.LibraryDownloadManager;
import com.loohp.multichatdiscordsrvaddon.libs.LibraryLoader;
import com.loohp.multichatdiscordsrvaddon.utils.VersionManager;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import com.loohp.multichatdiscordsrvaddon.utils.FileUtils;
import com.loohp.multichatdiscordsrvaddon.utils.HTTPRequestUtils;
import com.loohp.multichatdiscordsrvaddon.utils.HashUtils;
import com.loohp.multichatdiscordsrvaddon.hooks.ItemsAdderHook;
import com.loohp.multichatdiscordsrvaddon.resources.ResourceDownloadManager;
import com.loohp.multichatdiscordsrvaddon.utils.ResourcePackUtils;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class AssetsDownloader {

    private static final DecimalFormat FORMAT = new DecimalFormat("0.0");
    private static final ReentrantLock LOCK = new ReentrantLock(true);

    @SuppressWarnings("deprecation")
    public static void loadAssets(File rootFolder, boolean force, boolean clean, CommandSender... senders) throws Exception {
        if (!Arrays.asList(senders).contains(Bukkit.getConsoleSender())) {
            List<CommandSender> senderList = new ArrayList<>(Arrays.asList(senders));
            senderList.add(Bukkit.getConsoleSender());
            senders = senderList.toArray(new CommandSender[senderList.size()]);
        }
        try {
            if (!LOCK.tryLock(0, TimeUnit.MILLISECONDS)) {
                return;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            return;
        }
        try {
            File hashes = new File(rootFolder, "hashes.json");
            if (!hashes.exists()) {
                try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(Files.newOutputStream(hashes.toPath()), StandardCharsets.UTF_8))) {
                    pw.println("{}");
                    pw.flush();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            JSONObject json;
            try (InputStreamReader hashReader = new InputStreamReader(Files.newInputStream(hashes.toPath()), StandardCharsets.UTF_8)) {
                json = (JSONObject) new JSONParser().parse(hashReader);
            } catch (Throwable e) {
                new RuntimeException("Invalid hashes.json! It will be reset.", e).printStackTrace();
                json = new JSONObject();
            }
            String oldHash = MultiChatDiscordSrvAddon.plugin.defaultResourceHash = json.containsKey("Default") ? json.get("Default").toString() : "EMPTY";
            String oldVersion = json.containsKey("version") ? json.get("version").toString() : "EMPTY";

            File defaultAssetsFolder = new File(rootFolder + "/built-in", "Default");
            defaultAssetsFolder.mkdirs();

            ResourceDownloadManager downloadManager = new ResourceDownloadManager(VersionManager.exactMinecraftVersion, defaultAssetsFolder);

            String hash = downloadManager.getHash();

            if (force || !hash.equals(oldHash) || !MultiChatDiscordSrvAddon.plugin.getDescription().getVersion().equals(oldVersion)) {
                if (clean) {
                    ChatUtils.sendMessage("<grey>Cleaning old default resources!", senders);
                    FileUtils.removeFolderRecursively(defaultAssetsFolder);
                    defaultAssetsFolder.mkdirs();
                }
                if (force) {
                    ChatUtils.sendMessage("<aqua>Forcibly re-downloading default resources! Please wait... <grey>(" + oldHash + " -> " + hash + ")", senders);
                } else if (!hash.equals(oldHash)) {
                    ChatUtils.sendMessage("<aqua>Hash changed! Re-downloading default resources! Please wait... <grey>(" + oldHash + " -> " + hash + ")", senders);
                } else {
                    ChatUtils.sendMessage("<aqua>Plugin version changed! Re-downloading default resources! Please wait... <grey>(" + oldHash + " -> " + hash + ")", senders);
                }

                CommandSender[] finalSenders = senders;
                downloadManager.downloadResources((type, fileName, percentage) -> {
                    switch (type) {
                        case CLIENT_DOWNLOAD:
                            if (!MultiChatDiscordSrvAddon.plugin.reducedAssetsDownloadInfo && percentage == 0.0) {
                                ChatUtils.sendMessage("<grey>Downloading client jar", finalSenders);
                            }
                            break;
                        case EXTRACT:
                            if (!MultiChatDiscordSrvAddon.plugin.reducedAssetsDownloadInfo) {
                                ChatUtils.sendMessage("<grey>Extracting " + fileName + " (" + FORMAT.format(percentage) + "%)", finalSenders);
                            }
                            break;
                        case DOWNLOAD:
                            if (!MultiChatDiscordSrvAddon.plugin.reducedAssetsDownloadInfo) {
                                ChatUtils.sendMessage("<grey>Downloading " + fileName + " (" + FORMAT.format(percentage) + "%)", finalSenders);
                            }
                            break;
                        case DONE:
                            ChatUtils.sendMessage("<green>Done!", finalSenders);
                            break;
                    }
                });
            }

            downloadManager.downloadExtras(() -> {
                MultiChatDiscordSrvAddon.plugin.extras.clear();
            }, (key, dataBytes) -> {
                MultiChatDiscordSrvAddon.plugin.extras.put(key, dataBytes);
            });

            MultiChatDiscordSrvAddon.plugin.defaultResourceHash = hash;

            json.put("Default", hash);
            json.put("version", MultiChatDiscordSrvAddon.plugin.getDescription().getVersion());

            try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(Files.newOutputStream(hashes.toPath()), StandardCharsets.UTF_8))) {
                Gson g = new GsonBuilder().setPrettyPrinting().create();
                pw.println(g.toJson(new JsonParser().parse(json.toString())));
                pw.flush();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            LOCK.unlock();
        }
    }

    public static void loadExtras() {
        ResourceDownloadManager downloadManager = new ResourceDownloadManager(VersionManager.exactMinecraftVersion, null);
        downloadManager.downloadExtras(() -> {
            MultiChatDiscordSrvAddon.plugin.extras.clear();
        }, (key, dataBytes) -> {
            MultiChatDiscordSrvAddon.plugin.extras.put(key, dataBytes);
        });
    }

    public static ServerResourcePackDownloadResult downloadServerResourcePack(File packFolder) {
        String url = MultiChatDiscordSrvAddon.plugin.alternateResourcePackURL;
        String hash = MultiChatDiscordSrvAddon.plugin.alternateResourcePackHash;
        if (MultiChatDiscordSrvAddon.itemsAdderHook && MultiChatDiscordSrvAddon.plugin.itemsAdderPackAsServerResourcePack) {
            String iaUrl = ItemsAdderHook.getItemsAdderResourcePackURL();
            if (iaUrl != null) {
                url = iaUrl;
                hash = null;
            }
        }
        if (url == null || url.isEmpty()) {
            url = ResourcePackUtils.getServerResourcePack();
            hash = ResourcePackUtils.getServerResourcePackHash();
            if (url == null || url.isEmpty()) {
                return new ServerResourcePackDownloadResult(ServerResourcePackDownloadResultType.NO_PACK);
            }
        }
        File desFile = hash != null && !hash.isEmpty() ? new File(packFolder, hash) : null;
        if (desFile != null && desFile.exists()) {
            try {
                if (hash != null && !hash.isEmpty()) {
                    String packHash = HashUtils.createSha1String(desFile);
                    if (packHash.equalsIgnoreCase(hash)) {
                        return new ServerResourcePackDownloadResult(ServerResourcePackDownloadResultType.SUCCESS_NO_CHANGES, desFile, packHash, hash);
                    }
                }
            } catch (Exception ignore) {
            }
        }
        Arrays.stream(packFolder.listFiles()).forEach(each -> {
            if (each.isFile()) {
                each.delete();
            }
        });
        byte[] packData = HTTPRequestUtils.download(url);
        if (packData != null) {
            try {
                String packHash = HashUtils.createSha1String(new ByteArrayInputStream(packData));
                desFile = new File(packFolder, packHash);
                if (hash == null || hash.isEmpty()) {
                    FileUtils.copy(new ByteArrayInputStream(packData), desFile);
                    return new ServerResourcePackDownloadResult(ServerResourcePackDownloadResultType.SUCCESS_NO_HASH, desFile);
                } else {
                    if (packHash.equalsIgnoreCase(hash)) {
                        FileUtils.copy(new ByteArrayInputStream(packData), desFile);
                        return new ServerResourcePackDownloadResult(ServerResourcePackDownloadResultType.SUCCESS_WITH_HASH, desFile, packHash, hash);
                    }
                    return new ServerResourcePackDownloadResult(ServerResourcePackDownloadResultType.FAILURE_WRONG_HASH, packHash, hash);
                }
            } catch (Exception e) {
                return new ServerResourcePackDownloadResult(ServerResourcePackDownloadResultType.FAILURE_WRONG_HASH, null, "ERROR", hash, e);
            }
        } else {
            return new ServerResourcePackDownloadResult(ServerResourcePackDownloadResultType.FAILURE_DOWNLOAD);
        }
    }

    public static void loadLibraries(File rootFolder) {
        try {
            File hashes = new File(rootFolder, "hashes.json");
            if (!hashes.exists()) {
                try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(Files.newOutputStream(hashes.toPath()), StandardCharsets.UTF_8))) {
                    pw.println("{}");
                    pw.flush();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            JSONObject json;
            try (InputStreamReader hashReader = new InputStreamReader(Files.newInputStream(hashes.toPath()), StandardCharsets.UTF_8)) {
                json = (JSONObject) new JSONParser().parse(hashReader);
            } catch (Throwable e) {
                new RuntimeException("Invalid hashes.json! It will be reset.", e).printStackTrace();
                json = new JSONObject();
            }
            String oldHash = MultiChatDiscordSrvAddon.plugin.defaultResourceHash = json.containsKey("libs") ? json.get("libs").toString() : "EMPTY";
            String oldVersion = json.containsKey("version") ? json.get("version").toString() : "EMPTY";

            File libsFolder = new File(rootFolder, "libs");
            libsFolder.mkdirs();

            LibraryDownloadManager downloadManager = new LibraryDownloadManager(libsFolder);

            String hash = "N/A";
            try {
                hash = downloadManager.getHash();

                if (!hash.equals(oldHash) || !MultiChatDiscordSrvAddon.plugin.getDescription().getVersion().equals(oldVersion)) {
                    downloadManager.downloadLibraries((result, jarName, percentage) -> {
                        if (result) {
                            ChatUtils.sendMessage("<grey>Downloaded library \"" + jarName + "\"", Bukkit.getConsoleSender());
                        } else {
                            ChatUtils.sendMessage("<red>Unable to download library \"" + jarName + "\"", Bukkit.getConsoleSender());
                        }
                    });
                }
            } catch (Throwable e) {
                ChatUtils.sendMessage("<red>An error occurred while downloading libraries.", Bukkit.getConsoleSender());
                e.printStackTrace();
            }

            LibraryLoader.loadLibraries(libsFolder, (file, e) -> {
                String jarName = file.getName();
                if (e == null) {
                    ChatUtils.sendMessage("<green>Remapped library \"" + jarName + "\"", Bukkit.getConsoleSender());
                } else {
                    ChatUtils.sendMessage("<red>Unable to remap library \"" + jarName + "\"", Bukkit.getConsoleSender());
                    e.printStackTrace();
                }
            }, (file, e) -> {
                String jarName = file.getName();
                if (e == null) {
                    ChatUtils.sendMessage("<green>Loaded library \"" + jarName + "\"", Bukkit.getConsoleSender());
                } else {
                    ChatUtils.sendMessage("<red>Unable to load library \"" + jarName + "\"", Bukkit.getConsoleSender());
                    e.printStackTrace();
                }
            });

            json.put("libs", hash);
            json.put("version", MultiChatDiscordSrvAddon.plugin.getDescription().getVersion());

            try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(Files.newOutputStream(hashes.toPath()), StandardCharsets.UTF_8))) {
                Gson g = new GsonBuilder().setPrettyPrinting().create();
                pw.println(g.toJson(new JsonParser().parse(json.toString())));
                pw.flush();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String getEntryName(String name) {
        int pos = name.lastIndexOf("/");
        if (pos >= 0) {
            return name.substring(pos + 1);
        }
        pos = name.lastIndexOf("\\");
        if (pos >= 0) {
            return name.substring(pos + 1);
        }
        return name;
    }

    public static class ServerResourcePackDownloadResult {

        private final ServerResourcePackDownloadResultType type;
        private final  File resourcePackFile;
        private final String packHash;
        private final String expectedHash;
        private final Throwable error;

        public ServerResourcePackDownloadResult(ServerResourcePackDownloadResultType type, File resourcePackFile, String packHash, String expectedHash, Throwable error) {
            this.type = type;
            this.resourcePackFile = resourcePackFile;
            this.packHash = packHash;
            this.expectedHash = expectedHash;
            this.error = error;
        }

        public ServerResourcePackDownloadResult(ServerResourcePackDownloadResultType type, File resourcePackFile, String packHash, String expectedHash) {
            this(type, resourcePackFile, packHash, expectedHash, null);
        }

        public ServerResourcePackDownloadResult(ServerResourcePackDownloadResultType type, String packHash, String expectedHash) {
            this(type, null, packHash, expectedHash, null);
        }

        public ServerResourcePackDownloadResult(ServerResourcePackDownloadResultType type, Throwable error) {
            this(type, null, null, null, error);
        }

        public ServerResourcePackDownloadResult(ServerResourcePackDownloadResultType type, File resourcePackFile) {
            this(type, resourcePackFile, null, null, null);
        }

        public ServerResourcePackDownloadResult(ServerResourcePackDownloadResultType type) {
            this(type, null, null, null, null);
        }

        public ServerResourcePackDownloadResultType getType() {
            return type;
        }

        public File getResourcePackFile() {
            return resourcePackFile;
        }

        public String getPackHash() {
            return packHash;
        }

        public String getExpectedHash() {
            return expectedHash;
        }

        public Throwable getError() {
            return error;
        }

    }

    public enum ServerResourcePackDownloadResultType {
        NO_PACK, SUCCESS_NO_CHANGES, SUCCESS_NO_HASH, SUCCESS_WITH_HASH, FAILURE_DOWNLOAD, FAILURE_WRONG_HASH;
    }

}
