package mrq.plugin.minecraft.tool;

import org.bukkit.ChatColor;

public class ColorString {
    public static String format(String s, Object... os) {
        s = s.replaceAll("#0", String.valueOf(ChatColor.BLACK));
        s = s.replaceAll("#db", String.valueOf(ChatColor.DARK_BLUE));
        s = s.replaceAll("#dg", String.valueOf(ChatColor.DARK_GREEN));
        s = s.replaceAll("#da", String.valueOf(ChatColor.DARK_AQUA));
        s = s.replaceAll("#dr", String.valueOf(ChatColor.DARK_RED));
        s = s.replaceAll("#dp", String.valueOf(ChatColor.DARK_PURPLE));
        s = s.replaceAll("#go", String.valueOf(ChatColor.GOLD));
        s = s.replaceAll("#\\.5", String.valueOf(ChatColor.GRAY));
        s = s.replaceAll("#d\\.5", String.valueOf(ChatColor.DARK_GRAY));
        s = s.replaceAll("#b", String.valueOf(ChatColor.BLUE));
        s = s.replaceAll("#g", String.valueOf(ChatColor.GREEN));
        s = s.replaceAll("#a", String.valueOf(ChatColor.AQUA));
        s = s.replaceAll("#r", String.valueOf(ChatColor.RED));
        s = s.replaceAll("#lp", String.valueOf(ChatColor.LIGHT_PURPLE));
        s = s.replaceAll("#y", String.valueOf(ChatColor.YELLOW));
        s = s.replaceAll("#1", String.valueOf(ChatColor.WHITE));
        s = s.replaceAll("\\$m", String.valueOf(ChatColor.MAGIC));
        s = s.replaceAll("\\$b", String.valueOf(ChatColor.BOLD));
        s = s.replaceAll("\\$s", String.valueOf(ChatColor.STRIKETHROUGH));
        s = s.replaceAll("\\$u", String.valueOf(ChatColor.UNDERLINE));
        s = s.replaceAll("\\$i", String.valueOf(ChatColor.ITALIC));
        s = s.replaceAll("\\$x", String.valueOf(ChatColor.RESET));
        s = String.format(s, os);
        return s;
    }
}