package mrq.plugin.pattern;

import org.bukkit.plugin.Plugin;

public class AbstractAction implements Runnable {
    private Plugin plugin = null;
    
    public AbstractAction(Plugin plugin) {
        this.plugin = plugin;
    }
    @Override
    public void run() {
        
    }
    
    public Plugin getPlugin() {
        return plugin;
    }
}