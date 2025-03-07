package com.loohp.multichatdiscordsrvaddon.standalone.cmd;

import com.loohp.multichatdiscordsrvaddon.objectholders.OfflinePlayerData;
import com.loohp.multichatdiscordsrvaddon.utils.PlayerUtils;
import net.dv8tion.jda.api.interactions.commands.CommandInteraction;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class StandaloneCommandUtils {

    public static ItemStack resolveItemStack(CommandInteraction event, OfflinePlayer player) {
        OfflinePlayerData offlinePlayerData = PlayerUtils.getData(player);
        String subCommand = event.getSubcommandName();
        return switch (subCommand) {
            case "mainhand" -> offlinePlayerData.getInventory().getItem(offlinePlayerData.getSelectedSlot());
            case "offhand" ->
                    offlinePlayerData.getInventory().getSize() > 40 ? offlinePlayerData.getInventory().getItem(40) : null;
            case "hotbar", "inventory" ->
                    offlinePlayerData.getInventory().getItem((int) event.getOptions().get(0).getAsLong() - 1);
            case "armor" ->
                    offlinePlayerData.getEquipment().getItem(EquipmentSlot.valueOf(event.getOptions().get(0).getAsString().toUpperCase()));
            case "ender" -> offlinePlayerData.getEnderChest().getItem((int) event.getOptions().get(0).getAsLong() - 1);
            default -> null;
        };
    }

}
