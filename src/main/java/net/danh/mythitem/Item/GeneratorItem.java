package net.danh.mythitem.Item;

import io.lumine.mythic.api.mobs.MythicMob;
import io.lumine.mythic.api.skills.placeholders.PlaceholderDouble;
import io.lumine.mythic.bukkit.MythicBukkit;
import net.Indyuce.mmoitems.api.ConfigFile;
import net.Indyuce.mmoitems.api.Type;
import net.danh.mythitem.Calculator.Calculator;
import net.danh.mythitem.Manager.File;
import net.danh.mythitem.Manager.Number;
import net.danh.mythitem.MythItem;
import net.danh.mythitem.Utils.Chat;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.logging.Level;

public class GeneratorItem {

    public static void generator(@NotNull CommandSender c) {
        c.sendMessage(Chat.colorize(File.getConfig().getString("message.starting_load")));
        List<String> list_mobs_unload = File.getConfig().getStringList("load_items.list_mobs_unload");
        List<String> mob_name = new ArrayList<>(MythicBukkit.inst().getMobManager().getMobNames());
        mob_name.forEach(name -> {
            if (list_mobs_unload.contains(name)) {
                return;
            }
            Optional<MythicMob> mob = MythicBukkit.inst().getMobManager().getMythicMob(name);
            if (mob.isPresent()) {
                MythicMob mythicMob = mob.get();
                PlaceholderDouble health_data = mythicMob.getHealth();
                PlaceholderDouble damage_data = mythicMob.getDamage();
                PlaceholderDouble armor_data = mythicMob.getArmor();
                double health = 0;
                double damage = 0;
                double armor = 0;
                if (health_data != null) {
                    health = health_data.get();
                    if (File.isDebug()) {
                        MythItem.getMythItem().getLogger().log(Level.INFO, "Health: " + health);
                    }
                }
                if (damage_data != null) {
                    damage = damage_data.get();
                    if (File.isDebug()) {
                        MythItem.getMythItem().getLogger().log(Level.INFO, "Damage: " + damage);
                    }
                }
                if (armor_data != null) {
                    armor = armor_data.get();
                    if (File.isDebug()) {
                        MythItem.getMythItem().getLogger().log(Level.INFO, "Armor: " + armor);
                    }
                }
                if (File.isDebug()) {
                    MythItem.getMythItem().getLogger().log(Level.INFO, "Name : " + name);
                    MythItem.getMythItem().getLogger().log(Level.INFO, "-------------------------");
                }
                for (String type_name : Objects.requireNonNull(File.getConfig().getConfigurationSection("type")).getKeys(false)) {
                    Type type = Type.get(type_name.toUpperCase());
                    if (type != null) {
                        String type_setting = File.getConfig().getString("type." + type_name + ".type_settings.option");
                        if (type_setting != null) {
                            if (type_setting.equalsIgnoreCase("random")) {
                                random(type, name, health, damage, armor);
                            }
                            if (type_setting.equalsIgnoreCase("list")) {
                                list(type, name, health, damage, armor);
                            }
                        }
                    }
                }
            }
        });
        c.sendMessage(Chat.colorize(File.getConfig().getString("message.loading_done")));
    }

