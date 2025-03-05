package net.danh.mythitem.Item;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.ConfigFile;
import net.danh.mythitem.Manager.File;
import net.danh.mythitem.MythItem;

import java.util.Objects;
import java.util.logging.Level;

public class GeneratorType {
    public static void generator() {
        ConfigFile config = new ConfigFile("item-types");
        for (String type_name : Objects.requireNonNull(File.getConfig().getConfigurationSection("type")).getKeys(false)) {
            MythItem.getMythItem().getLogger().log(Level.INFO, "Loading type " + type_name);
            boolean enable = File.getConfig().getBoolean("type." + type_name + ".generator_custom_type.enable");
            MythItem.getMythItem().getLogger().log(Level.INFO, "Type enable: " + enable);
            if (enable) {
                if (config.getConfig().contains(type_name.toUpperCase() + ".display")) {
                    MythItem.getMythItem().getLogger().log(Level.INFO, "Type display loaded: " + config.getConfig().contains(type_name.toUpperCase() + ".display"));
                    continue;
                }
                config.getConfig().set(type_name.toUpperCase() + ".display", File.getConfig().getString("type." + type_name + ".generator_custom_type.display"));
                config.getConfig().set(type_name.toUpperCase() + ".name", File.getConfig().getString("type." + type_name + ".generator_custom_type.name"));
                config.getConfig().set(type_name.toUpperCase() + ".parent", File.getConfig().getString("type." + type_name + ".generator_custom_type.parent"));
                config.getConfig().set(type_name.toUpperCase() + ".unident-item.name", File.getConfig().getString("type." + type_name + ".generator_custom_type.unident-item.name"));
                config.getConfig().set(type_name.toUpperCase() + ".unident-item.lore", File.getConfig().getList("type." + type_name + ".generator_custom_type.unident-item.lore"));

                MythItem.getMythItem().getLogger().log(Level.INFO, "Type display: " + File.getConfig().getString("type." + type_name + ".generator_custom_type.display"));
                MythItem.getMythItem().getLogger().log(Level.INFO, "Type name: " + File.getConfig().getString("type." + type_name + ".generator_custom_type.name"));
                MythItem.getMythItem().getLogger().log(Level.INFO, "Type parent: " + File.getConfig().getString("type." + type_name + ".generator_custom_type.parent"));
                MythItem.getMythItem().getLogger().log(Level.INFO, "Type unident-item name: " + File.getConfig().getString("type." + type_name + ".generator_custom_type.unident-item.name"));
                MythItem.getMythItem().getLogger().log(Level.INFO, "Type unident-item lore: " + File.getConfig().getList("type." + type_name + ".generator_custom_type.unident-item.lore"));
                config.save();
                ConfigFile configFile = new ConfigFile("/item/", type_name.toLowerCase());
                MythItem.getMythItem().getLogger().log(Level.INFO, "Type file loaded: " + configFile.exists());
                if (!configFile.exists()) {
                    configFile.setup();
                }
            }
        }
        MMOItems.plugin.getTypes().reload(true);
    }
}
