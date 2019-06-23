package mrq.plugin.minecraft.move.tool;

import mrq.plugin.minecraft.tool.ColorString;
import mrq.plugin.minecraft.tool.m;
import mrq.plugin.pattern.AbstractAction;
import mrq.plugin.pattern.PluginListener;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class TeleportAction extends AbstractAction {
    public static final int HOME = 0;
    
    private static final int DELAY = 10;
    
    private Player player = null;
    private Location location = null;
    private int count = -1, repeat = -1;
    private int index = -1;
    private boolean first = false;
    
    public TeleportAction(PluginListener pl, Player player, Location location, int count, int index) {
        super(pl);
        this.player = player;
        this.location = location;
        this.count = count * 2;
        this.repeat = count;
        this.index = index;
    }
    @Override
    public void run() {
        location.setPitch(player.getLocation().getPitch());
        location.setYaw(player.getLocation().getYaw());
        if (player.teleport(location)) {
            if (first == false) {
                first = true;
                if (index > 0) {
                    player.playSound(location, Sound.PORTAL_TRAVEL, 0.3f, 3);
                    player.playSound(location, Sound.DOOR_OPEN, 1, 1);
                    m.sg(String.format("Warp to #%d: %s", index, player.getName()));
                }
                else {
                    player.playSound(location, Sound.PORTAL_TRIGGER, 0.5f, 2);
                    player.playSound(location, Sound.DOOR_OPEN, 1, 1);
                    m.sg(String.format("Warp to Home: %s", player.getName()));
                }
            }
            
            if (count > 0 && count%2 == 0) {
                String s = String.format("#dg%%%dd$x...", (int)Math.log10(repeat)+1);
                player.sendMessage(ColorString.format(s, count/2));
                player.playSound(player.getLocation(), Sound.CLICK, 1, 1);
            }
            count--;
            
            if (count > 0) {
                Plugin plugin = getPlugin();
                plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, this, DELAY);
            }
            else {
                if (index > 0) player.sendMessage(ColorString.format("#aWarp$x에 #b성공$x했습니다!"));
                else player.sendMessage(ColorString.format("기존에 저장된 위치로 #aWarp$x에 #b성공$x했습니다!"));
                player.playSound(player.getLocation(), Sound.CHEST_CLOSE, 1, 1);
            }
        }
        else {
            if (index > 0) m.sg(String.format("Warp to #%d fail...: %s", index, player.getName()));
            else m.sg(String.format("Warp to Home fail...: %s", player.getName()));
        }
    }
}