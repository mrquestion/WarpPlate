package mrq.plugin.minecraft.move.debug;

import org.bukkit.Material;
import org.bukkit.entity.Player;

public class Print {
    private void layer(Player player, Material[] checkLayer) {
        int length = (int)Math.sqrt(checkLayer.length);
        for (int i = 0; i < length; ++i) {
            StringBuilder sb = new StringBuilder();
            for (int j = 0; j < length; ++j) {
                sb.append(String.format(" %s", checkLayer[length * i + j]).replace("LEGACY_", "").substring(0, 4));
            }
            if (player == null) {
                l.og(sb.toString());
            }
            else {
                player.sendMessage(sb.toString());
            }
        }
    }
    private void pattern1(Player player, Material[] checkLayer) {
        int length = (int)Math.sqrt(checkLayer.length);
        for (int i = 1; i < length - 1; ++i) {
            StringBuilder sb = new StringBuilder();
            for (int j = 1; j < length - 1; ++j) {
                sb.append(!(i == length / 2 && j == length / 2) ? String.format(" %s", checkLayer[length * i + j]).replace("LEGACY_", "").substring(0, 4) : "    ");
            }
            if (player == null) {
                l.og(sb.toString());
            }
            else {
                player.sendMessage(sb.toString());
            }
        }
    }
    private void pattern2(Player player, Material[] pattern) {
        int length = (int)Math.sqrt(pattern.length + 1);
        int offset = 0;
        for (int i = 0; i < length; ++i) {
            StringBuilder sb = new StringBuilder();
            for (int j = 0; j < length; ++j) {
                sb.append(!(i == length / 2 && j == length / 2) ? String.format(" %s", pattern[offset++]).replace("LEGACY_", "").substring(0, 4) : "    ");
            }
            if (player == null) {
                l.og(sb.toString());
            }
            else {
                player.sendMessage(sb.toString());
            }
        }
    }
}
