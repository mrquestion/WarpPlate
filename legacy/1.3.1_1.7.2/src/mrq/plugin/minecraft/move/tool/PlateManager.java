package mrq.plugin.minecraft.move.tool;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import mrq.plugin.minecraft.tool.TimeStamp;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class PlateManager {
    public static final Material ACTIVATE_BLOCK = Material.COBBLESTONE;
    public static final int DESTINATION_ACTIVATE = 1;
    public static final int DESTINATION_DEACTIVATE = 2;
    public static final int SOURCE = 3;
    public static final int SIMPLE = 4;
    public static final int RECOVERY = 5;
    public static final int NORTH = 1;
    public static final int SOUTH = 2;
    public static final int WEST = 3;
    public static final int EAST = 4;
    public static final int MAX_X = 5;
    public static final int MAX_Y = 3;
    public static final int MAX_Z = 5;
    
    private static final Material DESTINATION_BLOCK_ACTIVATED = Material.MOSSY_COBBLESTONE;
    private static final int[][] DESTINATION_STEP1_LOCATION = {
        { -2, 0, -2 }, { -1, 0, -2 }, { 1, 0, -2 }, { 2, 0, -2 },
        { -2, 0, -1 }, { 2, 0, -1 },
        { -2, 0, 1 }, { 2, 0, 1 },
        { -2, 0, 2 }, { -1, 0, 2 }, { 1, 0, 2 }, { 2, 0, 2 }
    };
    private static final Material DESTINATION_STEP1_BLOCK1 = Material.COBBLESTONE;
    private static final Material DESTINATION_STEP1_BLOCK2 = Material.OBSIDIAN;
    private static final int[][] DESTINATION_STEP2_LOCATION = {
        { -2, 1, -2 }, { 2, 1, -2 },
        { -2, 1, 2 }, { 2, 1, 2 }
    };
    private static final Material DESTINATION_STEP2_BLOCK1 = Material.REDSTONE_TORCH_ON;
    private static final Material DESTINATION_STEP2_BLOCK2 = Material.TORCH;
    private static final int[][] DESTINATION_STEP3_LOCATION = {
        { -1, 1, -2 }, { 0, 1, -2 }, { 1, 1, -2 },
        { -2, 1, -1 }, { -1, 1, -1 }, { 0, 1, -1 }, { 1, 1, -1 }, { 2, 1, -1 },
        { -2, 1, 0 }, { -1, 1, 0 }, { 0, 1, 0 }, { 1, 1, 0 }, { 2, 1, 0 },
        { -2, 1, 1 }, { -1, 1, 1 }, { 0, 1, 1 }, { 1, 1, 1 }, { 2, 1, 1 },
        { -1, 1, 2 }, { 0, 1, 2 }, { 1, 1, 2 }
    };
    private static final Material DESTINATION_STEP3_BLOCK1 = Material.AIR;
    private static final int[][][] DESTINATION_LOCATIONS = {
        DESTINATION_STEP1_LOCATION,
        DESTINATION_STEP2_LOCATION,
        DESTINATION_STEP3_LOCATION
    };
    private static final Material[] DESTINATION_BLOCKS_ACTIVATE = {
        DESTINATION_STEP1_BLOCK1,
        DESTINATION_STEP2_BLOCK1,
        DESTINATION_STEP3_BLOCK1
    };
    private static final Material[] DESTINATION_BLOCKS_DEACTIVATE = {
        DESTINATION_STEP1_BLOCK2,
        DESTINATION_STEP2_BLOCK2,
        DESTINATION_STEP3_BLOCK1
    };
    private static final int[][] DESTINATION_DIRECTION_LOCATION = {
        { 0, 0, -2 }, { 0, 0, 2 }, { -2, 0, 0 }, { 2, 0, 0 }
    };
    private static final Material DESTINATION_DIRECTION_BLOCK1 = Material.TORCH;
    private static final Material DESTINATION_DIRECTION_BLOCK2 = Material.AIR;
    
    private static final int[][] SOURCE_STEP1_LOCATION = {
        { -1, 0, -2 }, { 0, 0, -2 }, { 1, 0, -2 },
        { -2, 0, -1 }, { -2, 0, 0 }, { -2, 0, 1 },
        { 2, 0, -1 }, { 2, 0, 0 }, { 2, 0, 1 },
        { -1, 0, 2 }, { 0, 0, 2 }, { 1, 0, 2 },
    };
    private static final Material SOURCE_STEP1_BLOCK = Material.COBBLESTONE;
    private static final int[][] SOURCE_STEP2_LOCATION = {
        { -2, 0, -2 }, { 2, 0, -2 },
        { -2, 0, 2 }, { 2, 0, 2 }
    };
    private static final Material SOURCE_STEP2_BLOCK = Material.AIR;
    private static final int[][][] SOURCE_LOCATIONS = {
        SOURCE_STEP1_LOCATION,
        SOURCE_STEP2_LOCATION
    };
    private static final Material[] SOURCE_BLOCKS = {
        SOURCE_STEP1_BLOCK,
        SOURCE_STEP2_BLOCK
    };
    private static final int[][] SOURCE_DIRECTION_LOCATION = {
        { 0, 1, -2 }, { 0, 1, 2 }, { -2, 1, 0 }, { 2, 1, 0 }
    };
    private static final Material SOURCE_DIRECTION_BLOCK1 = Material.TORCH;
    private static final Material SOURCE_DIRECTION_BLOCK2 = Material.AIR;
    
    private static final int[][] SIMPLE_STEP1_LOCATION = {
        { -1, -2 }, { 1, -2 },
        { -2, -1 }, { -1, -1 }, { 1, -1 }, { 2, -1 },
        { -2, 1 }, { -1, 1 }, { 1, 1 }, { 2, 1 },
        { -1, 2 }, { 1, 2 },
    };
    private static final Material SIMPLE_STEP1_BLOCK = Material.COBBLESTONE;
    
    private static final int[][][] PATTERN = {
        { { -1, 0, -1 }, { 0, 0, -1 }, { 1, 0, -1 } },
        { { -1, 0, 0 }, { 0, 0, 0 }, { 1, 0, 0 } },
        { { -1, 0, 1 }, { 0, 0, 1 }, { 1, 0, 1 } }
    };
    
    public static int getDestinationDiretion(Location location) {
        int direction = -1;
        World world = location.getWorld();
        Block block = null;
        
        for (int i=0, max=DESTINATION_LOCATIONS.length; i<max; i++) {
            for (int[] is: DESTINATION_LOCATIONS[i]) {
                if (is.length == 3) {
                    block = world.getBlockAt(location.getBlockX()+is[0], location.getBlockY()+is[1], location.getBlockZ()+is[2]);
                    if (block.getType().equals(DESTINATION_BLOCKS_ACTIVATE[i]) == false) return -1;
                }
                else return -1;
            }
        }
        for (int i=-MAX_X/2; i<MAX_X/2; i++) {
            for (int j=-MAX_Z/2; j<MAX_Z/2; j++) {
                block = world.getBlockAt(location.getBlockX()+i, location.getBlockY()+2, location.getBlockZ()+j);
                if (block.getType().equals(Material.AIR) == false) return -1;
            }
        }
        
        int count1 = 0, count2 = 0;
        for (int i=0, max=DESTINATION_DIRECTION_LOCATION.length; i<max; i++) {
            int[] is = DESTINATION_DIRECTION_LOCATION[i];
            if (is.length == 3) {
                block = world.getBlockAt(location.getBlockX()+is[0], location.getBlockY()+is[1], location.getBlockZ()+is[2]);
                if (block.getType().equals(DESTINATION_DIRECTION_BLOCK1)) {
                    direction = i + 1;
                    count1++;
                }
                else if (block.getType().equals(DESTINATION_DIRECTION_BLOCK2)) count2++;
            }
        }
        if (count1 == 1 && count1+count2 == DESTINATION_DIRECTION_LOCATION.length) return direction;
        else return -1;
    }
    public static int getSourceDiretion(Location location) {
        int direction = -1;
        World world = location.getWorld();
        Block block = null;
        
        for (int i=0, max=SOURCE_LOCATIONS.length; i<max; i++) {
            for (int[] is: SOURCE_LOCATIONS[i]) {
                if (is.length == 3) {
                    block = world.getBlockAt(location.getBlockX()+is[0], location.getBlockY()+is[1], location.getBlockZ()+is[2]);
                    if (block.getType().equals(SOURCE_BLOCKS[i]) == false) return -1;
                }
                else return -1;
            }
        }
        for (int i=-MAX_X/2; i<MAX_X/2; i++) {
            for (int j=-MAX_Z/2; j<MAX_Z/2; j++) {
                block = world.getBlockAt(location.getBlockX()+i, location.getBlockY()+2, location.getBlockZ()+j);
                if (block.getType().equals(Material.AIR) == false) return -1;
            }
        }
        
        int count1 = 0, count2 = 0;
        for (int i=0, max=SOURCE_DIRECTION_LOCATION.length; i<max; i++) {
            int[] is = SOURCE_DIRECTION_LOCATION[i];
            if (is.length == 3) {
                block = world.getBlockAt(location.getBlockX()+is[0], location.getBlockY()+is[1], location.getBlockZ()+is[2]);
                if (block.getType().equals(SOURCE_DIRECTION_BLOCK1)) {
                    direction = i + 1;
                    count1++;
                }
                else if (block.getType().equals(SOURCE_DIRECTION_BLOCK2)) count2++;
            }
        }
        if (count1 == 1 && count1+count2 == SOURCE_DIRECTION_LOCATION.length) return direction;
        else return -1;
    }
    public static boolean getSimpleDirection(Location location) {
        World world = location.getWorld();
        int count = 0;
        for (int[] is: SIMPLE_STEP1_LOCATION) {
            if (is.length == 2) {
                Block block = world.getBlockAt(location.getBlockX()+is[0], location.getBlockY(), location.getBlockZ()+is[1]);
                if (block.getType().equals(SIMPLE_STEP1_BLOCK)) count++;
            }
        }
        if (count == SIMPLE_STEP1_LOCATION.length) return true;
        return false;
    }
    
    public static Plate getPattern(Player player, Location location, int direction) {
        int degree = 0;
        switch (direction) {
            case NORTH: degree = 0; break;
            case SOUTH: degree = 180; break;
            case WEST: degree = -90; break;
            case EAST: degree = 90; break;
        }
        int[][][] rotated = new int[PATTERN.length][PATTERN[0].length][PATTERN[0][0].length];
        for (int i=0, maxi=PATTERN.length; i<maxi; i++) {
            for (int j=0, maxj=PATTERN[i].length; j<maxj; j++) {
                Point p = getRotationPoint(i-maxi/2, j-maxj/2, degree);
                rotated[p.x+maxi/2][p.y+maxj/2] = PATTERN[i][j];
            }
        }
        
        World world = location.getWorld();
        Block block = null;
//        int count = 1;
//        StringBuilder sb = new StringBuilder("1");
        List<Material> list = new ArrayList<>();
        for (int[][] iss: rotated) {
            for (int[] is: iss) {
                if (is.length == 3) {
                    block = world.getBlockAt(location.getBlockX()+is[0], location.getBlockY()+is[1], location.getBlockZ()+is[2]);
                    list.add(block.getType());
//                    sb.append((count++) + ":" + block.getType() + ", ");
                }
            }
        }
//        for (Player player: world.getPlayers())
//        player.sendMessage(sb.toString());
        
        Material[] pattern = new Material[list.size()];
        for (int i=0, max=pattern.length; i<max; i++) pattern[i] = list.get(i);
        
        Plate plate = new Plate(player.getWorld(), player.getName(), location, pattern, TimeStamp.format(), null);
        return plate;
//        PlateList.getInstance().add(player, location, pattern);
    }
    public static void setPlate(int type, Location location) {
        World world = location.getWorld();
        Block block = world.getBlockAt(location);
        switch (type) {
            case DESTINATION_ACTIVATE:
            case RECOVERY:
                block.setType(DESTINATION_BLOCK_ACTIVATED);
                for (int i=0, max=DESTINATION_LOCATIONS.length; i<max; i++) {
                    for (int[] is: DESTINATION_LOCATIONS[i]) {
                        if (is.length == 3) {
                            block = world.getBlockAt(location.getBlockX()+is[0], location.getBlockY()+is[1], location.getBlockZ()+is[2]);
                            block.setType(DESTINATION_BLOCKS_DEACTIVATE[i]);
                        }
                    }
                }
                break;
            case DESTINATION_DEACTIVATE:
                block.setType(ACTIVATE_BLOCK);
                for (int i=0, max=DESTINATION_LOCATIONS.length; i<max; i++) {
                    for (int[] is: DESTINATION_LOCATIONS[i]) {
                        if (is.length == 3) {
                            block = world.getBlockAt(location.getBlockX()+is[0], location.getBlockY()+is[1], location.getBlockZ()+is[2]);
                            block.setType(DESTINATION_BLOCKS_ACTIVATE[i]);
                        }
                    }
                }
                break;
            case SIMPLE:
                block.setType(Material.AIR);
                for (int[] is: SIMPLE_STEP1_LOCATION) {
                    if (is.length == 2) {
                        block = world.getBlockAt(location.getBlockX()+is[0], location.getBlockY(), location.getBlockZ()+is[1]);
                        block.setType(Material.AIR);
                    }
                }
                break;
        }
    }
    
    private static Point getRotationPoint(int x, int y, double degree) {
        Point p = null;
        double radian = Math.toRadians(degree);
        double a = x * Math.cos(radian) - y * Math.sin(radian);
        double b = x * Math.sin(radian) + y * Math.cos(radian);
        p = new Point((int)Math.round(a), (int)Math.round(b));
        return p;
    }
}