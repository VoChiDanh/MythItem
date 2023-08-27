package net.danh.mythitem.Manager;

import net.xconfig.bukkit.model.SimpleConfigurationManager;
import org.bukkit.configuration.file.FileConfiguration;

public class File {
    public static SimpleConfigurationManager getFileSetting() {
        return SimpleConfigurationManager.get();
    }

    public static FileConfiguration getConfig() {
        return getFileSetting().get("config.yml");
    }

    public static void loadFiles() {
        getFileSetting().build("", false, "config.yml");
    }

    public static void saveFiles() {
        getFileSetting().save("config.yml");
    }

    public static void reloadFiles() {
        getFileSetting().reload("config.yml");
    }

    public static boolean isDebug() {
        return getConfig().getBoolean("debug");
    }
}
