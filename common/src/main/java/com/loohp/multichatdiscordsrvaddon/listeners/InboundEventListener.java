package com.loohp.multichatdiscordsrvaddon.listeners;

import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSystemChatMessage;
import com.loohp.multichatdiscordsrvaddon.MultiChatDiscordSrvAddon;
import com.loohp.multichatdiscordsrvaddon.config.Config;
import com.loohp.multichatdiscordsrvaddon.debug.Debug;
import com.loohp.multichatdiscordsrvaddon.objectholders.DiscordAttachmentData;
import com.loohp.multichatdiscordsrvaddon.utils.ChatColorUtils;
import com.loohp.multichatdiscordsrvaddon.utils.ComponentReplacing;
import com.loohp.multichatdiscordsrvaddon.utils.CustomStringUtils;
import com.loohp.multichatdiscordsrvaddon.wrappers.GraphicsToPacketMapWrapper;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCreativeEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

public class InboundEventListener implements Listener, PacketListener {

    public static final Pattern TENOR_HTML_PATTERN = Pattern.compile("<link class=\"dynamic\" rel=\"image_src\" href=\"https://media1\\.tenor\\.com/m/(.*?)/.*?\">");

    public static final Map<UUID, DiscordAttachmentData> DATA = new ConcurrentHashMap<>();
    public static final Map<Player, GraphicsToPacketMapWrapper> MAP_VIEWERS = new ConcurrentHashMap<>();
    public static final Map<String, Component> components = new ConcurrentHashMap<>();

    @SuppressWarnings("deprecation")
    @EventHandler
    public void onInventory(InventoryOpenEvent event) {
        Player player = (Player) event.getPlayer();
        boolean removed = MAP_VIEWERS.remove(player) != null;

        if (removed) {
            player.getInventory().setItemInHand(player.getInventory().getItemInHand());
        }
    }

    @SuppressWarnings("deprecation")
    @EventHandler
    public void onInventory(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        if (player.getGameMode().equals(GameMode.CREATIVE)) {
            Bukkit.getScheduler().runTaskLater(MultiChatDiscordSrvAddon.plugin, () -> {
                boolean removed = MAP_VIEWERS.remove(player) != null;

                if (removed) {
                    player.getInventory().setItemInHand(player.getInventory().getItemInHand());
                }
            }, 1);
        } else {
            boolean removed = MAP_VIEWERS.remove(player) != null;

            if (removed) {
                player.getInventory().setItemInHand(player.getInventory().getItemInHand());
            }
        }
    }

    @SuppressWarnings("deprecation")
    @EventHandler
    public void onInventory(InventoryCreativeEvent event) {
        Player player = (Player) event.getWhoClicked();
        boolean removed = MAP_VIEWERS.remove(player) != null;

        int slot = event.getSlot();

        if (removed) {
            if (player.getInventory().equals(event.getClickedInventory()) && slot >= 9) {
                ItemStack item = player.getInventory().getItem(slot);
                Bukkit.getScheduler().runTaskLater(MultiChatDiscordSrvAddon.plugin, () -> player.getInventory().setItem(slot, item), 1);
            } else {
                event.setCursor(null);
            }
        }

        if (removed) {
            player.getInventory().setItemInHand(player.getInventory().getItemInHand());
        }
    }

    @SuppressWarnings("deprecation")
    @EventHandler
    public void onSlotChange(PlayerItemHeldEvent event) {
        if (event.getNewSlot() == event.getPreviousSlot()) {
            return;
        }

        Player player = event.getPlayer();
        boolean removed = MAP_VIEWERS.remove(player) != null;

        if (removed) {
            player.getInventory().setItemInHand(player.getInventory().getItemInHand());
        }
    }

