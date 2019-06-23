package mrq.plugin.minecraft.move.i18n;

import mrq.plugin.minecraft.move.debug.l;
import org.apache.commons.lang.text.StrSubstitutor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;

public class Message {
    private static String pluginName;

    private static String dataFolder;
    private static String i18nFolder;

    public static void setPluginName(String pluginName) {
        Message.pluginName = pluginName;
    }

    public static void setDataFolder(File dataFolder) {
        Message.dataFolder = dataFolder.getPath();

        // Make i18n folder
        File i18nFolder = Paths.get(Message.dataFolder, "i18n").toFile();
        Message.i18nFolder = i18nFolder.getPath();
        if (i18nFolder.mkdirs()) {

        }
    }

    private static YamlConfiguration getLocaleMessages(String locale) {
        // Messages from <locale>.yml
        String fileName = String.join(".", locale, "yml");
        // Locale file from <plugin-name>/i18n/<locale>.yml
        Path configPath = Paths.get(i18nFolder, fileName);
        File configFile = configPath.toFile();
        if (!configFile.exists()) {
            // Resource file from /i18n/<locale>.yml
            String resourcePath = String.join("/", "", "i18n", fileName);
            InputStream inputStream = Message.class.getResourceAsStream(resourcePath);
            // Check resource file
            if (inputStream == null) {
                // No resource file
                return null;
            }
            try {
                // Copy locale file
                Files.copy(inputStream, configPath, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return YamlConfiguration.loadConfiguration(configFile);
    }

    public static void sendMessage(Player player, Messages messageKey) {
        sendMessage(player, messageKey, null);
    }
    public static void sendMessage(Player player, Messages messageKey, Map<String, Object> map) {
        // Load configuration from player locale
        YamlConfiguration yamlConfiguration = getLocaleMessages(player.getLocale());
        if (yamlConfiguration == null) {
            // Change to default locale
            yamlConfiguration = getLocaleMessages(String.valueOf(Messages.DEFAULT_LOCALE));
        }
        // Get template string
        String template = yamlConfiguration.getString(String.valueOf(messageKey), "");
        StrSubstitutor strSubstitutor = new StrSubstitutor(map);
        // Make message from template string
        String message = strSubstitutor.replace(template);
        player.sendMessage(String.format("[ %s ] %s", pluginName, message));
    }
}
