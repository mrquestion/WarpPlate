package mrq.plugin.minecraft.move;

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import mrq.plugin.minecraft.move.i18n.Message;
import mrq.plugin.minecraft.move.i18n.Messages;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.IntStream;

public class WarpPlate extends JavaPlugin implements Listener {
    @Override
    public void onEnable() {
        super.onEnable();
        // Register listener to plugin
        getServer().getPluginManager().registerEvents(this, this);
        // l.og(this.getName(), "enabled");
        // Create default configuration from config.yml and make data folder
        saveDefaultConfig();
        // l.og(getDataFolder().getPath(), "prepared");
        // Set plugin name to message
        Message.setPluginName(getName());
        // Set data folder to message
        Message.setDataFolder(getDataFolder());
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoinEvent(PlayerJoinEvent e) {
        // l.og("PlayerJoinEvent");
        Player player = e.getPlayer();

        Map<String, Object> map = new HashMap<>();
        List<Plate> plates = loadPlates();
        map.put("count", plates.size());
        map.put("plugin.name", getName());
        Message.sendMessage(player, Messages.INFORMATION_COUNT, map);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerInteractAtEntityEvent(PlayerInteractAtEntityEvent e) {
        // l.og("PlayerInteractAtEntityEvent");
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerInteractEntityEvent(PlayerInteractEntityEvent e) {
        // l.og("PlayerInteractEntityEvent");
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerInteractEvent(PlayerInteractEvent e) {
        // l.og("PlayerInteractEvent");
        Player player = e.getPlayer();
        World world = player.getWorld();
        Block block = e.getClickedBlock();
        if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (e.getHand() == EquipmentSlot.HAND) {
                Material[][] layers = getLayers(block);
                // Entrance Template
                if (isEntranceTemplate(layers)) {
                    // Get direction
                    BlockFace direction = getDirection(layers[1]);
                    // Set first layer to north
                    rotateLayerToNorth(layers[0], direction);

                    // Get pattern from first layer
                    Material[] pattern = getPattern(layers[0]);

                    // Find plate from pattern
                    Plate plate = getPlate(pattern);
                    // Plate unmatched
                    if (plate == null) {
                        Message.sendMessage(player, Messages.PLATE_NOT_FOUND);
                    }
                    // Plate matched
                    else {
                        Map<String, Object> map = new HashMap<>();
                        map.put("plate.x", plate.getX());
                        map.put("plate.y", plate.getY());
                        map.put("plate.z", plate.getZ());
                        Message.sendMessage(player, Messages.TELEPORT_COORDINATES, map);

                        // Get pitch and yaw from player
                        Location location = player.getLocation();
                        // Set world and coordinates from plate
                        location.setWorld(getServer().getWorld(plate.getWorldUID()));
                        location.setX(plate.getX() + .5);
                        location.setY(plate.getY() + 1);
                        location.setZ(plate.getZ() + .5);
                        // Teleport to exit
                        player.teleport(location);
                    }
                }
                // Exit Template
                else if (isExitTemplate(layers)) {
                    // Get direction
                    BlockFace direction = getDirection(layers[0]);
                    // Set first layer to north
                    rotateLayerToNorth(layers[0], direction);

                    // Get pattern from first layer
                    Material[] pattern = getPattern(layers[0]);

                    // Find plate from pattern
                    Plate plate = getPlate(pattern);
                    // Plate unmatched
                    if (plate == null) {
                        // Save plate to plate list
                        savePattern(player, block, pattern);

                        Map<String, Object> map = new HashMap<>();
                        map.put("plugin.name", getName());
                        Message.sendMessage(player, Messages.ADD_NEW_PLATE, map);

                        // Restruct layers to enable
                        restructLayers(block, true);
                    }
                    // Plate matched
                    else {
                        Map<String, Object> map = new HashMap<>();
                        map.put("plate.id", plate.getId());
                        Message.sendMessage(player, Messages.ADD_DUPLICATED, map);
                    }
                }
            }
        }
    }

    private Material[][] getLayers(Block block) {
        // Get layers from block
//        Material[][] layers = {
//                getLayer(block, 0),
//                getLayer(block, 1),
//                getLayer(block, 2),
//        };
//        Material[][] layers = new Material[3][];
        Material[][] layers = IntStream.range(0, Plate.HEIGHT)
                .mapToObj(dy -> getLayer(block, dy))
                .toArray(Material[][]::new);
        return layers;
    }
    private Material[] getLayer(Block block, int dy) {
        // Get layer from block with height
        int length = Plate.LENGTH;
        World world = block.getWorld();
        int x = block.getX();
        int y = block.getY();
        int z = block.getZ();
        Material[] layer = new Material[length * length];
        for (int dz = -(length / 2); dz <= length / 2; ++dz) {
            for (int dx = -(length / 2); dx <= length / 2; ++dx) {
                Block checkBlock = world.getBlockAt(x + dx, y + dy, z + dz);
                layer[length * (dz + 2) + (dx + 2)] = checkBlock.getBlockData().getMaterial();
            }
        }
        return layer;
    }

    private Material[] THIRD_LAYER = {
            Material.AIR, Material.AIR, Material.AIR, Material.AIR, Material.AIR,
            Material.AIR, Material.AIR, Material.AIR, Material.AIR, Material.AIR,
            Material.AIR, Material.AIR, Material.AIR, Material.AIR, Material.AIR,
            Material.AIR, Material.AIR, Material.AIR, Material.AIR, Material.AIR,
            Material.AIR, Material.AIR, Material.AIR, Material.AIR, Material.AIR,
    };

    // Entrance template
    private Material[][] ENTRANCE_TEMPLATE_LAYERS = {
            {
                    Material.AIR, Material.COBBLESTONE, Material.COBBLESTONE, Material.COBBLESTONE, Material.AIR,
                    Material.COBBLESTONE, null, null, null, Material.COBBLESTONE,
                    Material.COBBLESTONE, null, Material.COBBLESTONE, null, Material.COBBLESTONE,
                    Material.COBBLESTONE, null, null, null, Material.COBBLESTONE,
                    Material.AIR, Material.COBBLESTONE, Material.COBBLESTONE, Material.COBBLESTONE, Material.AIR,
            },
            {
                    Material.AIR, Material.AIR, null, Material.AIR, Material.AIR,
                    Material.AIR, Material.AIR, Material.AIR, Material.AIR, Material.AIR,
                    null, Material.AIR, Material.AIR, Material.AIR, null,
                    Material.AIR, Material.AIR, Material.AIR, Material.AIR, Material.AIR,
                    Material.AIR, Material.AIR, null, Material.AIR, Material.AIR,
            },
            THIRD_LAYER,
    };
    private boolean isEntranceTemplate(Material[][] checkLayers) {
        if (!isTemplate(checkLayers, ENTRANCE_TEMPLATE_LAYERS)) {
            return false;
        } else if (getDirection(checkLayers[1]) == null) {
            return false;
        }
        return true;
    }

    // Exit template
    private Material[][] EXIT_TEMPLATE_LAYERS = {
            {
                    Material.COBBLESTONE, Material.COBBLESTONE, null, Material.COBBLESTONE, Material.COBBLESTONE,
                    Material.COBBLESTONE, null, null, null, Material.COBBLESTONE,
                    null, null, Material.COBBLESTONE, null, null,
                    Material.COBBLESTONE, null, null, null, Material.COBBLESTONE,
                    Material.COBBLESTONE, Material.COBBLESTONE, null, Material.COBBLESTONE, Material.COBBLESTONE,
            },
            {
                    Material.TORCH, Material.AIR, Material.AIR, Material.AIR, Material.TORCH,
                    Material.AIR, Material.AIR, Material.AIR, Material.AIR, Material.AIR,
                    Material.AIR, Material.AIR, Material.AIR, Material.AIR, Material.AIR,
                    Material.AIR, Material.AIR, Material.AIR, Material.AIR, Material.AIR,
                    Material.TORCH, Material.AIR, Material.AIR, Material.AIR, Material.TORCH,
            },
            THIRD_LAYER,
    };
    private boolean isExitTemplate(Material[][] checkLayers) {
        if (!isTemplate(checkLayers, EXIT_TEMPLATE_LAYERS)) {
            return false;
        } else if (getDirection(checkLayers[0]) == null) {
            return false;
        }
        return true;
    }

    private boolean isTemplate(Material[][] checkLayers, Material[][] templateLayers) {
        // Check first layer
        if (!checkLayer(checkLayers[0], templateLayers[0])) {
            return false;
        }
        // Check second layer
        else if (!checkLayer(checkLayers[1], templateLayers[1])) {
            return false;
        }
        // Check third layer
        else if (!checkLayer(checkLayers[2], templateLayers[2])) {
            return false;
        }
        return true;
    }

    private boolean checkLayer(Material[] checkLayer, Material[] templateLayer) {
        for (int i = 0; i < checkLayer.length; ++i) {
            // Pass null for later
            if (templateLayer[i] == null) {
                continue;
            }
            // Check template
            if (checkLayer[i] != templateLayer[i]) {
                // Not matched
                return false;
            }
        }
        // All matched
        return true;
    }

    private BlockFace getDirection(Material[] checkLayer) {
        Map<Material, Integer> countMap = new HashMap<>();
        countMap.put(Material.TORCH, 0);
        countMap.put(Material.AIR, 0);
        int length = (int)Math.sqrt(checkLayer.length);
        int[] directionOffsets = {
                length * 0 + (length / 2),
                length * (length / 2) + 0, length * (length / 2) + (length - 1),
                length * (length - 1) + (length / 2),
        };
        BlockFace[] directions = {
                BlockFace.NORTH,
                BlockFace.WEST, BlockFace.EAST,
                BlockFace.SOUTH,
        };
        BlockFace direction = null;
        for (int i = 0; i < directionOffsets.length; ++i) {
            Material material = checkLayer[directionOffsets[i]];
            // Check direction template
            if (material != Material.TORCH && material != Material.AIR) {
                return null;
            }
            // Increase each count
            countMap.put(checkLayer[directionOffsets[i]], countMap.get(checkLayer[directionOffsets[i]]) + 1);
            // Check direction
            if (material == Material.TORCH) {
                // Save direction
                direction = directions[i];
            }
        }
        // Check direction template
        if (countMap.get(Material.TORCH) == 1 && countMap.get(Material.AIR) == 3) {
            // Template matched, return direction
            return direction;
        }
        // Template unmatched
        return null;
    }

    private Material[] getPattern(Material[] checkLayer) {
        int length = (int)Math.sqrt(checkLayer.length);
        Material[] pattern = new Material[(length - 2) * (length - 2) - 1];
        int offset = 0;
        for (int j = 1; j < length - 1; ++j) {
            for (int i = 1; i < length - 1; ++i) {
                // Check center
                if (!(j == length / 2 && i == length / 2)) {
                    // Add block to pattern
                    pattern[offset++] = checkLayer[length * j + i];
                }
            }
        }
        return pattern;
    }

    private void rotateLayer(Material[] layer) {
        int length = (int)Math.sqrt(layer.length);
        for (int z = 0; z < length / 2; ++z) {
            for (int x = z; x < length - (z + 1); ++x) {
                Material material = layer[z * length + x];
                layer[z * length + x] = layer[x * length + (length - (z + 1))];
                layer[x * length + (length - (z + 1))] = layer[(length - (z + 1)) * length + (length - (x + 1))];
                layer[(length - (z + 1)) * length + (length - (x + 1))] = layer[(length - (x + 1)) * length + z];
                layer[(length - (x + 1)) * length + z] = material;
            }
        }
    }

    private void rotateLayerToNorth(Material[] layer, BlockFace checkDirection) {
        BlockFace[] directions = {
                BlockFace.NORTH,
                BlockFace.EAST,
                BlockFace.SOUTH,
                BlockFace.WEST,
        };
        for (BlockFace direction: directions) {
            if (direction == checkDirection) {
                break;
            }
            rotateLayer(layer);
        }
    }

    private List<Plate> loadPlates() {
        // Load from plugins/WarpPlate/plates.json
        File path = Paths.get(getDataFolder().getPath(), "plates.json").toFile();
        ObjectMapper objectMapper = new ObjectMapper();
        // Check file exists
        if (path.exists() && path.length() > 0) {
            try {
                // Load saved plate objects
                return new ArrayList<>(Arrays.asList(objectMapper.readValue(path, Plate[].class)));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return new ArrayList<>();
    }

    private void savePlates(List<Plate> plates) {
        // Save to plugins/WarpPlate/plates.json
        File path = Paths.get(getDataFolder().getPath(), "plates.json").toFile();
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            // Save plate objects
            ObjectWriter objectWriter = objectMapper.writer(new DefaultPrettyPrinter());
            objectWriter.writeValue(path, plates);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void savePattern(Player player, Block block, Material[] pattern) {
        // Load plate objects or make default list
        List<Plate> plates = loadPlates();
        // Make plate object
        int maxId = 0;
        Optional<Plate> maxIdPlate = plates.stream().max(Comparator.comparing(Plate::getId));
        if (maxIdPlate.isPresent()) {
            maxId = maxIdPlate.get().getId();
        }
        Plate plate = new Plate(maxId + 1, player, block, pattern);
        // Add to list
        plates.add(plate);
        // Save plate list
        savePlates(plates);
    }

    private void removePlate(Plate removePlate) {
        List<Plate> plates = loadPlates();
        for (Plate plate: plates) {
            // Check pattern is same
            if (plate.equals(removePlate.getWorldUID(), removePlate.getPattern())) {
                // Check coordinates is same
                if (plate.getX() == removePlate.getX() && plate.getY() == removePlate.getY() && plate.getZ() == removePlate.getZ()) {
                    plates.remove(plate);
                    savePlates(plates);
                    break;
                }
            }
        }
    }

    private void restructLayers(Block block, boolean enable) {
        int length = Plate.LENGTH;
        int height = Plate.HEIGHT;
        World world = block.getWorld();
        int x = block.getX();
        int y = block.getY();
        int z = block.getZ();
        for (int dy = 0; dy < height; ++dy) {
            for (int dz = -(length / 2); dz <= length / 2; ++dz) {
                for (int dx = -(length / 2); dx <= length / 2; ++dx) {
                    if (EXIT_TEMPLATE_LAYERS[dy][length * (dz + 2) + (dx + 2)] == null) {
                        continue;
                    }
                    Block replaceBlock = world.getBlockAt(x + dx, y + dy, z + dz);
                    Material replaceMaterial = replaceBlock.getBlockData().getMaterial();
                    if (enable) {
                        enableExitTemplate(replaceBlock, replaceMaterial, dx, dy, dz);
                    } else {
                        disableExitTemplate(replaceBlock, replaceMaterial, dx, dy, dz);
                    }
                }
            }
        }

    }
    private void enableExitTemplate(Block block, Material material, int dx, int dy, int dz) {
        if (material == Material.COBBLESTONE) {
            if (dz == 0 && dx == 0) {
                block.setType(Material.MOSSY_COBBLESTONE);
            } else {
                block.setType(Material.OBSIDIAN);
            }
        } else if (material == Material.TORCH) {
            block.setType(Material.REDSTONE_TORCH);
        }
    }
    private void disableExitTemplate(Block block, Material material, int dx, int dy, int dz) {
        if (material == Material.OBSIDIAN) {
            block.setType(Material.COBBLESTONE);
        } else if (material == Material.MOSSY_COBBLESTONE) {
            block.setType(Material.COBBLESTONE);
        } else if (material == Material.REDSTONE_TORCH) {
            block.setType(Material.TORCH);
        }
    }

    private Plate getPlate(Material[] pattern) {
        // Load plate objects or make default list
        List<Plate> plates = loadPlates();
        for (Plate plate: plates) {
            if (plate.equals(pattern)) {
                return plate;
            }
        }
        return null;
    }


    @EventHandler(priority = EventPriority.NORMAL)
    public void onBlockBreakEvent(BlockBreakEvent e) {
        // l.og("BlockBreakEvent");
        Player player = e.getPlayer();
        Block block = e.getBlock();
        checkDestruction(player, block);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onBlockPlaceEvent(BlockPlaceEvent e) {
        // l.og("BlockPlaceEvent");
        Player player = e.getPlayer();
        Block block = e.getBlock();
        checkDestruction(player, block);
    }

    public void checkDestruction(Player destructPlayer, Block checkBlock) {
        World world = checkBlock.getWorld();
        int x = checkBlock.getX();
        int y = checkBlock.getY();
        int z = checkBlock.getZ();
        for (Plate plate: loadPlates()) {
            // Check broken block in layers
            if (plate.contains(world, x, y, z)) {
                // Restruct layer to disable
                Block restructBlock = world.getBlockAt(plate.getX(), plate.getY(), plate.getZ());
                restructLayers(restructBlock, false);
                // Remove plate from plate list
                removePlate(plate);

                for (Player player: world.getPlayers()) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("plugin.name", getName());
                    Message.sendMessage(player, Messages.SOME_PLATE_DESTROYED, map);
                    if (player.getName().equals(plate.getPlayerName())) {
                        map.put("destroyer.name", player.getName());
                        Message.sendMessage(player, Messages.YOUR_PLATE_DESTROYED, map);
                        if (player.getName().equals(destructPlayer.getName())) {
//                            destructPlayer.sendMessage(String.format("[ %s ] Oh, it was you...", getName()));
                        }
                    }
                }
            }
        }
    }

//    @EventHandler(priority = EventPriority.NORMAL)
//    public void onBlockPistonEvent(BlockPistonEvent e) {
//        // l.og("BlockPistonEvent");
//    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onBlockPistonExtendEvent(BlockPistonExtendEvent e) {
        // l.og("BlockPistonExtendEvent");
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onBlockPistonRetractEvent(BlockPistonRetractEvent e) {
        // l.og("BlockPistonRetractEvent");
    }
}
