package mrq.plugin.minecraft.move.tool;

import mrq.plugin.minecraft.tool.ColorString;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;

public class Plate {
    private World world = null;
    private String player = null;
    private Location location = null;
    private int x = -1, y = -1, z = -1;
    private Material[] pattern = null;
    private String time = null;
    private String name = null;
    
    public Plate(World world, String player, Location location, Material[] pattern, String time, String name) {
        this.world = world;
        this.player = player;
        this.location = location;
        this.pattern = pattern;
        this.time = time;
        this.name = name;
        
        x = location.getBlockX();
        y = location.getBlockY();
        z = location.getBlockZ();
    }
    public String toString(int i) {
        String s = null;
        switch (i) {
            case 1: s = String.format("- %s, %s, %s", pattern[0], pattern[1], pattern[2]); break;
            case 2: s = ColorString.format("- %s, #.5%s$x, %s", pattern[3], PlateManager.ACTIVATE_BLOCK, pattern[4]); break;
            case 3: s = String.format("- %s, %s, %s", pattern[5], pattern[6], pattern[7]); break;
        }
        return s;
    }
    
    public World getWorld() {
        return world;
    }
    public String getPlayer() {
        return player;
    }
    public Location getLocation() {
        return location;
    }
    public int getX() {
        return x;
    }
    public int getY() {
        return y;
    }
    public int getZ() {
        return z;
    }
    public Material getPattern(int i) {
        if (i >= 0 && i < pattern.length) return pattern[i];
        else return null;
    }
    public int size() {
        if (pattern == null) return 0;
        return pattern.length;
    }
    public String getTime() {
        return time;
    }
    public String getName() {
        return name;
    }
}