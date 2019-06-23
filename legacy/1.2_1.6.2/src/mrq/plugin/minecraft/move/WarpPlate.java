package mrq.plugin.minecraft.move;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashMap;

import mrq.plugin.minecraft.tool.CommandManager;
import mrq.plugin.minecraft.tool.CreateManager;
import mrq.plugin.minecraft.tool.WarpPatterns;
import mrq.plugin.minecraft.tool.m;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;


public class WarpPlate extends JavaPlugin implements Listener {
    private static final int[][] DESTINATION_STEP1 = {
        { -2, 0, -2 }, { -1, 0, -2 }, { 1, 0, -2 }, { 2, 0, -2 },
        { -2, 0, -1 }, { 2, 0, -1 },
        { -2, 0, 1 }, { 2, 0, 1 },
        { -2, 0, 2 }, { -1, 0, 2 }, { 1, 0, 2 }, { 2, 0, 2 }
    };
    private static final int[][] DESTINATION_STEP2 = {
        { -2, 1, -2 }, { 2, 1, -2 },
        { -2, 1, 2 }, { 2, 1, 2 }
    };
    private static final int[][] DESTINATION_STEP3 = {
        { -1, 1, -2 }, { 0, 1, -2 }, { 1, 1, -2 },
        { -2, 1, -1 }, { -1, 1, -1 }, { 0, 1, -1 }, { 1, 1, -1 }, { 2, 1, -1 },
        { -2, 1, 0 }, { -1, 1, 0 }, { 0, 1, 0 }, { 1, 1, 0 }, { 2, 1, 0 },
        { -2, 1, 1 }, { -1, 1, 1 }, { 0, 1, 1 }, { 1, 1, 1 }, { 2, 1, 1 },
        { -1, 1, 2 }, { 0, 1, 2 }, { 1, 1, 2 }
    };
    private static final int[][] DESTINATION_STEP4 = {
        { 0, 0, -2 }, { 0, 0, 2 }, { -2, 0, 0 }, { 2, 0, 0 }
    };
    
    private static final int[][] SOURCE_STEP1 = {
        { -1, 0, -2 }, { 0, 0, -2 }, { 1, 0, -2 },
        { -2, 0, -1 }, { -2, 0, 0 }, { -2, 0, 1 },
        { 2, 0, -1 }, { 2, 0, 0 }, { 2, 0, 1 },
        { -1, 0, 2 }, { 0, 0, 2 }, { 1, 0, 2 },
    };
    private static final int[][] SOURCE_STEP2 = {
        { -2, 0, -2 }, { 2, 0, -2 },
        { -2, 0, 2 }, { 2, 0, 2 }
    };
    private static final int[][] SOURCE_STEP3 = {
        { 0, 1, -2 }, { 0, 1, 2 }, { -2, 1, 0 }, { 2, 1, 0 }
    };
    
    private static final int[][] SIMPLE_STEP1 = {
        { -1, -2 }, { 1, -2 },
        { -2, -1 }, { -1, -1 }, { 1, -1 }, { 2, -1 },
        { -2, 1 }, { -1, 1 }, { 1, 1 }, { 2, 1 },
        { -1, 2 }, { 1, 2 },
    };
    
    private static final int[][] PATTERN_NS = {
        { -1, 0, -1 }, { 0, 0, -1 }, { 1, 0, -1 },
        { -1, 0, 0 }, { 1, 0, 0 },
        { -1, 0, 1 }, { 0, 0, 1 }, { 1, 0, 1 }
    };
    private static final int[][] PATTERN_WE = {
        { -1, 0, 1 }, { -1, 0, 0 }, { -1, 0, -1 },
        { 0, 0, 1 }, { 0, 0, -1 },
        { 1, 0, 1 }, { 1, 0, 0 }, { 1, 0, -1 },
    };
    
