package mrq.plugin.minecraft.tool;

import mrq.plugin.minecraft.move.WarpPlate;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;


public class CommandManager {
    public static boolean showWarpPlate(Player player, String s) {
        boolean b = false;
        try {
            int index = Integer.parseInt(s);
            WarpPatterns wp = WarpPatterns.getInstance();
            if (index > wp.size()) {
                player.sendMessage(ChatColor.WHITE + "[" + ChatColor.LIGHT_PURPLE + "WarpPlate" + ChatColor.WHITE + "] #" + ChatColor.DARK_GREEN + index + ChatColor.RED + " - ���� ��ȣ�Դϴ�. " + ChatColor.GRAY + "(�� ��ϵ� �� : " + ChatColor.DARK_GREEN + wp.size() + ChatColor.GRAY + "��)");
            }
            else if (index <= 0) {
                player.sendMessage(ChatColor.WHITE + "[" + ChatColor.LIGHT_PURPLE + "WarpPlate" + ChatColor.WHITE + "] ������ ��� ��ȣ�Դϴ�." + ChatColor.DARK_GREEN + "�ڿ���" + ChatColor.WHITE + "���� �Է����ּ���.");
            }
            else {
                index--;
                Location location = wp.getLocation(index);
                player.sendMessage(ChatColor.WHITE + "[" + ChatColor.LIGHT_PURPLE + "WarpPlate" + ChatColor.WHITE + "] #" + ChatColor.DARK_GREEN + (index+1) + ChatColor.WHITE + " - X : " + ChatColor.GOLD + location.getBlockX() + ChatColor.WHITE + ", Y : " + ChatColor.GOLD + location.getBlockY() + ChatColor.WHITE + ", Z : " + ChatColor.GOLD + location.getBlockZ());
                
                StringBuilder sb = new StringBuilder(ChatColor.WHITE + " - ");
                for (int i=1; i<=3; i++) {
                    int pattern = wp.getPattern(index, i-1);
                    if (pattern > 0) {
                        Material material = Material.getMaterial(pattern);
                        sb.append(i + ". " + ChatColor.DARK_AQUA + material + ChatColor.WHITE + ", ");
                    }
                }
                player.sendMessage(sb.toString());
                sb = new StringBuilder(ChatColor.WHITE + " - ");
                for (int i=4; i<=5; i++) {
                    int pattern = wp.getPattern(index, i-1);
                    if (pattern > 0) {
                        Material material = Material.getMaterial(pattern);
                        sb.append(i + ". " + ChatColor.DARK_AQUA + material + ChatColor.WHITE + ", ");
                    }
                }
                player.sendMessage(sb.toString());
                sb = new StringBuilder(ChatColor.WHITE + " - ");
                for (int i=6; i<=8; i++) {
                    int pattern = wp.getPattern(index, i-1);
                    if (pattern > 0) {
                        Material material = Material.getMaterial(pattern);
                        sb.append(i + ". " + ChatColor.DARK_AQUA + material);
                        if (i < 8) {
                            sb.append(ChatColor.WHITE + ", ");
                        }
                    }
                }
                player.sendMessage(sb.toString());
            }
            return true;
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.WHITE + "[" + ChatColor.LIGHT_PURPLE + "WarpPlate" + ChatColor.WHITE + "] " + ChatColor.DARK_GREEN + "����" + ChatColor.WHITE + "�� " + ChatColor.RED + "�ƴմϴ�.");
        }
        return b;
    }
    
    public static boolean changeWarpDelay(Object o, String s) {
        boolean b = false;
        try {
            if (o instanceof WarpPlate) {
                WarpPlate wp = (WarpPlate)o;
                int before = wp.getDelay();
                int delay = Integer.parseInt(s);
                if (delay < 0) delay = 0;
                wp.setDelay(delay);
                m.sg(o, "Warp delay changed! : " + before + " �� " + delay);
                wp.getServer().broadcastMessage(ChatColor.LIGHT_PURPLE + "Warp Plate" + ChatColor.WHITE + "�� ��� �ð��� �ٲ�����ϴ�! " + ChatColor.GRAY + before + ChatColor.WHITE + " �� " + ChatColor.DARK_GREEN + delay + (delay==0?ChatColor.GRAY+" (���)":""));
                b = true;
            }
        } catch (NumberFormatException e) {
            m.sg(o, s + " is not number.");
        }
        return b;
    }
}