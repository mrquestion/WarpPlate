package mrq.plugin.minecraft.move;

import java.io.FileNotFoundException;
import java.io.IOException;

import mrq.plugin.minecraft.move.tool.Plate;
import mrq.plugin.minecraft.move.tool.PlateFile;
import mrq.plugin.minecraft.move.tool.PlateList;
import mrq.plugin.minecraft.move.tool.PlateManager;
import mrq.plugin.minecraft.move.tool.TeleportAction;
import mrq.plugin.minecraft.tool.ColorString;
import mrq.plugin.minecraft.tool.m;
import mrq.plugin.pattern.AbstractAction;
import mrq.plugin.pattern.PluginListener;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.craftbukkit.v1_7_R1.command.ColouredConsoleSender;
import org.bukkit.craftbukkit.v1_7_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;

public class WarpPlate extends PluginListener {
    private static final int DEFAULT_DELAY = 3;
    
    private int delay = DEFAULT_DELAY;
    
    @Override
    public void onEnable() {
        super.onEnable();
        
        try {
            fc.load(String.format("%s%s.properties", PLUGIN_PATH, pdf.getName()));
            delay = fc.getInt("server.warp.delay");
        } catch (FileNotFoundException e) {
            fc.set("server.warp.delay", DEFAULT_DELAY);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InvalidConfigurationException e) {
            e.printStackTrace();
        }
        
        PlateList pl = PlateList.getInstance();
        pl.clear();
        PlateFile.getInstance().load(getServer());
        m.sg(String.format("%d Warp Plate detected!", pl.size()));
    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        boolean success = false;
        
        if (sender instanceof CraftPlayer) {
            Player player = (Player)sender;
            if (args == null || args.length == 0) {
                player.sendMessage(ColorString.format("#1[#lp%s#1] $x명령어 도움말", pdf.getName()));
                player.sendMessage(ColorString.format(" - #1/%s #dg<번호> #1: $x설치된 #lpWarpPlate$x의 정보를 보여줍니다.", command.getName()));
            }
            else if (args.length == 1) {
                PlateList pl = PlateList.getInstance();
                try {
                    int index = Integer.parseInt(args[0]);
                    if (index <= 0) {
                        player.sendMessage(ColorString.format("#1[#lp%s#1] $x범위를 벗어난 번호입니다. #dg자연수$x를 입력해주세요.", pdf.getName()));
                    }
                    else if (index > pl.size()) {
                        player.sendMessage(ColorString.format("#1[#lp%s#1] $x##dg%d $x- 없는 번호입니다. #.5(총 등록된 수 : #dg%d#.5개)", pdf.getName(), index, pl.size()));
                    }
                    else {
                        Plate plate = pl.get(index-1);
                        if (plate == null) {
                            player.sendMessage(ColorString.format("#1[#lp%s#1] #r오류$x가 발생했습니다.", pdf.getName()));
                        }
                        else {
                            player.sendMessage(ColorString.format("#1[#lp%s#1] $x##dg%d $x- (X: %d, Y: %d, Z: %d)", pdf.getName(), index, plate.getX(), plate.getY(), plate.getZ()));
                            player.sendMessage(plate.toString(1));
                            player.sendMessage(plate.toString(2));
                            player.sendMessage(plate.toString(3));
                        }
                    }
                } catch (NumberFormatException e) {
                    player.sendMessage(ColorString.format("#1[#lp%s#1] #dg숫자$x가 #r아닙니다.", pdf.getName()));
                }
            }
            success = true;
        }
        else if (sender instanceof ColouredConsoleSender){
            if (args == null || args.length == 0) {
                m.sg(String.format("[%s] You can use next command: -d( or --delay)", pdf.getName()));
            }
            else {
                if (args[0].equalsIgnoreCase("-d") || args[0].equalsIgnoreCase("--delay")) {
                    if (args.length == 2) {
                        try {
                            int before = delay;
                            delay = Integer.parseInt(args[1]);
                            fc.set("server.warp.delay", delay);
                            m.sg(String.format("Warp delay changed: %d → %d", before, delay));
                            StringBuilder sb = new StringBuilder("#lp%s$x의 대기 시간이 바뀌었습니다! : ");
                            sb.append(String.format("%d → %d", before, delay));
                            if (delay == 0) sb.append("(즉시)");
                            getServer().broadcastMessage(ColorString.format(sb.toString(), pdf.getName()));
                        } catch (NumberFormatException e) {
                            m.sg(String.format("[%s] Usage: %s %s <delay:second(integer)>", pdf.getName(), command.getName(), args[0]));
                        }
                    }
                    else {
                        m.sg(String.format("[%s] Delay: %s second", pdf.getName(), fc.get("server.warp.delay")));
                        m.sg(String.format("[%s] Usage: %s %s <delay:second(integer)>", pdf.getName(), command.getName(), args[0]));
                    }
                }
                else m.sg(String.format("Invalid command: %s", args[0]));
            }
            success = true;
        }
        
        return success;
    }
    @Override
    public void onDisable() {
        try {
            fc.save(String.format("%s%s.properties", PLUGIN_PATH, pdf.getName()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        PlateFile.getInstance().save();
        super.onDisable();
    }
    
    @EventHandler (priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent pje) {
        Player player = pje.getPlayer();
        // TODO 갯수 수정
        int size = PlateList.getInstance().size();
        if (size > 0) {
            player.sendMessage(ColorString.format("#1[#lp%s #1v%s] #dg%d $x개의 #lp%s$x이(가) #b활성화$x되어 있습니다!", pdf.getName(), pdf.getVersion(), size, pdf.getName()));
        }
        else {
            player.sendMessage(ColorString.format("#1[#lp%s #1v%s] 현재 #b활성화$x되어 있는 #lp%s$x이(가) #dr없습니다$x.", pdf.getName(), pdf.getVersion(), pdf.getName()));
        }
    }
    @EventHandler (priority = EventPriority.LOWEST)
    public void onPlayerInteract(PlayerInteractEvent pie) {
        Action action = pie.getAction();
        if (action.equals(Action.RIGHT_CLICK_BLOCK)) {
            Player player = pie.getPlayer();
            Block block = pie.getClickedBlock();
            if (block.getType().equals(PlateManager.ACTIVATE_BLOCK)) {
                Location location = block.getLocation();
                
                int direction = PlateManager.getDestinationDiretion(location);
//                if (direction < 0) {
//                    player.sendMessage("pattern not matched");
//                }
//                else {
                if (direction > 0) {
//                    String s = null;
//                    switch (direction) {
//                        case PlateManager.NORTH: s = "north"; break;
//                        case PlateManager.SOUTH: s = "south"; break;
//                        case PlateManager.WEST: s = "west"; break;
//                        case PlateManager.EAST: s = "east"; break;
//                    }
                    Plate plate = PlateManager.getPattern(player, location, direction);
                    
                    PlateList pl = PlateList.getInstance();
//                    if (pl.exist(plate)) {
                    int index = pl.getIndex(plate);
                    if (index < 0) {
                        pl.add(plate);
                        PlateManager.setPlate(PlateManager.DESTINATION_ACTIVATE, location);
                        
                        m.sg(String.format("New Warp Plate! #%d (x: %d, y: %d, z: %d)", pl.size(), plate.getX(), plate.getY(), plate.getZ()));
                        player.sendMessage(ColorString.format("#lp%s$x의 설치에 #b성공$x했습니다!", pdf.getName()));
                        getServer().broadcastMessage(ColorString.format("새로운 #dg%d $x번째 #lp%s$x이(가) 설치되었습니다!", pl.size(), pdf.getName()));
                        World world = player.getWorld();
                        for (Player p: world.getPlayers()) {
                            Location l = p.getLocation();
                            if (p.equals(player) == false) {
                                l.setX(-(location.getBlockX() - l.getBlockX()));
                                l.setY(-(location.getBlockY() - l.getBlockY()));
                                l.setZ(-(location.getBlockZ() - l.getBlockZ()));
                                double d1 = 10;
                                double d2 = Math.sqrt(Math.pow(l.getBlockX(), 2) + Math.pow(l.getBlockY(), 2) + Math.pow(l.getBlockZ(), 2));
                                if (d2 > d1) {
                                    d1 /= d2;
                                    l.setX(l.getX() * d1);
                                    l.setY(l.getY() * d1);
                                    l.setZ(l.getZ() * d1);
                                }
                                l.add(p.getLocation());
                            }
                            p.playSound(l, Sound.PORTAL, 1, 1);
                            p.playSound(l, Sound.LEVEL_UP, 1, 1);
                        }
                    }
                    else {
                        player.sendMessage(ColorString.format("#lp%s$x 설치 #dr실패$x: 등록된 패턴이 이미 있습니다. (#dg%d$x번)", pdf.getName(), index+1));
                        player.playSound(location, Sound.ITEM_BREAK, 1, 1);
                    }
                }
                else {
                    direction = PlateManager.getSourceDiretion(location);
                    if (direction > 0) {
                        Plate plate = PlateManager.getPattern(player, location, direction);
                        PlateList pl = PlateList.getInstance();
                        int index = pl.getIndex(plate);
                        if (index < 0) {
                            player.sendMessage(ColorString.format("#lp%s $x사용 #dr실패$x: 등록된 패턴이 없습니다.", pdf.getName()));
                            player.playSound(location, Sound.ITEM_BREAK, 1, 1);
                        }
                        else {
                            plate = pl.get(index);
                            location = plate.getLocation().clone().add(0.5, 1, 0.5);
                            AbstractAction aa = new TeleportAction(WarpPlate.this, player, location, delay, index+1);
                            getServer().getScheduler().scheduleSyncDelayedTask(WarpPlate.this, aa);
                        }
                    }
                    else {
                        boolean success = PlateManager.getSimpleDirection(location);
                        if (success) {
                            Location spawn = player.getBedSpawnLocation();
                            if (spawn == null) {
                                player.sendMessage(ColorString.format("기존에 저장된 위치가 #r없습니다$x."));
                            }
                            else {
                                PlateManager.setPlate(PlateManager.SIMPLE, location);
                                AbstractAction aa = new TeleportAction(WarpPlate.this, player, spawn, delay, TeleportAction.HOME);
                                getServer().getScheduler().scheduleSyncDelayedTask(WarpPlate.this, aa);
                            }
                        }
                        else {
                            
                        }
                    }
                }
            }
        }
    }
    @EventHandler (priority = EventPriority.HIGHEST)
    public void onBlockPlace(BlockPlaceEvent bpe) {
        breakPlate(bpe.getBlock(), bpe.getPlayer());
    }
    @EventHandler (priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent bbe) {
        breakPlate(bbe.getBlock(), bbe.getPlayer());
    }
    
    private void breakPlate(Block block, Player player) {
        World world = block.getWorld();
        Location location = block.getLocation();
        PlateList pl = PlateList.getInstance();
        int index = pl.getIntersectIndex(block.getLocation());
        if (index >= 0) {
            Plate plate = pl.get(index);
            PlateManager.setPlate(PlateManager.DESTINATION_DEACTIVATE, plate.getLocation());
            pl.remove(index);
            
            m.sg(String.format("Warp Plate #%d destroyed.", (index+1)));
            getServer().broadcastMessage(ColorString.format("기존에 설치된 #dg%d$x번 #lp%s$x이(가) #r파괴$x되었습니다!", (index+1), pdf.getName()));
            for (Player p: world.getPlayers()) {
                Location l = p.getLocation();
                if (p.equals(player) == false) {
                    l.setX(-(location.getBlockX() - l.getBlockX()));
                    l.setY(-(location.getBlockY() - l.getBlockY()));
                    l.setZ(-(location.getBlockZ() - l.getBlockZ()));
                    double d1 = 10;
                    double d2 = Math.sqrt(Math.pow(l.getBlockX(), 2) + Math.pow(l.getBlockY(), 2) + Math.pow(l.getBlockZ(), 2));
                    if (d2 > d1) {
                        d1 /= d2;
                        l.setX(l.getX() * d1);
                        l.setY(l.getY() * d1);
                        l.setZ(l.getZ() * d1);
                    }
                    l.add(p.getLocation());
                }
                p.playSound(l, Sound.ANVIL_BREAK, 1, 1);
            }
        }
    }
}