/*
 * This file is part of InteractiveChat.
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

package com.loohp.multichatdiscordsrvaddon.utils;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.md_5.bungee.api.ChatColor;

import java.awt.Color;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ColorUtils {

    private static final BiMap<ChatColor, Color> colors = HashBiMap.create();

    private static boolean chatColorHasGetColor;

    static {
        colors.put(ChatColor.BLACK, new Color(0x000000));
        colors.put(ChatColor.DARK_BLUE, new Color(0x0000AA));
        colors.put(ChatColor.DARK_GREEN, new Color(0x00AA00));
        colors.put(ChatColor.DARK_AQUA, new Color(0x00AAAA));
        colors.put(ChatColor.DARK_RED, new Color(0xAA0000));
        colors.put(ChatColor.DARK_PURPLE, new Color(0xAA00AA));
        colors.put(ChatColor.GOLD, new Color(0xFFAA00));
        colors.put(ChatColor.GRAY, new Color(0xAAAAAA));
        colors.put(ChatColor.DARK_GRAY, new Color(0x555555));
        colors.put(ChatColor.BLUE, new Color(0x05555FF));
        colors.put(ChatColor.GREEN, new Color(0x55FF55));
        colors.put(ChatColor.AQUA, new Color(0x55FFFF));
        colors.put(ChatColor.RED, new Color(0xFF5555));
        colors.put(ChatColor.LIGHT_PURPLE, new Color(0xFF55FF));
        colors.put(ChatColor.YELLOW, new Color(0xFFFF55));
        colors.put(ChatColor.WHITE, new Color(0xFFFFFF));

        chatColorHasGetColor = Arrays.stream(ChatColor.class.getMethods()).anyMatch(each -> each.getName().equalsIgnoreCase("getColor") && each.getReturnType().equals(Color.class));
    }

    private static final Pattern COLOR_STRIP_PATTERN = Pattern.compile("((&|\u00a7)[0-9a-fk-or])|(\u00a7x(\u00a7[0-9a-fA-F]){6})|((?<!\\\\)(\\{|&|)#((?:[0-9a-fA-F]{3}){2})(\\}|))");

    public static ChatColor toChatColor(String str) {
        try {
            if (str.length() < 2) {
                return null;
            }
            if (str.charAt(1) == 'x' && str.length() > 13) {
                return ChatColor.of("#" + str.charAt(3) + str.charAt(5) + str.charAt(7) + str.charAt(9) + str.charAt(11) + str.charAt(13));
            } else {
                return ChatColor.getByChar(str.charAt(1));
            }
        } catch (Throwable e) {
            return null;
        }
    }

    public static String wipeColors(String message) {
        if (message == null || message.isEmpty()) {
            return message;
        }
        Matcher matcher = COLOR_STRIP_PATTERN.matcher(message);
        return matcher.replaceAll("");
    }

    public static Color getColor(ChatColor chatcolor) {
        if (chatColorHasGetColor) {
            return chatcolor.getColor();
        } else {
            Color color = colors.get(chatcolor);
            return color == null ? Color.white : color;
        }
    }

    public static ChatColor getLegacyChatColor(Color color) {
        ChatColor chatcolor = colors.inverse().get(color);
        return chatcolor == null ? ChatColor.WHITE : chatcolor;
    }

    public static Color hex2Rgb(String colorStr) {
        return new Color(Integer.valueOf(colorStr.substring(1, 3), 16), Integer.valueOf(colorStr.substring(3, 5), 16),
                Integer.valueOf(colorStr.substring(5, 7), 16));
    }

    public static String rgb2Hex(Color color) {
        return String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
    }

    public static Color getFirstColor(String str) {
        String colorStr = ChatColorUtils.getFirstColors(str);
        if (colorStr.length() > 1) {
            ChatColor chatColor = toChatColor(colorStr);
            if (chatColor != null && ChatColorUtils.isColor(chatColor)) {
                return chatColorHasGetColor ? chatColor.getColor() : getColor(chatColor);
            }
        }
        return null;
    }

    public static NamedTextColor toNamedTextColor(ChatColor color) {
        Color awtColor = getColor(color);
        return NamedTextColor.nearestTo(TextColor.color(awtColor.getRed(), awtColor.getGreen(), awtColor.getBlue()));
    }

    public static TextColor toTextColor(ChatColor color) {
        if (color.toString().length() == 2) {
            return toNamedTextColor(color);
        }
        Color awtColor = getColor(color);
        return TextColor.color(awtColor.getRed(), awtColor.getGreen(), awtColor.getBlue());
    }

    public static ChatColor toChatColor(NamedTextColor color) {
        return getLegacyChatColor(new Color(color.value()));
    }

}