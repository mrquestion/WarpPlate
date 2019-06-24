package mrq.plugin.minecraft.move;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Charsets;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.UUID;

public class Plate {
    public static int LENGTH = 5;
    public static int HEIGHT = 3;

    private int id;
    private String alias;
    private String hash;
    @JsonIgnore private Player player;
    private String playerName;
    @JsonIgnore private World world;
    private String worldName;
    private UUID worldUID;
    private int x, y, z;
    private Material[] pattern;
    @JsonIgnore private LocalDate localDate;
    @JsonIgnore private LocalTime localTime;
    private String timestamp;

    Plate() {}
    Plate(int id, Player player, Block block, Material[] pattern) {
        setId(id);
        setAlias("");
        setHash(pattern);
        setPlayer(player);
        setPlayerName(player.getName());
        setWorld(player.getWorld());
        setWorldName(getWorld().getName());
        setWorldUID(getWorld().getUID());
        setX(block.getX());
        setY(block.getY());
        setZ(block.getZ());
        setPattern(pattern);
        setLocalDate(LocalDate.now());
        setLocalTime(LocalTime.now());
        setTimestamp(String.format("%s %s", getLocalDate(), getLocalTime()));
    }

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }

    public String getAlias() {
        return alias;
    }
    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getHash() {
        return hash;
    }
    public void setHash(String hash) {
        this.hash = hash;
    }
    public void setHash(Material[] pattern) {
        setHash(getPatternHash(pattern));
    }
    public String getPatternHash(Material[] pattern) {
        String[] patternStrings = Arrays.stream(pattern).map(String::valueOf).toArray(String[]::new);
        String patternString = String.join(":", patternStrings);
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-512");
            messageDigest.reset();
            messageDigest.update(patternString.getBytes(Charsets.UTF_8));
            return String.format("%0128x", new BigInteger(1, messageDigest.digest()));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    @JsonIgnore public Player getPlayer() {
        return player;
    }
    public void setPlayer(Player player) {
        this.player = player;
    }
    public String getPlayerName() {
        return playerName;
    }
    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }
    @JsonIgnore public World getWorld() {
        return world;
    }
    public void setWorld(World world) {
        this.world = world;
    }
    public String getWorldName() {
        return worldName;
    }
    public void setWorldName(String worldName) {
        this.worldName = worldName;
    }
    public UUID getWorldUID() {
        return worldUID;
    }
    public void setWorldUID(UUID worldUID) {
        this.worldUID = worldUID;
    }

    public int getX() {
        return x;
    }
    public void setX(int x) {
        this.x = x;
    }
    public int getY() {
        return y;
    }
    public void setY(int y) {
        this.y = y;
    }
    public int getZ() {
        return z;
    }
    public void setZ(int z) {
        this.z = z;
    }

    public Material[] getPattern() {
        return pattern;
    }
    public void setPattern(Material[] pattern) {
        this.pattern = pattern;
    }

    @JsonIgnore public LocalDate getLocalDate() {
        if (localDate == null) {
            setLocalDate(LocalDate.parse(getTimestamp().split(" ")[0]));
        }
        return localDate;
    }
    public void setLocalDate(LocalDate localDate) {
        this.localDate = localDate;
    }

    @JsonIgnore public LocalTime getLocalTime() {
        if (localTime == null) {
            setLocalTime(LocalTime.parse(getTimestamp().split(" ")[0]));
        }
        return localTime;
    }
    public void setLocalTime(LocalTime localTime) {
        this.localTime = localTime;
    }

    public String getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public boolean equals(UUID worldUID, Material[] pattern) {
        return equals(worldUID) && equals(pattern);
    }
    public boolean equals(UUID worldUID) {
        return worldUID.equals(getWorldUID());
    }
    public boolean equals(Material[] pattern) {
        return getHash().equals(getPatternHash(pattern));
    }

    public boolean contains(World world, int x, int y, int z) {
        if (!world.getUID().equals(getWorldUID())) {
            return false;
        } else if (Math.abs(x - getX()) > LENGTH / 2) {
            return false;
        } else if (Math.abs(z - getZ()) > LENGTH / 2) {
            return false;
        } else if (y - getY() >= HEIGHT || y - getY() < 0) {
            return false;
        }
        return true;
    }
}