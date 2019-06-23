package mrq.plugin.pattern;

import java.io.File;

import mrq.plugin.minecraft.tool.m;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

public abstract class PluginListener extends JavaPlugin implements Listener {
    public static String PLUGIN_PATH = null;
    
    protected static PluginDescriptionFile pdf = null;
    protected static FileConfiguration fc = null;
    
    @Override
    public void onEnable() {
        pdf = getDescription();
        fc = getConfig();
        
        PLUGIN_PATH = String.format("plugins%s%s%s", File.separator, pdf.getName(), File.separator);
        
        getServer().getPluginManager().registerEvents(this, this);
        m.sg(String.format("[%s v%s] plugin is enabled!", pdf.getName(), pdf.getVersion()));
        
        super.onEnable();
    }
    @Override
    public void onDisable() {
        m.sg(String.format("[%s v%s] plugin is disabled!", pdf.getName(), pdf.getVersion()));
        
        super.onDisable();
    }
    
    public static PluginDescriptionFile getPluginDescription() {
        return pdf;
    }
}