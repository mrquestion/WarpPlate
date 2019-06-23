package mrq.plugin.minecraft.move.tool;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;

public class PlateList {
    private static final PlateList instance = new PlateList();
    public static final PlateList getInstance() {
        return instance;
    }
    
    private List<Plate> list = null;
    
    private PlateList() {
        list = new ArrayList<>();
    }
    
//    public void add(Player player, Location location, Material[] pattern) {
//        for (Material material: pattern) player.sendMessage(String.valueOf(material));
//        list.add(new Plate(player.getWorld(), location, pattern, player.getName()));
//    }
    
//    public void add(World world, String player, Location location, Material[] pattern, String name) {
//        list.add(new Plate(world, player, location, pattern, name));
//    }
//    public void add(Player player, Location location, Material[] pattern) {
//        list.add(new Plate(player.getWorld(), player.getName(), location, pattern, null));
//    }
//    public void add(Player player, Location location, Material[] pattern, String name) {
//        list.add(new Plate(player.getWorld(), player.getName(), location, pattern, name));
//    }
    public void add(Plate plate) {
        list.add(plate);
    }
    public Plate get(int i) {
        if (i >= 0 && i < list.size()) return list.get(i);
        else return null;
    }
    public void remove(int index) {
        list.remove(index);
    }
    public int size() {
        return list.size();
    }
    public void clear() {
        list.clear();
    }
    public int getIndex(Plate plate1) {
        int index = -1;
        for (int i=0, maxi=list.size(); i<maxi; i++) {
            Plate plate2 = list.get(i);
            for (int j=0, count=0, maxj=Math.min(plate1.size(), plate2.size()); j<maxj; j++) {
                if (plate1.getPattern(j).compareTo(plate2.getPattern(j)) == 0) count++;
                if (count >= maxj) {
                    index = i;
                    break;
                }
            }
            if (index > 0) break;
        }
//        int index = -1;
//        for (Plate plate2: list) {
//            for (int i=0, count=0, max=Math.min(plate1.size(), plate2.size()); i<max; i++) {
//                if (plate1.getPattern(i).compareTo(plate2.getPattern(i)) == 0) count++;
//                if (count >= max) {
//                    index = i;
//                    break;
//                }
//            }
//            if (index >= 0) break;
//        }
        return index;
    }
    public int getIntersectIndex(Location location1) {
        int index = -1;
        for (int i=0, max=list.size(); i<max; i++) {
            Plate plate = list.get(i);
            Location location2 = plate.getLocation();
            int dx = Math.abs(location1.getBlockX() - location2.getBlockX());
            int dy = location1.getBlockY() - location2.getBlockY();
            int dz = Math.abs(location1.getBlockZ() - location2.getBlockZ());
            if (dx <= PlateManager.MAX_X/2 && (dy >= 0 && dy < PlateManager.MAX_Y) && dz <= PlateManager.MAX_Z/2) {
                index = i;
                break;
            }
        }
        return index;
//        Plate plate1 = null;
//        for (Plate plate2: list) {
//            Location location2 = plate2.getLocation();
//            int dx = Math.abs(location1.getBlockX() - location2.getBlockX());
//            int dy = location1.getBlockY() - location2.getBlockY();
//            int dz = Math.abs(location1.getBlockZ() - location2.getBlockZ());
//            if (dx <= PlateManager.MAX_X/2 && (dy >= 0 && dy < PlateManager.MAX_Y) && dz <= PlateManager.MAX_Z/2) {
//                plate1 = plate2;
//            }
//        }
//        return plate1;
    }
}