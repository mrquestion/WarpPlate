package mrq.plugin.minecraft.move.tool;

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
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import mrq.plugin.pattern.PluginListener;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.World;

import com.google.common.io.Files;

public class PlateFile {
    private static final PlateFile instance = new PlateFile();
    public static final PlateFile getInstance() {
        return instance;
    }
    
    private File file = null;
    
    private PlateFile() {
        initialize();
    }
    
    public int load(Server server) {
        int count = -1;
        if (file != null) {
            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
                
                String s = null;
                Pattern pattern = Pattern.compile("^([^:]+):([^:]+):([\\d-]+),([\\d-]+),([\\d-]+):(.*):([\\d]*):(.*)$");
                while ((s=br.readLine()) != null) {
                    Matcher matcher = pattern.matcher(s);
                    if (matcher.find()) {
                        World world = server.getWorld(matcher.group(1));
                        String player = matcher.group(2);
                        double x = Double.parseDouble(matcher.group(3));
                        double y = Double.parseDouble(matcher.group(4));
                        double z = Double.parseDouble(matcher.group(5));
                        Location location = new Location(world, x, y, z);
                        String[] ss = matcher.group(6).split(",");
                        Material[] patterns = new Material[ss.length];
                        for (int i=0, max=patterns.length; i<max; i++) {
                            patterns[i] = Material.getMaterial(ss[i]);
                        }
                        String time = matcher.group(7);
                        String name = matcher.group(8);
                        Plate plate = new Plate(world, player, location, patterns, time, name);
                        PlateList pl = PlateList.getInstance();
                        if (pl.getIndex(plate) < 0) pl.add(plate);
//                        if (pl.exist(plate));
//                        else pl.add(plate);
//                        PlateList.getInstance().add(world, player, location, patterns, name);
                    }
                }
                
                br.close();
            } catch (FileNotFoundException e) {
                count = -1;
                e.printStackTrace();
            } catch (IOException e) {
                count = -1;
                e.printStackTrace();
            }
        }
        return count;
    }
    public void save() {
        if (file != null) {
            if (file.exists() == false) initialize();;
            
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("YYYYMMddHHmmss", Locale.US);
                sdf.setTimeZone(TimeZone.getTimeZone("GMT+9"));
                Files.move(file, new File(file.getParentFile(), String.format("list_%s.plate", sdf.format(new Date()))));
//                Files.move(file, new File(file.getParentFile(), String.format("list_%s.plate", TimeStamp.format())));
            } catch (IOException e) {
                e.printStackTrace();
            }
            
            try {
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)));
                
                PlateList pl = PlateList.getInstance();
                for (int i=0, maxi=pl.size(); i<maxi; i++) {
                    Plate plate = pl.get(i);
                    StringBuilder sb = new StringBuilder();
                    World world = plate.getWorld();
                    Location location = plate.getLocation();
                    String time = plate.getTime();
                    String name = plate.getName();
                    sb.append(String.format("%s:%s:%d,%d,%d:", world.getName(), plate.getPlayer(), location.getBlockX(), location.getBlockY(), location.getBlockZ()));
                    for (int j=0, maxj=plate.size(); j<maxj; j++) {
                        if (j > 0) sb.append(",");
                        sb.append(plate.getPattern(j));
                    }
                    sb.append(":");
                    if (time != null) sb.append(time);
                    sb.append(":");
                    if (name != null) sb.append(name);
                    sb.append(System.lineSeparator());
                    bw.write(sb.toString());
                }
                bw.flush();
                
                bw.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    private void initialize() {
        file = new File(PluginListener.PLUGIN_PATH);
        if (file.exists()) {
            if (file.isDirectory() == false) {
                file.delete();
                file.mkdir();
            }
        }
        else file.mkdir();
        
        file = new File(String.format("%slist.plate", PluginListener.PLUGIN_PATH));
        if (file.exists() == false) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                file = null;
                e.printStackTrace();
            }
        }
    }
}