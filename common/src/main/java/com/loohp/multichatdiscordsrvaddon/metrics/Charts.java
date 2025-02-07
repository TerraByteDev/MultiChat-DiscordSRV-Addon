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

package com.loohp.multichatdiscordsrvaddon.metrics;

import com.loohp.multichatdiscordsrvaddon.MultiChatDiscordSrvAddon;
import com.loohp.multichatdiscordsrvaddon.config.Config;
import github.scarsz.discordsrv.dependencies.jda.api.JDA;

import java.util.concurrent.Callable;

public class Charts {

    public static void setup(Metrics metrics) {

        metrics.addCustomChart(new Metrics.SingleLineChart("discord_servers_present", new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                JDA jda = MultiChatDiscordSrvAddon.discordsrv.getJda();
                if (jda == null) {
                    return 0;
                }
                return jda.getGuilds().size();
            }
        }));

        metrics.addCustomChart(new Metrics.SingleLineChart("discord_channels_present", new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                JDA jda = MultiChatDiscordSrvAddon.discordsrv.getJda();
                if (jda == null) {
                    return 0;
                }
                return jda.getGuilds().stream().mapToInt(each -> each.getChannels().size()).sum();
            }
        }));

        metrics.addCustomChart(new Metrics.SingleLineChart("total_discord_members", new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                JDA jda = MultiChatDiscordSrvAddon.discordsrv.getJda();
                if (jda == null) {
                    return 0;
                }
                return jda.getUsers().size();
            }
        }));

        metrics.addCustomChart(new Metrics.SimplePie("item_image_view_enabled", new Callable<String>() {
            @Override
            public String call() throws Exception {
                String string = "Disabled";
                if (Config.i().getInventoryImage().item().enabled()) {
                    string = "Enabled";
                }
                return string;
            }
        }));

        metrics.addCustomChart(new Metrics.SimplePie("inventory_image_view_enabled", new Callable<String>() {
            @Override
            public String call() throws Exception {
                String string = "Disabled";
                if (Config.i().getInventoryImage().inventory().enabled()) {
                    string = "Enabled";
                }
                return string;
            }
        }));

        metrics.addCustomChart(new Metrics.SimplePie("enderchest_image_view_enabled", new Callable<String>() {
            @Override
            public String call() throws Exception {
                String string = "Disabled";
                if (Config.i().getInventoryImage().enderChest().enabled()) {
                    string = "Enabled";
                }
                return string;
            }
        }));

        metrics.addCustomChart(new Metrics.SimplePie("hoverevent_display_enabled", new Callable<String>() {
            @Override
            public String call() throws Exception {
                String string = "Disabled";
                if (Config.i().getHoverEventDisplay().enabled()) {
                    string = "Enabled";
                }
                return string;
            }
        }));

        metrics.addCustomChart(new Metrics.SimplePie("hoverevent_image_tooltip_enabled", new Callable<String>() {
            @Override
            public String call() throws Exception {
                String string = "Disabled";
                if (Config.i().getHoverEventDisplay().useTooltipImage()) {
                    string = "Enabled";
                }
                return string;
            }
        }));

        metrics.addCustomChart(new Metrics.SimplePie("discord_images_preview_enabled", new Callable<String>() {
            @Override
            public String call() throws Exception {
                String string = "Disabled";
                if (Config.i().getDiscordAttachments().convert()) {
                    string = "Enabled";
                }
                return string;
            }
        }));

        metrics.addCustomChart(new Metrics.SimplePie("show_death_message_weapon", new Callable<String>() {
            @Override
            public String call() throws Exception {
                String string = "Disabled";
                if (Config.i().getDeathMessage().showItems()) {
                    string = "Enabled";
                }
                return string;
            }
        }));

        metrics.addCustomChart(new Metrics.SimplePie("correct_advancement_name", new Callable<String>() {
            @Override
            public String call() throws Exception {
                String string = "Disabled";
                if (Config.i().getAdvancements().correctAdvancementName()) {
                    string = "Enabled";
                }
                return string;
            }
        }));

        metrics.addCustomChart(new Metrics.SimplePie("show_advancement_item_icon", new Callable<String>() {
            @Override
            public String call() throws Exception {
                String string = "Disabled";
                if (Config.i().getAdvancements().changeToItemIcon()) {
                    string = "Enabled";
                }
                return string;
            }
        }));

        metrics.addCustomChart(new Metrics.SimplePie("show_advancement_description", new Callable<String>() {
            @Override
            public String call() throws Exception {
                String string = "Disabled";
                if (Config.i().getAdvancements().showDescription()) {
                    string = "Enabled";
                }
                return string;
            }
        }));

        metrics.addCustomChart(new Metrics.SimplePie("translate_mentions_enabled", new Callable<String>() {
            @Override
            public String call() throws Exception {
                String string = "Disabled";
                if (Config.i().getDiscordMention().translateMentions()) {
                    string = "Enabled";
                }
                return string;
            }
        }));

        metrics.addCustomChart(new Metrics.SingleLineChart("resource_packs_installed", new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                try {
                    return Math.max(0, MultiChatDiscordSrvAddon.plugin.getResourceManager().getResourcePackInfo().size() - 1);
                } catch (Throwable e) {
                    return 0;
                }
            }
        }));

        metrics.addCustomChart(new Metrics.SingleLineChart("total_messages_processed_per_interval", new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                long amount = MultiChatDiscordSrvAddon.plugin.messagesCounter.getAndSet(0);
                return amount > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) amount;
            }
        }));

        metrics.addCustomChart(new Metrics.SingleLineChart("total_images_created_per_interval", new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                long amount = MultiChatDiscordSrvAddon.plugin.imageCounter.getAndSet(0);
                return amount > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) amount;
            }
        }));

        metrics.addCustomChart(new Metrics.SingleLineChart("total_inventory_images_created_per_interval", new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                long amount = MultiChatDiscordSrvAddon.plugin.inventoryImageCounter.getAndSet(0);
                return amount > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) amount;
            }
        }));

        metrics.addCustomChart(new Metrics.SingleLineChart("total_discord_attachments_processed_per_interval", new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                long amount = MultiChatDiscordSrvAddon.plugin.attachmentCounter.getAndSet(0);
                return amount > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) amount;
            }
        }));

        metrics.addCustomChart(new Metrics.SingleLineChart("total_discord_images_processed_per_interval", new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                long amount = MultiChatDiscordSrvAddon.plugin.attachmentImageCounter.getAndSet(0);
                return amount > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) amount;
            }
        }));

        metrics.addCustomChart(new Metrics.SingleLineChart("total_image_maps_viewed_per_interval", new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                long amount = MultiChatDiscordSrvAddon.plugin.imagesViewedCounter.getAndSet(0);
                return amount > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) amount;
            }
        }));

        metrics.addCustomChart(new Metrics.SingleLineChart("servers_rendering_player_models_with_hand_items", new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return Config.i().getInventoryImage().inventory().usePlayerInventoryView() && Config.i().getInventoryImage().inventory().renderHandHeldItems() ? 1 : 0;
            }
        }));

        metrics.addCustomChart(new Metrics.SingleLineChart("servers_combined_average_pmwhh_rendering_times", new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                long combined = 0;
                Integer i;
                int counter = 0;
                while ((i = MultiChatDiscordSrvAddon.plugin.playerModelRenderingTimes.poll()) != null) {
                    combined += i;
                    counter++;
                }
                return (int) ((double) combined / (double) counter);
            }
        }));

    }

}
