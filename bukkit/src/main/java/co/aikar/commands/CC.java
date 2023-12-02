package co.aikar.commands;

import org.bukkit.ChatColor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CC {
    public static final Pattern hexPattern = Pattern.compile("&#[a-fA-F0-9]{6}");
    public static boolean hasHexColors = false;

    public static void checkHexColors() {
        try {
            Class<net.md_5.bungee.api.ChatColor> clazz = net.md_5.bungee.api.ChatColor.class;
            clazz.getMethod("of", String.class);
            hasHexColors = true;
        } catch (Exception e) {
            hasHexColors = false;
        }
    }

    public static String translate(String input) {
        try {
            return ChatColor.translateAlternateColorCodes('&', getHexColor(input));
        } catch (Exception ignored) {
            return ChatColor.translateAlternateColorCodes('&', input);
        }
    }

    private static String getHexColor(String msg) {
        if (!hasHexColors) return msg;

        Matcher matcher = hexPattern.matcher(msg);
        while (matcher.find()) {
            String color = matcher.group();
            msg = msg.replace(color, net.md_5.bungee.api.ChatColor.of(color.replace("&", "")) + "");

        }
        return msg;
    }
}
