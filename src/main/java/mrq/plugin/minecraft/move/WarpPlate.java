package mrq.plugin.minecraft.move;

import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public class WarpPlate extends JavaPlugin implements Listener {
    @Override
    public void onEnable() {
        super.onEnable();
        // Register listener to plugin
        getServer().getPluginManager().registerEvents(this, this);
        // Create default configuration from config.yml and make data folder
        saveDefaultConfig();
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }
}