    @SuppressWarnings("deprecation")
    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction().equals(Action.PHYSICAL)) {
            return;
        }
        Player player = event.getPlayer();

        if (player.getGameMode().equals(GameMode.CREATIVE)) {
            Bukkit.getScheduler().runTaskLater(MultiChatDiscordSrvAddon.plugin, () -> {
                boolean removed = MAP_VIEWERS.remove(player) != null;

                if (removed) {
                    player.getInventory().setItemInHand(player.getInventory().getItemInHand());
                }
            }, 1);
        } else {
            boolean removed = MAP_VIEWERS.remove(player) != null;

            if (removed) {
                player.getInventory().setItemInHand(player.getInventory().getItemInHand());
            }
        }
    }

    @SuppressWarnings("deprecation")
    @EventHandler
    public void onAttack(EntityDamageByEntityEvent event) {
        Entity entity = event.getDamager();
        if (entity instanceof Player) {
            Player player = (Player) entity;
            boolean removed = MAP_VIEWERS.remove(player) != null;

            if (removed) {
                player.getInventory().setItemInHand(player.getInventory().getItemInHand());
            }
        }
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent event) {
        MAP_VIEWERS.remove(event.getPlayer());
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (event.getPacketType() == PacketType.Play.Server.SYSTEM_CHAT_MESSAGE) {
            Debug.debug("Triggering onChatPacket");

            WrapperPlayServerSystemChatMessage messageWrapper = new WrapperPlayServerSystemChatMessage(event);
            Component component = messageWrapper.getMessage();
            String plain = PlainTextComponentSerializer.plainText().serialize(component);

            for (Map.Entry<String, Component> entry : components.entrySet()) {
                if (plain.contains(entry.getKey())) {
                    messageWrapper.setMessage(ComponentReplacing.replace(MiniMessage.miniMessage().deserialize(plain), CustomStringUtils.escapeMetaCharacters(entry.getKey()), false, entry.getValue()));

                    event.setLastUsedWrapper(messageWrapper);
                    event.markForReEncode(true);
                    break;
                }
            }

            if (Config.i().getDiscordAttachments().convert()) {
                Debug.debug("onChatPacket converting discord attachments");

                for (Map.Entry<UUID, DiscordAttachmentData> entry : DATA.entrySet()) {
                    DiscordAttachmentData data = entry.getValue();
                    String url = data.getUrl();

                    Component textComponent = ChatColorUtils.format(Config.i().getDiscordAttachments().formatting().text()
                            .replace("{FileName}", data.getFileName()));
                    if (Config.i().getDiscordAttachments().formatting().hover().enabled()) {
                        textComponent = textComponent.hoverEvent(net.kyori.adventure.text.event.HoverEvent.showText(ChatColorUtils.format(String.join("\n", Config.i().getDiscordAttachments().formatting().hover().hoverText())
                                .replace("{FileName}", data.getFileName()))));
                    }

                    if (Config.i().getDiscordAttachments().showImageUsingMaps() && data.isImage()) {
                        textComponent = textComponent.clickEvent(net.kyori.adventure.text.event.ClickEvent.runCommand("/mc imagemap " + data.getUniqueId().toString()));
                        Component imageAppend = ChatColorUtils.format(Config.i().getDiscordAttachments().formatting().imageOriginal()
                                .replace("{FileName}", data.getFileName()));

                        imageAppend.hoverEvent(HoverEvent.showText(ChatColorUtils.format(String.join("\n", Config.i().getDiscordAttachments().formatting().hover().imageOriginalHover())
                                .replace("{FileName}", data.getFileName()))));
                        imageAppend = imageAppend.clickEvent(ClickEvent.openUrl(url));
                        textComponent = textComponent.append(imageAppend);
                    } else {
                        textComponent = textComponent.clickEvent(ClickEvent.openUrl(url));
                    }

                    component = ComponentReplacing.replace(component, "\\\\?" + CustomStringUtils.escapeMetaCharacters(url), textComponent);

                    messageWrapper.setMessage(component);

                    event.setLastUsedWrapper(messageWrapper);
                    event.markForReEncode(true);
                }
            }
        }
    }
}
