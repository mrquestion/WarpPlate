package mrq.plugin.minecraft.tool;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;

public class WarpPatterns {
    private static final WarpPatterns wp = new WarpPatterns();
    public static final WarpPatterns getInstance() {
        return wp;
    }
    
    private ArrayList<WarpPattern> al = null;
    
    private WarpPatterns() {
        al = new ArrayList<>();
    }
    
    private class WarpPattern {
        public static final int SIZE = 8;
        
        private World world = null;
        private Location location = null;
        private int[] pattern = null;
        
        private WarpPattern(World world, Location l, int[] pattern) {
            this.world = world;
            this.location = l;
            this.pattern = pattern;
        }
        
        private String getWorldName() {
            return world.getName();
        }
        private Location getLocation() {
            return location;
        }
        private int get(int i) {
            if (pattern == null) return -1;
            return pattern[i];
        }
    }
    
    public void load(Server server) {
        File file = new File("plugins" + File.separator + "WarpPlate" + File.separator + "list.dat");
        if (CreateManager.createFile(this, file)) {
            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
                String s;
                while ((s=br.readLine()) != null) {
                    String[] ss = s.split(":");
                    
                    World world = server.getWorld(ss[0]);
                    
                    String[] sss1 = ss[1].split(", ");
                    Location location = new Location(world, Integer.parseInt(sss1[0]), Integer.parseInt(sss1[1]), Integer.parseInt(sss1[2]));
                    
                    String[] sss2 = ss[2].split(", ");
                    int[] pattern = new int[WarpPattern.SIZE];
                    for (int i=0; i<pattern.length; i++) {
                        pattern[i] = Integer.parseInt(sss2[i]);
                    }
                    
                    add(world, location, pattern);
                }
                br.close();
            } catch (FileNotFoundException e) {
                m.sg(this, file.getName() + " 파일을 찾을 수 없습니다: " + e.getMessage());
            } catch (IOException e) {
                m.sg(this, "파일을 읽는 중 오류가 발생했습니다: " + e.getMessage());
            }
        }
    }
    public void save() {
        File file = new File("plugins" + File.separator + "WarpPlate" + File.separator + "list.dat");
        if (file.exists()) {
            SimpleDateFormat sdf = new SimpleDateFormat("YYYYMMDDHHmmss");
            sdf.setTimeZone(TimeZone.getTimeZone("GMT+9"));
            File file2 = new File("plugins" + File.separator + "WarpPlate" + File.separator + "list_" + sdf.format(new Date()) + ".dat");
            if (file.renameTo(file2) == false) {
                m.sg(this, "파일 이름 변경에 실패했습니다.");
            }
        }
        
        if (CreateManager.createFile(this, file)) {
            try {
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)));
                for (int i=0; i<al.size(); i++) {
                    WarpPattern wp = al.get(i);
                    StringBuilder sb = new StringBuilder();
                    
                    sb.append(wp.getWorldName());
                    sb.append(":");
                    
                    sb.append(wp.getLocation().getBlockX());
                    sb.append(", ");
                    sb.append(wp.getLocation().getBlockY());
                    sb.append(", ");
                    sb.append(wp.getLocation().getBlockZ());
                    sb.append(":");
                    
                    for (int j=0; j<WarpPattern.SIZE; j++) {
                        if (j > 0) sb.append(", ");
                        sb.append(wp.get(j));
                    }
                    
                    bw.write(sb.toString());
                    bw.newLine();
                }
                bw.flush();
                bw.close();
            } catch (FileNotFoundException e) {
                m.sg(this, file.getName() + " 파일을 찾을 수 없습니다: " + e.getMessage());
            } catch (IOException e) {
                m.sg(this, "파일을 쓰는 중 오류가 발생했습니다: " + e.getMessage());
            }
        }
    }
    
    public void add(World world, Location l, int[] pattern) {
        al.add(new WarpPattern(world, l, pattern));
    }
    public boolean isWorldEquals(World world, int i) {
        return al.get(i).getWorldName().equals(world.getName());
    }
    public Location getLocation(int i) {
        return al.get(i).getLocation();
    }
    public int getPattern(int i, int j) {
        return al.get(i).get(j);
    }
    public int size() {
        return al.size();
    }
    public int getIncludeIndex(Location l1) {
        int index = -1;
        for (int i=0; i<al.size(); i++) {
            Location l2 = getLocation(i);
            if (l1.getBlockX() >= l2.getBlockX()-2 && l1.getBlockX() <= l2.getBlockX()+2) {
                if (l1.getBlockY() >= l2.getBlockY() && l1.getBlockY() <= l2.getBlockY()+2) {
                    if (l1.getBlockZ() >= l2.getBlockZ()-2 && l1.getBlockZ() <= l2.getBlockZ()+2) {
                        index = i;
                        break;
                    }
                }
            }
        }
        return index;
    }
    public boolean isIncluded(Location l1) {
        boolean b = false;
        for (int i=0; i<al.size(); i++) {
            Location l2 = getLocation(i);
            if (l1.getBlockX() >= l2.getBlockX()-2 && l1.getBlockX() <= l2.getBlockX()+2) {
                if (l1.getBlockY() >= l2.getBlockY() && l1.getBlockY() <= l2.getBlockY()+2) {
                    if (l1.getBlockZ() >= l2.getBlockZ()-2 && l1.getBlockZ() <= l2.getBlockZ()+2) {
                        b = true;
                        break;
                    }
                }
            }
        }
        return b;
    }
    public boolean isMatched(Location l1) {
        boolean b = false;
        for (int i=0; i<al.size(); i++) {
            Location l2 = getLocation(i);
            if (l1.getBlockX() == l2.getBlockX() && l1.getBlockY() == l2.getBlockY() && l1.getBlockZ() == l2.getBlockZ()) {
                b = true;
                break;
            }
        }
        return b;
    }
    public int getMatched(World world, int[] pattern) {
        int index = -1;
        for (int i=0; i<al.size(); i++) {
            WarpPattern wp = al.get(i);
            if (wp.getWorldName().equals(world.getName())) {
                boolean b = true;
                for (int j=0; j<WarpPattern.SIZE; j++) {
                    if (wp.get(j) != pattern[j]) {
                        b = false;
                        break;
                    }
                }
                if (b) {
                    index = i;
                    break;
                }
            }
        }
        return index;
    }
    public boolean isMatched(World world, int[] pattern) {
        boolean b = false;
        for (int i=0; i<al.size(); i++) {
            WarpPattern wp = al.get(i);
            if (wp.getWorldName().equals(world.getName())) {
                b = true;
                for (int j=0; j<WarpPattern.SIZE; j++) {
                    if (wp.get(j) != pattern[j]) {
                        b = false;
                        break;
                    }
                }
                if (b) break;
            }
        }
        return b;
    }
    public void remove(int i) {
        al.remove(i);
    }
}