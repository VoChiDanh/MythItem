package net.danh.mythitem.Item;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.ConfigFile;
import net.danh.mythitem.Manager.File;

import java.util.Objects;

public class GeneratorType {
    public static void generator() {
        ConfigFile config = new ConfigFile("item-types");
        for (String type_name : Objects.requireNonNull(File.getConfig().getConfigurationSection("type")).getKeys(false)) {
            boolean enable = File.getConfig().getBoolean("type." + type_name + ".generator_custom_type.enable");
            if (enable) {
                if (config.getConfig().contains(type_name.toUpperCase() + ".display")) {
                    return;
                }
                config.getConfig().set(type_name.toUpperCase() + ".display", File.getConfig().getString("type." + type_name + ".generator_custom_type.display"));
                config.getConfig().set(type_name.toUpperCase() + ".name", File.getConfig().getString("type." + type_name + ".generator_custom_type.name"));
                config.getConfig().set(type_name.toUpperCase() + ".parent", File.getConfig().getString("type." + type_name + ".generator_custom_type.parent"));
                config.getConfig().set(type_name.toUpperCase() + ".unident-item.name", File.getConfig().getString("type." + type_name + ".generator_custom_type.unident-item.name"));
                config.getConfig().set(type_name.toUpperCase() + ".unident-item.lore", File.getConfig().getList("type." + type_name + ".generator_custom_type.unident-item.lore"));
                config.save();
                ConfigFile configFile = new ConfigFile("/item", type_name.toLowerCase());
                if (!configFile.exists()) {
                    configFile.setup();
                }
            }
        }
        MMOItems.plugin.getTypes().reload();
    }
}