    private static void list(Type type, String name, double health, double damage, double armor) {
        String type_name = type.getName().toLowerCase();
        Optional<MythicMob> mob = MythicBukkit.inst().getMobManager().getMythicMob(name);
        if (mob.isPresent()) {
            String random_level = File.getConfig().getString("type." + type_name + ".random_level");
            if (random_level == null) {
                random_level = "1-10";
            }
            for (int i = Number.getInteger(random_level.split("-")[0]); i <= Number.getInteger(random_level.split("-")[1]); i++) {
                List<String> list_suffix = File.getConfig().getStringList("type." + type_name + ".type_settings.suffix");
                int finalI = i;
                list_suffix.forEach(suffix -> {
                    if (type.getConfigFile().getConfig().contains("MM_" + name + "_" + finalI + "_" + suffix)) {
                        return;
                    }
                    MythicMob mythicMob = mob.get();
                    String random_pre = "DIAMOND";
                    ConfigFile config = type.getConfigFile();
                    if (!config.getConfig().contains("MM_" + name + "_" + finalI + "_" + suffix)) {
                        List<String> list_prefix = File.getConfig().getStringList("type." + type_name + ".type_settings.prefix");
                        Random rnd = new Random();
                        int pos_pre = rnd.nextInt(list_prefix.size());
                        random_pre = list_prefix.get(pos_pre);
                        String material = random_pre + "_" + suffix;
                        Material m1;
                        try {
                            m1 = Material.valueOf(material);
                        } catch (IllegalArgumentException ignored) {
                            try {
                                m1 = Material.valueOf(random_pre);
                            } catch (IllegalArgumentException ignored1) {
                                try {
                                    m1 = Material.valueOf(suffix);
                                } catch (IllegalArgumentException ignored2) {
                                    try {
                                        m1 = Material.DIAMOND_CHESTPLATE;
                                    } catch (IllegalArgumentException e) {
                                        throw new RuntimeException(e);
                                    }
                                }
                            }
                        }
                        config.getConfig().set("MM_" + name + "_" + finalI + "_" + suffix + ".base.material", m1.toString());
                    }
                    String d_name = File.getConfig().getString("type." + type_name + ".name");
                    if (d_name == null) {
                        d_name = "#mm_name# &7[&f#level#&7]";
                    }
                    if (mythicMob.getDisplayName() != null) {
                        String c_s = File.getConfig().getString("type." + type_name + ".custom_name." + suffix);
                        boolean prefix_to_suffix = File.getConfig().getBoolean("type." + type_name + ".type_settings.prefix_to_suffix");
                        if (prefix_to_suffix) {
                            config.getConfig().set("MM_" + name + "_" + finalI + "_" + suffix + ".base.name", d_name.replace("#mm_name#", mythicMob.getDisplayName().get() + " " + c_s).replace("#level#", String.valueOf(finalI)));
                        } else {
                            config.getConfig().set("MM_" + name + "_" + finalI + "_" + suffix + ".base.name", d_name.replace("#mm_name#", c_s + " " + mythicMob.getDisplayName().get()).replace("#level#", String.valueOf(finalI)));
                        }
                    } else {
                        String material = random_pre + "_" + suffix;
                        Material m1;
                        int step;
                        try {
                            m1 = Material.valueOf(material);
                            step = 1;
                        } catch (IllegalArgumentException ignored) {
                            try {
                                m1 = Material.valueOf(random_pre);
                                step = 2;
                            } catch (IllegalArgumentException ignored1) {
                                try {
                                    m1 = Material.valueOf(suffix);
                                    step = 3;
                                } catch (IllegalArgumentException ignored2) {
                                    try {
                                        m1 = Material.DIAMOND_CHESTPLATE;
                                        step = 4;
                                    } catch (IllegalArgumentException e) {
                                        throw new RuntimeException(e);
                                    }
                                }
                            }
                        }
                        if (!m1.isAir()) {
                            if (step == 1) {
                                String c_p = File.getConfig().getString("type." + type_name + ".custom_name." + random_pre);
                                String c_s = File.getConfig().getString("type." + type_name + ".custom_name." + suffix);
                                config.getConfig().set("MM_" + name + "_" + finalI + "_" + suffix + ".base.name", d_name.replace("#mm_name#", c_p + " " + c_s).replace("#level#", String.valueOf(finalI)));
                            }
                            if (step == 4) {
                                config.getConfig().set("MM_" + name + "_" + finalI + "_" + suffix + ".base.name", d_name.replace("#mm_name#", "Diamond Chestplate").replace("#level#", String.valueOf(finalI)));
                            }
                            if (step == 3) {
                                String c_s = File.getConfig().getString("type." + type_name + ".custom_name." + suffix);
                                if (c_s != null) {
                                    config.getConfig().set("MM_" + name + "_" + finalI + "_" + suffix + ".base.name", d_name.replace("#mm_name#", c_s).replace("#level#", String.valueOf(finalI)));
                                }
                            }
                            if (step == 2) {
                                String c_p = File.getConfig().getString("type." + type_name + ".custom_name." + random_pre);
                                if (c_p != null) {
                                    config.getConfig().set("MM_" + name + "_" + finalI + "_" + suffix + ".base.name", d_name.replace("#mm_name#", c_p).replace("#level#", String.valueOf(finalI)));
                                }
                            }
                        }
                    }
                    for (String stats : Objects.requireNonNull(File.getConfig().getConfigurationSection("type." + type_name + ".stats.string_boolean")).getKeys(false)) {
                        config.getConfig().set("MM_" + name + "_" + finalI + "_" + suffix + ".base." + stats, File.getConfig().getBoolean("type." + type_name + ".stats.string_boolean." + stats));
                    }
                    for (String stats : Objects.requireNonNull(File.getConfig().getConfigurationSection("type." + type_name + ".stats.number")).getKeys(false)) {
                        String un_c = File.getConfig().getString("type." + type_name + ".stats.number." + stats);
                        if (un_c != null) {
                            String p_un_c = un_c.replace("#mm_health#", String.valueOf(health)).replace("#mm_damage#", String.valueOf(damage)).replace("#mm_armor#", String.valueOf(armor)).replace("#level#", String.valueOf(finalI));
                            String c_un_c = Calculator.calculator(p_un_c, 0);
                            if ((int) Double.parseDouble(c_un_c) > 0) {
                                config.getConfig().set("MM_" + name + "_" + finalI + "_" + suffix + ".base." + stats, (int) Double.parseDouble(c_un_c));
                            }
                        }
                    }
                    config.save();
                    if (File.isDebug()) {
                        MythItem.getMythItem().getLogger().log(Level.WARNING, "Created item: " + "MM_" + name + "_" + finalI + "_" + suffix);
                    }
                });
            }
        }
    }