    private static final int DO_DESTINATION = 1;
    private static final int DO_SOURCE = 2;
    private static final int DO_SIMPLE = 3;
    private static final int DO_RECOVERY = 4;
    
    private int delay = 3;
    private HashMap<String, Integer> hm = null;
    
    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        m.sg(this, "plugin is enabled!");
        
        hm = new HashMap<>();
        
        WarpPatterns wp = WarpPatterns.getInstance();
        wp.load(getServer());
        m.sg(this, wp.size() + " Warp Plate detected!");

        File file = new File("plugins" + File.separator + "WarpPlate" + File.separator + "WarpPlate.properties");
        if (CreateManager.createFile(this, file)) {
            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
                String s = br.readLine();
                if (s != null) {
                    String[] ss = s.split("=");
                    if (ss.length == 2 && ss[0].equals("delay")) {
                        try {
                            int delay = Integer.parseInt(ss[1]);
                            this.delay = delay;
                        } catch (NumberFormatException e) {
                            this.delay = 3;
                        }
                    }
                }
                br.close();
            } catch (FileNotFoundException e) {
                m.sg(this, file.getName() + " 파일을 찾을 수 없습니다: " + e.getMessage());
            } catch (IOException e) {
                m.sg(this, "파일을 읽는 중 오류가 발생했습니다: " + e.getMessage());
            }
        }
        m.sg(this, "Warp Delay is " + delay);
    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        boolean b = false;
        String s = command.getName();
        if (sender instanceof Player) {
            Player player = (Player)sender;
            if (s.equalsIgnoreCase("WarpPlate")) {
                if (args.length == 0) {
                    player.sendMessage(ChatColor.WHITE + "[" + ChatColor.LIGHT_PURPLE + "WarpPlate" + ChatColor.WHITE + "] 명령어 도움말");
                    player.sendMessage(ChatColor.WHITE + "  /WarpPlate " + ChatColor.DARK_GREEN + "<번호>" + ChatColor.WHITE + " : 설치된 " + ChatColor.LIGHT_PURPLE + "Warp Plate" + ChatColor.WHITE  + "의 정보를 보여줍니다.");
                }
                else if (args.length == 1) {
                    b = CommandManager.showWarpPlate(player, args[0]);
                }
            }
        }
        else {
            if (s.equalsIgnoreCase("WarpPlate") || s.equalsIgnoreCase("/WarpPlate")) {
                if (args.length == 0) {
                    m.sg(this, "You can use next command: -d, --delay");
                }
                else {
                    if (args[0].equalsIgnoreCase("-d") || args[0].equalsIgnoreCase("--delay")) {
                        if (args.length == 2) {
                            b = CommandManager.changeWarpDelay(this, args[1]);
                        }
                        else {
                            m.sg(this, "Usage: /WarpPlate " + args[0] + " <delay:integer>");
                        }
                    }
                    else {
                        m.sg(this, "Invalid command: " + args[0]);
                    }
                }
            }
        }
        return b;
    }
    @Override
    public void onDisable() {
        m.sg(this, "plugin is disabled!");
        
        WarpPatterns wp = WarpPatterns.getInstance();
        m.sg(this, "Save " + wp.size() + " Warp Plate list...");
        wp.save();
        m.sg(this, "Done.");

        File file = new File("plugins" + File.separator + "WarpPlate" + File.separator + "WarpPlate.properties");
        if (CreateManager.createFile(this, file)) {
            try {
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)));
                bw.write("delay=" + delay);
                bw.close();
            } catch (FileNotFoundException e) {
                m.sg(this, file.getName() + " 파일을 찾을 수 없습니다: " + e.getMessage());
            } catch (IOException e) {
                m.sg(this, "파일을 쓰는 중 오류가 발생했습니다: " + e.getMessage());
            }
        }
    }
    
    @EventHandler (priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent pje) {
        pje.getPlayer().sendMessage(ChatColor.WHITE + "[" + ChatColor.LIGHT_PURPLE + "WarpPlate" + ChatColor.WHITE + " v" + getDescription().getVersion() + "] " + ChatColor.DARK_GREEN + WarpPatterns.getInstance().size() + ChatColor.WHITE + " 개의 " + ChatColor.LIGHT_PURPLE + "Warp Plate" + ChatColor.WHITE + "가 " + ChatColor.BLUE + "사용가능" + ChatColor.WHITE + "합니다!");
    }
    @EventHandler (priority = EventPriority.LOWEST)
    public void onPlayerInteract(PlayerInteractEvent pie) {
        if (pie.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
            Player player = pie.getPlayer();
            
            Block block = pie.getClickedBlock();
            if (block.getType() == Material.COBBLESTONE) {
                Integer doWhat = hm.get(player.getWorld() + "." + player.getName());
                if (doWhat == null) {
                    if (doDestination(block, player)) { }
                    else if (doSource(block, player)) { }
                    else if (doSimple(block, player)) { }
                }
                else {
                    switch (doWhat) {
                        case DO_DESTINATION:
                            player.sendMessage(ChatColor.LIGHT_PURPLE + "Warp Plate" + ChatColor.WHITE + "를 설치 중 입니다. 잠시만 기다려 주세요.");
                            break;
                        case DO_SOURCE:
                        case DO_SIMPLE:
                            player.sendMessage(ChatColor.AQUA + "Warp" + ChatColor.WHITE + " 중 입니다. 잠시만 기다려 주세요.");
                            break;
                        case DO_RECOVERY:
                            player.sendMessage(ChatColor.LIGHT_PURPLE + "Warp Plate" + ChatColor.WHITE + "를 복구 중 입니다. 잠시만 기다려 주세요.");
                            break;
                    }
                }
            }
            else if (block.getType() == Material.MOSSY_COBBLESTONE) {
                if (doRecovery(block)) {
                    player.sendMessage(ChatColor.WHITE + "비정상적인 " + ChatColor.LIGHT_PURPLE + "Warp Plate" + ChatColor.WHITE + "를 " + ChatColor.RED + "강제" + ChatColor.WHITE + "로 되돌렸습니다.");
                    player.playSound(player.getLocation(), Sound.DOOR_OPEN, 1, 1);
                }
            }
        }
    }
    @EventHandler (priority = EventPriority.HIGHEST)
    public void onBlockPlace(BlockPlaceEvent bpe) {
        Player player = bpe.getPlayer();
        WarpPatterns wp = WarpPatterns.getInstance();
        int index = wp.getIncludeIndex(bpe.getBlock().getLocation());
        if (index >= 0) {
            World world = player.getWorld();
            Block block1 = world.getBlockAt(wp.getLocation(index));
            block1.setType(Material.COBBLESTONE);
            for (int i=0; i<DESTINATION_STEP1.length; i++) {
                Block block2 = world.getBlockAt(block1.getLocation().add(DESTINATION_STEP1[i][0], DESTINATION_STEP1[i][1], DESTINATION_STEP1[i][2]));
                block2.setType(Material.COBBLESTONE);
            }
            for (int i=0; i<DESTINATION_STEP2.length; i++) {
                Block block2 = world.getBlockAt(block1.getLocation().add(DESTINATION_STEP2[i][0], DESTINATION_STEP2[i][1], DESTINATION_STEP2[i][2]));
                block2.setType(Material.REDSTONE_TORCH_OFF);
            }
            
            wp.remove(index);
            getServer().broadcastMessage(ChatColor.WHITE + "기존에 설치된 " + ChatColor.DARK_GREEN + (index+1) + ChatColor.WHITE + " 번째 " + ChatColor.LIGHT_PURPLE + "Warp Plate" + ChatColor.WHITE + "가 " + ChatColor.RED + "파괴" + ChatColor.WHITE + "되었습니다!");
            for (Player p: player.getWorld().getPlayers()) {
                p.playSound(p.getLocation(), Sound.ANVIL_BREAK, 1, 1);
            }
        }
    }
    @EventHandler (priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent bbe) {
        Player player = bbe.getPlayer();
        WarpPatterns wp = WarpPatterns.getInstance();
        int index = wp.getIncludeIndex(bbe.getBlock().getLocation());
        if (index >= 0) {
            World world = player.getWorld();
            Block block1 = world.getBlockAt(wp.getLocation(index));
            changePlate(block1, false);
            
            wp.remove(index);
            getServer().broadcastMessage(ChatColor.WHITE + "기존에 설치된 " + ChatColor.DARK_GREEN + (index+1) + ChatColor.WHITE + " 번째 " + ChatColor.LIGHT_PURPLE + "Warp Plate" + ChatColor.WHITE + "가 " + ChatColor.RED + "파괴" + ChatColor.WHITE + "되었습니다!");
            for (Player p: player.getWorld().getPlayers()) {
                p.playSound(p.getLocation(), Sound.ANVIL_BREAK, 1, 1);
            }
        }
    }

    private class Teleport implements Runnable {
        private static final int DELAY = 10;
        
        private Player player = null;
        private Location location = null;
        private int index = 0, repeat = 0, count = 0;
        
        private Teleport(Player player, Location location, int index) {
            this.player = player;
            this.location = location;
            this.index = index;
            repeat = delay * 2;
            count = repeat;
        }
        @Override
        public void run() {
            location.setPitch(player.getLocation().getPitch());
            location.setYaw(player.getLocation().getYaw());
            if (player.teleport(location)) {
                if (count == repeat) {
                    if (index > 0) {
                        player.playSound(location, Sound.PORTAL_TRAVEL, 0.3f, 3);
                        player.playSound(location, Sound.DOOR_OPEN, 1, 1);
                        m.sg(this, "Warp to #" + index + " success! : " + player.getName());
                    }
                    else {
                        player.playSound(location, Sound.PORTAL_TRIGGER, 0.5f, 2);
                        player.playSound(location, Sound.DOOR_OPEN, 1, 1);
                        m.sg(this, "Warp to Home success! : " + player.getName());
                    }
                }
                if (count > 0 && count%2 == 0) {
                    player.sendMessage(ChatColor.DARK_GREEN + String.format(" %" + ((int)Math.log10(repeat)+1) + "d", count/2) + ChatColor.WHITE + "...");
                    player.playSound(player.getLocation(), Sound.CLICK, 1, 1);
                }
                count--;
                
                if (count > 0) {
                    getServer().getScheduler().scheduleSyncDelayedTask(WarpPlate.this, this, DELAY);
                }
                else {
                    if (index > 0) {
                        player.sendMessage(ChatColor.AQUA + "Warp" + ChatColor.WHITE + "에 " + ChatColor.BLUE + "성공" + ChatColor.WHITE + "했습니다!");
                    }
                    else {
                        player.sendMessage(ChatColor.WHITE + "기존에 저장된 위치로 " + ChatColor.AQUA + "Warp" + ChatColor.WHITE + "에 " + ChatColor.BLUE + "성공" + ChatColor.WHITE + "했습니다!");
                    }
                    player.playSound(player.getLocation(), Sound.CHEST_CLOSE, 1, 1);
                    
                    hm.remove(player.getWorld() + "." + player.getName());
                }
            }
            else {
                if (index > 0) {
                    m.sg(this, "Warp to #" + index + " fail... : " + player.getName());
                }
                else {
                    m.sg(this, "Warp to Home fail... : " + player.getName());
                }
            }
        }
    }
    
    private boolean doDestination(Block block1, Player player) {
        boolean b = true;
        
        WarpPatterns wp = WarpPatterns.getInstance();
        if (wp.isIncluded(block1.getLocation())) {
            b = false;
        }
        
        World world = block1.getWorld();
        if (b) {
            for (int i=-2; i<=2; i++) {
                for (int j=-2; j<=2; j++) {
                    Block block2 = world.getBlockAt(block1.getLocation().add(i, 2, j));
                    if (block2.getType() != Material.AIR) {
                        b = false;
                        break;
                    }
                }
                if (b == false) break;
            }
        }
        
        if (b) {
            for (int i=0; i<DESTINATION_STEP1.length; i++) {
                Block block2 = world.getBlockAt(block1.getLocation().add(DESTINATION_STEP1[i][0], DESTINATION_STEP1[i][1], DESTINATION_STEP1[i][2]));
                if (block2.getType() != block1.getType()) {
                    b = false;
                    break;
                }
            }
        }
        
        if (b) {
            for (int i=0; i<DESTINATION_STEP2.length; i++) {
                Block block2 = world.getBlockAt(block1.getLocation().add(DESTINATION_STEP2[i][0], DESTINATION_STEP2[i][1], DESTINATION_STEP2[i][2]));
                if (block2.getType() != Material.REDSTONE_TORCH_ON && block2.getType() != Material.REDSTONE_TORCH_OFF) {
                    b = false;
                    break;
                }
            }
        }
        
        if (b) {
            for (int i=0; i<DESTINATION_STEP3.length; i++) {
                Block block2 = world.getBlockAt(block1.getLocation().add(DESTINATION_STEP3[i][0], DESTINATION_STEP3[i][1], DESTINATION_STEP3[i][2]));
                if (block2.getType() != Material.AIR) {
                    b = false;
                    break;
                }
            }
        }
        
        char c = '\0';
        if (b) {
            int count = 0;
            for (int i=0; i<DESTINATION_STEP4.length; i++) {
                Block block2 = world.getBlockAt(block1.getLocation().add(DESTINATION_STEP4[i][0], DESTINATION_STEP4[i][1], DESTINATION_STEP4[i][2]));
                if (block2.getType() == Material.TORCH) {
                    switch (i) {
                        case 0: c = 'n'; break;
                        case 1: c = 's'; break;
                        case 2: c = 'w'; break;
                        case 3: c = 'e'; break;
                    }
                    count++;
                }
                else if (block2.getType() != Material.AIR) {
                    b = false;
                    break;
                }
            }
            if (b && count != 1) {
                b = false;
            }
        }
        
        if (b) {
            int[] pattern = new int[8];
            switch (c) {
                case 'n':
                    for (int i=0; i<PATTERN_NS.length; i++) {
                        Block block2 = world.getBlockAt(block1.getLocation().add(PATTERN_NS[i][0], PATTERN_NS[i][1], PATTERN_NS[i][2]));
                        pattern[i] = block2.getTypeId();
                    }
                    break;
                case 's':
                    for (int i=0; i<PATTERN_NS.length; i++) {
                        Block block2 = world.getBlockAt(block1.getLocation().add(PATTERN_NS[PATTERN_NS.length-1-i][0], PATTERN_NS[PATTERN_NS.length-1-i][1], PATTERN_NS[PATTERN_NS.length-1-i][2]));
                        pattern[i] = block2.getTypeId();
                    }
                    break;
                case 'w':
                    for (int i=0; i<PATTERN_WE.length; i++) {
                        Block block2 = world.getBlockAt(block1.getLocation().add(PATTERN_WE[i][0], PATTERN_WE[i][1], PATTERN_WE[i][2]));
                        pattern[i] = block2.getTypeId();
                    }
                    break;
                case 'e':
                    for (int i=0; i<PATTERN_WE.length; i++) {
                        Block block2 = world.getBlockAt(block1.getLocation().add(PATTERN_WE[PATTERN_WE.length-1-i][0], PATTERN_WE[PATTERN_WE.length-1-i][1], PATTERN_WE[PATTERN_WE.length-1-i][2]));
                        pattern[i] = block2.getTypeId();
                    }
                    break;
            }
            
            if (c != '\0') {
                Location location = block1.getLocation();
                if (wp.isMatched(world, pattern)) {
                    player.sendMessage(ChatColor.LIGHT_PURPLE + "Warp Plate" + ChatColor.WHITE + " 설치 " + ChatColor.RED + "실패" + ChatColor.WHITE + ": 등록된 패턴이 이미 있습니다.");
                    player.playSound(player.getLocation(), Sound.ITEM_BREAK, 1, 1);
                    b = false;
                }
                else {
                    hm.put(player.getWorld() + "." + player.getName(), DO_DESTINATION);
                    
                    wp.add(block1.getWorld(), location, pattern);
                    changePlate(block1, true);
                    m.sg(this, "New Warp Plate! #" + wp.size() + "(x: " + location.getBlockX() + ", y: " + location.getBlockY() + ", z: " + location.getBlockZ() + ")");
                    player.sendMessage(ChatColor.LIGHT_PURPLE + "Warp Plate" + ChatColor.WHITE + "의 설치에 " + ChatColor.BLUE + "성공" + ChatColor.WHITE + "했습니다!");
                    getServer().broadcastMessage(ChatColor.WHITE + "새로운 " + ChatColor.DARK_GREEN + wp.size() + ChatColor.WHITE + " 번째 " + ChatColor.LIGHT_PURPLE + "Warp Plate" + ChatColor.WHITE + "가 설치되었습니다!");
                    for (Player p: world.getPlayers()) {
                        p.playSound(p.getLocation(), Sound.PORTAL, 1, 1);
                        p.playSound(p.getLocation(), Sound.LEVEL_UP, 1, 1);
                    }
                    
                    hm.remove(player.getWorld() + "." + player.getName());
                }
            }
        }
        
        return b;
    }
    private boolean doSource(Block block1, Player player) {
        boolean b = true;
        
        World world = block1.getWorld();
        int count = 0;
        for (int i=-2; i<=2; i++) {
            for (int j=-2; j<=2; j++) {
                Block block2 = world.getBlockAt(block1.getLocation().add(i, 1, j));
                if (block2.getType() == Material.AIR) count++;
                
                Block block3 = world.getBlockAt(block1.getLocation().add(i, 2, j));
                if (block3.getType() == Material.AIR) count++;
            }
        }
        if (count != 49) {
            b = false;
        }
        
        if (b) {
            for (int i=0; i<SOURCE_STEP1.length; i++) {
                Block block2 = world.getBlockAt(block1.getLocation().add(SOURCE_STEP1[i][0], SOURCE_STEP1[i][1], SOURCE_STEP1[i][2]));
                if (block2.getType() != block1.getType()) {
                    b = false;
                    break;
                }
            }
        }
        
        if (b) {
            for (int i=0; i<SOURCE_STEP2.length; i++) {
                Block block2 = world.getBlockAt(block1.getLocation().add(SOURCE_STEP2[i][0], SOURCE_STEP2[i][1], SOURCE_STEP2[i][2]));
                if (block2.getType() != Material.AIR) {
                    b = false;
                    break;
                }
            }
        }
        
        char c = '\0';
        if (b) {
            count = 0;
            for (int i=0; i<SOURCE_STEP3.length; i++) {
                Block block2 = world.getBlockAt(block1.getLocation().add(SOURCE_STEP3[i][0], SOURCE_STEP3[i][1], SOURCE_STEP3[i][2]));
                if (block2.getType() == Material.TORCH) {
                    switch (i) {
                        case 0: c = 'n'; break;
                        case 1: c = 's'; break;
                        case 2: c = 'w'; break;
                        case 3: c = 'e'; break;
                    }
                    count++;
                }
                else if (block2.getType() != Material.AIR) {
                    b = false;
                    break;
                }
            }
            if (b && count != 1) {
                b = false;
            }
        }
        
        if (b) {
            int[] pattern = new int[8];
            switch (c) {
                case 'n':
                    for (int i=0; i<PATTERN_NS.length; i++) {
                        Block block2 = world.getBlockAt(block1.getLocation().add(PATTERN_NS[i][0], PATTERN_NS[i][1], PATTERN_NS[i][2]));
                        pattern[i] = block2.getTypeId();
                    }
                    break;
                case 's':
                    for (int i=0; i<PATTERN_NS.length; i++) {
                        Block block2 = world.getBlockAt(block1.getLocation().add(PATTERN_NS[PATTERN_NS.length-1-i][0], PATTERN_NS[PATTERN_NS.length-1-i][1], PATTERN_NS[PATTERN_NS.length-1-i][2]));
                        pattern[i] = block2.getTypeId();
                    }
                    break;
                case 'w':
                    for (int i=0; i<PATTERN_WE.length; i++) {
                        Block block2 = world.getBlockAt(block1.getLocation().add(PATTERN_WE[i][0], PATTERN_WE[i][1], PATTERN_WE[i][2]));
                        pattern[i] = block2.getTypeId();
                    }
                    break;
                case 'e':
                    for (int i=0; i<PATTERN_WE.length; i++) {
                        Block block2 = world.getBlockAt(block1.getLocation().add(PATTERN_WE[PATTERN_WE.length-1-i][0], PATTERN_WE[PATTERN_WE.length-1-i][1], PATTERN_WE[PATTERN_WE.length-1-i][2]));
                        pattern[i] = block2.getTypeId();
                    }
                    break;
            }
            
            if (c != '\0') {
                WarpPatterns wp = WarpPatterns.getInstance();
                int i = wp.getMatched(world, pattern);
                if (i >= 0) {
                    hm.put(player.getWorld() + "." + player.getName(), DO_SOURCE);
                    
                    player.sendMessage(ChatColor.AQUA + "Warp" + ChatColor.WHITE + " 중 입니다. 잠시만 기다려 주세요...");
                    
                    Location location = wp.getLocation(i).clone().add(0.5, 1, 0.5);
                    getServer().getScheduler().scheduleSyncDelayedTask(this, new Teleport(player, location, i+1));
                }
                else {
                    b = false;
                }
            }
        }
        
        return b;
    }
    private boolean doSimple(Block block1, Player player) {
        boolean b = true;
        
        World world = block1.getWorld();
        for (int i=0; i<SIMPLE_STEP1.length; i++) {
            Block block2 = world.getBlockAt(block1.getLocation().add(SIMPLE_STEP1[i][0], 0, SIMPLE_STEP1[i][1]));
            if (block2.getType() != block1.getType()) {
                b = false;
                break;
            }
        }
        
        if (b) {
            int count = 0;
            for (int i=-2; i<=2; i++) {
                for (int j=-2; j<=2; j++) {
                    Block block2 = world.getBlockAt(block1.getLocation().add(i, 0, j));
                    if (block2.getType() == Material.AIR) {
                        count++;
                    }
                }
            }
            
            if (count+SIMPLE_STEP1.length+1 < 5*5) {
                b = false;
            }
        }
        
        if (b) {
            Location location = player.getBedSpawnLocation();
            if (location == null) {
                player.sendMessage(ChatColor.WHITE + "기존에 저장된 위치가 " + ChatColor.RED + "없습니다.");
            }
            else {
                hm.put(player.getWorld() + "." + player.getName(), DO_SIMPLE);
                
                player.sendMessage(ChatColor.AQUA + "Warp" + ChatColor.WHITE + " 중 입니다. 잠시만 기다려 주세요...");
                
                location.add(0, 1, 0);
                getServer().getScheduler().scheduleSyncDelayedTask(this, new Teleport(player, location, 0));
                
                block1.setType(Material.AIR);
                for (int i=0; i<SIMPLE_STEP1.length; i++) {
                    Block block2 = world.getBlockAt(block1.getLocation().add(SIMPLE_STEP1[i][0], 0, SIMPLE_STEP1[i][1]));
                    block2.setType(Material.AIR);
                }
            }
        }
        
        return b;
    }
    private boolean doRecovery(Block block1) {
        boolean b = true;
        
        if (WarpPatterns.getInstance().isMatched(block1.getLocation())) {
            b = false;
        }
        
        int count = 0;
        if (b) {
            World world = block1.getWorld();
            for (int i=0; i<DESTINATION_STEP1.length; i++) {
                Block block2 = world.getBlockAt(block1.getLocation().add(DESTINATION_STEP1[i][0], DESTINATION_STEP1[i][1], DESTINATION_STEP1[i][2]));
                if (block2.getType() == Material.OBSIDIAN) {
                    count++;
                }
            }
            
            for (int i=0; i<DESTINATION_STEP2.length; i++) {
                Block block2 = world.getBlockAt(block1.getLocation().add(DESTINATION_STEP1[i][0], DESTINATION_STEP1[i][1], DESTINATION_STEP1[i][2]));
                if (block2.getType() == Material.TORCH) {
                    count++;
                }
            }
            
            if ((DESTINATION_STEP1.length+DESTINATION_STEP2.length)/count > 1) {
                b = false;
            }
        }
        
        if (b) {
            recoverPlate(block1);
        }
        
        return b;
    }
    private void changePlate(Block block1, boolean b) {
        World world = block1.getWorld();
        if (b) {
            block1.setType(Material.MOSSY_COBBLESTONE);
            for (int i=0; i<DESTINATION_STEP1.length; i++) {
                Block block2 = world.getBlockAt(block1.getLocation().add(DESTINATION_STEP1[i][0], DESTINATION_STEP1[i][1], DESTINATION_STEP1[i][2]));
                block2.setType(Material.OBSIDIAN);
            }
            for (int i=0; i<DESTINATION_STEP2.length; i++) {
                Block block2 = world.getBlockAt(block1.getLocation().add(DESTINATION_STEP2[i][0], DESTINATION_STEP2[i][1], DESTINATION_STEP2[i][2]));
                block2.setType(Material.TORCH);
            }
        }
        else {
            block1.setType(Material.COBBLESTONE);
            for (int i=0; i<DESTINATION_STEP1.length; i++) {
                Block block2 = world.getBlockAt(block1.getLocation().add(DESTINATION_STEP1[i][0], DESTINATION_STEP1[i][1], DESTINATION_STEP1[i][2]));
                block2.setType(Material.COBBLESTONE);
            }
            for (int i=0; i<DESTINATION_STEP2.length; i++) {
                Block block2 = world.getBlockAt(block1.getLocation().add(DESTINATION_STEP2[i][0], DESTINATION_STEP2[i][1], DESTINATION_STEP2[i][2]));
                block2.setType(Material.REDSTONE_TORCH_OFF);
            }
        }
    }
    private void recoverPlate(Block block1) {
        World world = block1.getWorld();
        block1.setType(Material.COBBLESTONE);
        for (int i=0; i<DESTINATION_STEP1.length; i++) {
            Block block2 = world.getBlockAt(block1.getLocation().add(DESTINATION_STEP1[i][0], DESTINATION_STEP1[i][1], DESTINATION_STEP1[i][2]));
            if (block2.getType() == Material.OBSIDIAN) {
                block2.setType(Material.COBBLESTONE);
            }
        }
        for (int i=0; i<DESTINATION_STEP2.length; i++) {
            Block block2 = world.getBlockAt(block1.getLocation().add(DESTINATION_STEP2[i][0], DESTINATION_STEP2[i][1], DESTINATION_STEP2[i][2]));
            if (block2.getType() == Material.TORCH) {
                block2.setType(Material.REDSTONE_TORCH_OFF);
            }
        }
    }
    
    public int getDelay() { return delay; }
    public void setDelay(int delay) { this.delay = delay; }
}