    private static void random(Type type, String name, double health, double damage, double armor) {
        String type_name = type.getName().toLowerCase();
        Optional<MythicMob> mob = MythicBukkit.inst().getMobManager().getMythicMob(name);
        if (mob.isPresent()) {
            MythicMob mythicMob = mob.get();
            String random_pre = "DIAMOND";
            String random_su = "SWORD";
            String random_level = File.getConfig().getString("type." + type_name + ".random_level");
            if (random_level == null) {
                random_level = "1-10";
            }
            for (int i = Number.getInteger(random_level.split("-")[0]); i <= Number.getInteger(random_level.split("-")[1]); i++) {
                if (type.getConfigFile().getConfig().contains("MM_" + name + "_" + i)) {
                    return;
                }
                ConfigFile config = type.getConfigFile();
                if (!config.getConfig().contains("MM_" + name + "_" + i)) {
                    List<String> list_prefix = File.getConfig().getStringList("type." + type_name + ".type_settings.prefix");
                    Random rnd = new Random();
                    int pos_pre = rnd.nextInt(list_prefix.size());
                    random_pre = list_prefix.get(pos_pre);
                    List<String> list_suffix = File.getConfig().getStringList("type." + type_name + ".type_settings.suffix");
                    int pos_su = rnd.nextInt(list_suffix.size());
                    random_su = list_suffix.get(pos_su);
                    String material = random_pre + "_" + random_su;
                    Material m1;
                    try {
                        m1 = Material.valueOf(material);
                    } catch (IllegalArgumentException ignored) {
                        try {
                            m1 = Material.valueOf(random_pre);
                        } catch (IllegalArgumentException ignored1) {
                            try {
                                m1 = Material.valueOf(random_su);
                            } catch (IllegalArgumentException ignored2) {
                                try {
                                    m1 = Material.DIAMOND_SWORD;
                                } catch (IllegalArgumentException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        }
                    }
                    config.getConfig().set("MM_" + name + "_" + i + ".base.material", m1.toString());
                }
                String d_name = File.getConfig().getString("type." + type_name + ".name");
                if (d_name == null) {
                    d_name = "#mm_name# &7[&f#level#&7]";
                }
                if (mythicMob.getDisplayName() != null) {
                    config.getConfig().set("MM_" + name + "_" + i + ".base.name", d_name.replace("#mm_name#", mythicMob.getDisplayName().get()).replace("#level#", String.valueOf(i)));
                } else {
                    String material = random_pre + "_" + random_su;
                    Material m1;
                    int step;
                    try {
                        m1 = Material.valueOf(material);
                        step = 1;
                    } catch (IllegalArgumentException ignored) {
                        try {
                            m1 = Material.valueOf(random_pre);
                            step = 2;
                        } catch (IllegalArgumentException ignored1) {
                            try {
                                m1 = Material.valueOf(random_su);
                                step = 3;
                            } catch (IllegalArgumentException ignored2) {
                                try {
                                    m1 = Material.DIAMOND_SWORD;
                                    step = 4;
                                } catch (IllegalArgumentException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        }
                    }
                    if (!m1.isAir()) {
                        if (step == 1) {
                            String c_p = File.getConfig().getString("type." + type_name + ".custom_name." + random_pre);
                            String c_s = File.getConfig().getString("type." + type_name + ".custom_name." + random_su);
                            config.getConfig().set("MM_" + name + "_" + i + ".base.name", d_name.replace("#mm_name#", c_p + " " + c_s).replace("#level#", String.valueOf(i)));
                        }
                        if (step == 4) {
                            config.getConfig().set("MM_" + name + "_" + i + ".base.name", d_name.replace("#mm_name#", "Diamond Sword").replace("#level#", String.valueOf(i)));
                        }
                        if (step == 3) {
                            String c_s = File.getConfig().getString("type." + type_name + ".custom_name." + random_su);
                            if (c_s != null) {
                                config.getConfig().set("MM_" + name + "_" + i + ".base.name", d_name.replace("#mm_name#", c_s).replace("#level#", String.valueOf(i)));
                            }
                        }
                        if (step == 2) {
                            String c_p = File.getConfig().getString("type." + type_name + ".custom_name." + random_pre);
                            if (c_p != null) {
                                config.getConfig().set("MM_" + name + "_" + i + ".base.name", d_name.replace("#mm_name#", c_p).replace("#level#", String.valueOf(i)));
                            }
                        }
                    }
                }
                for (String stats : Objects.requireNonNull(File.getConfig().getConfigurationSection("type." + type_name + ".stats.string_boolean")).getKeys(false)) {
                    config.getConfig().set("MM_" + name + "_" + i + ".base." + stats, File.getConfig().getBoolean("type." + type_name + ".stats.string_boolean." + stats));
                }
                for (String stats : Objects.requireNonNull(File.getConfig().getConfigurationSection("type." + type_name + ".stats.number")).getKeys(false)) {
                    String un_c = File.getConfig().getString("type." + type_name + ".stats.number." + stats);
                    if (un_c != null) {
                        String p_un_c = un_c.replace("#mm_health#", String.valueOf(health)).replace("#mm_damage#", String.valueOf(damage)).replace("#mm_armor#", String.valueOf(armor)).replace("#level#", String.valueOf(i));
                        String c_un_c = Calculator.calculator(p_un_c, 0);
                        if ((int) Double.parseDouble(c_un_c) > 0) {
                            config.getConfig().set("MM_" + name + "_" + i + ".base." + stats, (int) Double.parseDouble(c_un_c));
                        }
                    }
                }
                config.save();
                if (File.isDebug()) {
                    MythItem.getMythItem().getLogger().log(Level.WARNING, "Created item: " + "MM_" + name + "_" + i);
                }
            }
        }
    }
}
