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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.logging.Level;

public class GeneratorItem {

    static HashMap<String, Double> stats_generate = new HashMap<>();

    public static void generator(@NotNull CommandSender c) {
        if (!stats_generate.isEmpty())
            stats_generate.clear();
        c.sendMessage(Chat.colorize(File.getConfig().getString("message.starting_load")));
        List<String> list_mobs_load = File.getConfig().getStringList("load_items.list_mobs_load");
        list_mobs_load.forEach(name -> {
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
                                if (File.isDebug()) MythItem.getMythItem().getLogger().log(Level.WARNING, "Random");
                                random(type, name, health, damage, armor);
                            }
                            if (type_setting.equalsIgnoreCase("list")) {
                                if (File.isDebug()) MythItem.getMythItem().getLogger().log(Level.WARNING, "List");
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
            HashMap<String, Double> min = new HashMap<>();
            HashMap<String, Double> max = new HashMap<>();
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
                            config.getConfig().set("MM_" + name + "_" + finalI + "_" + suffix + ".base.name", d_name.replace("#mm_name#", mythicMob.getDisplayName().get() + " " + c_s)
                                    .replace("<caster.level>", "").replace("#level#", String.valueOf(finalI)));
                        } else {
                            config.getConfig().set("MM_" + name + "_" + finalI + "_" + suffix + ".base.name", d_name.replace("#mm_name#", c_s + " " + mythicMob.getDisplayName().get())
                                    .replace("<caster.level>", "").replace("#level#", String.valueOf(finalI)));
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
                        if (File.isDebug()) MythItem.getMythItem().getLogger().log(Level.WARNING, stats);
                        String un_c_min = File.getConfig().getString("type." + type_name + ".stats.number." + stats + ".min");
                        String un_c_max = File.getConfig().getString("type." + type_name + ".stats.number." + stats + ".max");
                        String un_c_min_default = File.getConfig().getString("type." + type_name + ".stats.number." + stats + ".min_default");
                        String un_c_max_default = File.getConfig().getString("type." + type_name + ".stats.number." + stats + ".max_default");
                        if (File.isDebug())
                            MythItem.getMythItem().getLogger().log(Level.WARNING, "un_c_min: " + un_c_min);
                        if (File.isDebug())
                            MythItem.getMythItem().getLogger().log(Level.WARNING, "un_c_min_default: " + un_c_min_default);
                        if (File.isDebug())
                            MythItem.getMythItem().getLogger().log(Level.WARNING, "un_c_max: " + un_c_max);
                        if (File.isDebug())
                            MythItem.getMythItem().getLogger().log(Level.WARNING, "un_c_max_default: " + un_c_max_default);
                        if (un_c_min != null && un_c_max != null && un_c_min_default != null && un_c_max_default != null) {
                            String p_un_c_min_default = un_c_min_default.replace("#mm_health#", String.valueOf(health))
                                    .replace("#mm_damage#", String.valueOf(damage))
                                    .replace("#mm_armor#", String.valueOf(armor))
                                    .replace("#level#", String.valueOf(finalI));
                            String p_un_c_max_default = un_c_max_default.replace("#mm_health#", String.valueOf(health))
                                    .replace("#mm_damage#", String.valueOf(damage))
                                    .replace("#mm_armor#", String.valueOf(armor))
                                    .replace("#level#", String.valueOf(finalI));
                            String c_un_c_min_default = Calculator.calculator(p_un_c_min_default, 0);
                            String c_un_c_max_default = Calculator.calculator(p_un_c_max_default, 0);
                            double min_value_default;
                            double max_value_default;
                            boolean intStats = File.getConfig().getStringList("load_items.integer_stats").contains(stats);
                            if (intStats) {
                                min_value_default = BigDecimal.valueOf(Double.parseDouble(c_un_c_min_default)).setScale(0, RoundingMode.DOWN).doubleValue();
                                max_value_default = BigDecimal.valueOf(Double.parseDouble(c_un_c_max_default)).setScale(0, RoundingMode.UP).doubleValue();
                            } else {
                                min_value_default = BigDecimal.valueOf(Double.parseDouble(c_un_c_min_default)).setScale(1, RoundingMode.DOWN).doubleValue();
                                max_value_default = BigDecimal.valueOf(Double.parseDouble(c_un_c_max_default)).setScale(1, RoundingMode.UP).doubleValue();
                            }
                            if (File.isDebug())
                                MythItem.getMythItem().getLogger().log(Level.WARNING, "min_value_default: " + min_value_default);
                            if (File.isDebug())
                                MythItem.getMythItem().getLogger().log(Level.WARNING, "max_value_default: " + max_value_default);
                            if (File.isDebug())
                                MythItem.getMythItem().getLogger().log(Level.WARNING, "#stats_min_" + type_name.toLowerCase() + ";" + stats.toLowerCase() + "#: " + stats_generate.getOrDefault(type_name + "_" + stats + "_min_" + finalI, (double) 0));
                            if (File.isDebug())
                                MythItem.getMythItem().getLogger().log(Level.WARNING, "#stats_max_" + type_name.toLowerCase() + ";" + stats.toLowerCase() + "#: " + stats_generate.getOrDefault(type_name + "_" + stats + "_max_" + finalI, (double) 0));
                            String p_un_c_min = un_c_min.replace("#mm_health#", String.valueOf(health))
                                    .replace("#mm_damage#", String.valueOf(damage))
                                    .replace("#mm_armor#", String.valueOf(armor))
                                    .replace("#level#", String.valueOf(finalI))
                                    .replace("#min_before#", String.valueOf(min.getOrDefault(stats + "_" + (finalI - 1), min_value_default)))
                                    .replace("#max_before#", String.valueOf(max.getOrDefault(stats + "_" + (finalI - 1), max_value_default)));
                            for (String key : stats_generate.keySet()) {
                                String type_key = key.split("_")[0];
                                String stats_key = key.split("_")[1];
                                int level_key = Integer.parseInt(key.split("_")[3]);
                                if (level_key == finalI)
                                    p_un_c_min = p_un_c_min
                                            .replace("#stats_min_" + type_key + ";" + stats_key + "#", String.valueOf(stats_generate.getOrDefault(type_key + "_" + stats_key + "_min_" + finalI, (double) 0)))
                                            .replace("#stats_max_" + type_key + ";" + stats_key + "#", String.valueOf(stats_generate.getOrDefault(type_key + "_" + stats_key + "_max_" + finalI, (double) 0)));
                            }
                            if (File.isDebug())
                                MythItem.getMythItem().getLogger().log(Level.WARNING, "p_un_c_min: " + p_un_c_min);
                            String p_un_c_max = un_c_max.replace("#mm_health#", String.valueOf(health))
                                    .replace("#mm_damage#", String.valueOf(damage))
                                    .replace("#mm_armor#", String.valueOf(armor))
                                    .replace("#level#", String.valueOf(finalI))
                                    .replace("#min_before#", String.valueOf(min.getOrDefault(stats + "_" + (finalI - 1), min_value_default)))
                                    .replace("#max_before#", String.valueOf(max.getOrDefault(stats + "_" + (finalI - 1), max_value_default)));
                            for (String key : stats_generate.keySet()) {
                                String type_key = key.split("_")[0];
                                String stats_key = key.split("_")[1];
                                int level_key = Integer.parseInt(key.split("_")[3]);
                                if (level_key == finalI)
                                    p_un_c_max = p_un_c_max
                                            .replace("#stats_min_" + type_key + ";" + stats_key + "#", String.valueOf(stats_generate.getOrDefault(type_key + "_" + stats_key + "_min_" + finalI, (double) 0)))
                                            .replace("#stats_max_" + type_key + ";" + stats_key + "#", String.valueOf(stats_generate.getOrDefault(type_key + "_" + stats_key + "_max_" + finalI, (double) 0)));
                            }
                            if (File.isDebug())
                                MythItem.getMythItem().getLogger().log(Level.WARNING, "p_un_c_max: " + p_un_c_max);
                            String c_un_c_min = Calculator.calculator(p_un_c_min, 0);
                            String c_un_c_max = Calculator.calculator(p_un_c_max, 0);
                            if (File.isDebug())
                                MythItem.getMythItem().getLogger().log(Level.WARNING, "c_un_c_min: " + c_un_c_min);
                            if (File.isDebug())
                                MythItem.getMythItem().getLogger().log(Level.WARNING, "c_un_c_max: " + c_un_c_max);
                            double min_value;
                            double max_value;
                            if (intStats) {
                                min_value = BigDecimal.valueOf(Double.parseDouble(c_un_c_min)).setScale(0, RoundingMode.DOWN).doubleValue();
                                max_value = BigDecimal.valueOf(Double.parseDouble(c_un_c_max)).setScale(0, RoundingMode.UP).doubleValue();
                            } else {
                                min_value = BigDecimal.valueOf(Double.parseDouble(c_un_c_min)).setScale(1, RoundingMode.DOWN).doubleValue();
                                max_value = BigDecimal.valueOf(Double.parseDouble(c_un_c_max)).setScale(1, RoundingMode.UP).doubleValue();
                            }
                            if (File.isDebug())
                                MythItem.getMythItem().getLogger().log(Level.WARNING, "min_value: " + min_value);
                            if (File.isDebug())
                                MythItem.getMythItem().getLogger().log(Level.WARNING, "max_value: " + max_value);
                            if (Double.parseDouble(c_un_c_min) > 0 && Double.parseDouble(c_un_c_max) > 0) {
                                config.getConfig().set("MM_" + name + "_" + finalI + "_" + suffix + ".base." + stats + ".min", min_value);
                                config.getConfig().set("MM_" + name + "_" + finalI + "_" + suffix + ".base." + stats + ".max", max_value);
                                min.put(stats + "_" + finalI, min_value);
                                max.put(stats + "_" + finalI, max_value);
                                stats_generate.put(type_name + "_" + stats + "_min_" + finalI, min_value);
                                stats_generate.put(type_name + "_" + stats + "_max_" + finalI, max_value);
                                if (File.isDebug())
                                    MythItem.getMythItem().getLogger().log(Level.WARNING, "MM_" + name + "_" + finalI + "_" + suffix + ".base." + stats + ".min: " + min_value);
                                if (File.isDebug())
                                    MythItem.getMythItem().getLogger().log(Level.WARNING, "MM_" + name + "_" + finalI + "_" + suffix + ".base." + stats + ".max: " + max_value);
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
            HashMap<String, Double> min = new HashMap<>();
            HashMap<String, Double> max = new HashMap<>();
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
                            config.getConfig().set("MM_" + name + "_" + i + ".base.name", d_name.replace("#mm_name#", c_p + " " + c_s)
                                    .replace("<caster.level>", "").replace("#level#", String.valueOf(i)));
                        }
                        if (step == 4) {
                            config.getConfig().set("MM_" + name + "_" + i + ".base.name", d_name.replace("#mm_name#", "Diamond Sword")
                                    .replace("<caster.level>", "").replace("#level#", String.valueOf(i)));
                        }
                        if (step == 3) {
                            String c_s = File.getConfig().getString("type." + type_name + ".custom_name." + random_su);
                            if (c_s != null) {
                                config.getConfig().set("MM_" + name + "_" + i + ".base.name", d_name.replace("#mm_name#", c_s)
                                        .replace("<caster.level>", "").replace("#level#", String.valueOf(i)));
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
                    if (File.isDebug()) MythItem.getMythItem().getLogger().log(Level.WARNING, stats);
                    String un_c_min = File.getConfig().getString("type." + type_name + ".stats.number." + stats + ".min");
                    String un_c_max = File.getConfig().getString("type." + type_name + ".stats.number." + stats + ".max");
                    String un_c_min_default = File.getConfig().getString("type." + type_name + ".stats.number." + stats + ".min_default");
                    String un_c_max_default = File.getConfig().getString("type." + type_name + ".stats.number." + stats + ".max_default");
                    if (File.isDebug()) MythItem.getMythItem().getLogger().log(Level.WARNING, "un_c_min: " + un_c_min);
                    if (File.isDebug())
                        MythItem.getMythItem().getLogger().log(Level.WARNING, "un_c_min_default: " + un_c_min_default);
                    if (File.isDebug()) MythItem.getMythItem().getLogger().log(Level.WARNING, "un_c_max: " + un_c_max);
                    if (File.isDebug())
                        MythItem.getMythItem().getLogger().log(Level.WARNING, "un_c_max_default: " + un_c_max_default);
                    if (un_c_min != null && un_c_max != null && un_c_min_default != null && un_c_max_default != null) {
                        String p_un_c_min_default = un_c_min_default.replace("#mm_health#", String.valueOf(health))
                                .replace("#mm_damage#", String.valueOf(damage))
                                .replace("#mm_armor#", String.valueOf(armor))
                                .replace("#level#", String.valueOf(i));
                        String p_un_c_max_default = un_c_max_default.replace("#mm_health#", String.valueOf(health))
                                .replace("#mm_damage#", String.valueOf(damage))
                                .replace("#mm_armor#", String.valueOf(armor))
                                .replace("#level#", String.valueOf(i));
                        String c_un_c_min_default = Calculator.calculator(p_un_c_min_default, 0);
                        String c_un_c_max_default = Calculator.calculator(p_un_c_max_default, 0);
                        double min_value_default;
                        double max_value_default;
                        boolean intStats = File.getConfig().getStringList("load_items.integer_stats").contains(stats);
                        if (intStats) {
                            min_value_default = BigDecimal.valueOf(Double.parseDouble(c_un_c_min_default)).setScale(0, RoundingMode.DOWN).doubleValue();
                            max_value_default = BigDecimal.valueOf(Double.parseDouble(c_un_c_max_default)).setScale(0, RoundingMode.UP).doubleValue();
                        } else {
                            min_value_default = BigDecimal.valueOf(Double.parseDouble(c_un_c_min_default)).setScale(1, RoundingMode.DOWN).doubleValue();
                            max_value_default = BigDecimal.valueOf(Double.parseDouble(c_un_c_max_default)).setScale(1, RoundingMode.UP).doubleValue();
                        }
                        if (File.isDebug())
                            MythItem.getMythItem().getLogger().log(Level.WARNING, "min_value_default: " + min_value_default);
                        if (File.isDebug())
                            MythItem.getMythItem().getLogger().log(Level.WARNING, "max_value_default: " + max_value_default);
                        if (File.isDebug())
                            MythItem.getMythItem().getLogger().log(Level.WARNING, "#stats_min_" + type_name.toLowerCase() + ";" + stats.toLowerCase() + "#: " + stats_generate.getOrDefault(type_name + "_" + stats + "_min_" + i, (double) 0));
                        if (File.isDebug())
                            MythItem.getMythItem().getLogger().log(Level.WARNING, "#stats_max_" + type_name.toLowerCase() + ";" + stats.toLowerCase() + "#: " + stats_generate.getOrDefault(type_name + "_" + stats + "_max_" + i, (double) 0));
                        String p_un_c_min = un_c_min.replace("#mm_health#", String.valueOf(health))
                                .replace("#mm_damage#", String.valueOf(damage))
                                .replace("#mm_armor#", String.valueOf(armor))
                                .replace("#level#", String.valueOf(i))
                                .replace("#min_before#", String.valueOf(min.getOrDefault(stats + "_" + (i - 1), min_value_default)))
                                .replace("#max_before#", String.valueOf(max.getOrDefault(stats + "_" + (i - 1), max_value_default)));
                        for (String key : stats_generate.keySet()) {
                            String type_key = key.split("_")[0];
                            String stats_key = key.split("_")[1];
                            int level_key = Integer.parseInt(key.split("_")[3]);
                            if (level_key == i)
                                p_un_c_min = p_un_c_min
                                        .replace("#stats_min_" + type_key + ";" + stats_key + "#", String.valueOf(stats_generate.getOrDefault(type_key + "_" + stats_key + "_min_" + i, (double) 0)))
                                        .replace("#stats_max_" + type_key + ";" + stats_key + "#", String.valueOf(stats_generate.getOrDefault(type_key + "_" + stats_key + "_max_" + i, (double) 0)));
                        }
                        if (File.isDebug())
                            MythItem.getMythItem().getLogger().log(Level.WARNING, "p_un_c_min: " + p_un_c_min);
                        String p_un_c_max = un_c_max.replace("#mm_health#", String.valueOf(health))
                                .replace("#mm_damage#", String.valueOf(damage))
                                .replace("#mm_armor#", String.valueOf(armor))
                                .replace("#level#", String.valueOf(i))
                                .replace("#min_before#", String.valueOf(min.getOrDefault(stats + "_" + (i - 1), min_value_default)))
                                .replace("#max_before#", String.valueOf(max.getOrDefault(stats + "_" + (i - 1), max_value_default)));
                        for (String key : stats_generate.keySet()) {
                            String type_key = key.split("_")[0];
                            String stats_key = key.split("_")[1];
                            int level_key = Integer.parseInt(key.split("_")[3]);
                            if (level_key == i)
                                p_un_c_max = p_un_c_max
                                        .replace("#stats_min_" + type_key + ";" + stats_key + "#", String.valueOf(stats_generate.getOrDefault(type_key + "_" + stats_key + "_min_" + i, (double) 0)))
                                        .replace("#stats_max_" + type_key + ";" + stats_key + "#", String.valueOf(stats_generate.getOrDefault(type_key + "_" + stats_key + "_max_" + i, (double) 0)));
                        }
                        if (File.isDebug())
                            MythItem.getMythItem().getLogger().log(Level.WARNING, "p_un_c_max: " + p_un_c_max);
                        String c_un_c_min = Calculator.calculator(p_un_c_min, 0);
                        String c_un_c_max = Calculator.calculator(p_un_c_max, 0);
                        if (File.isDebug())
                            MythItem.getMythItem().getLogger().log(Level.WARNING, "c_un_c_min: " + c_un_c_min);
                        if (File.isDebug())
                            MythItem.getMythItem().getLogger().log(Level.WARNING, "c_un_c_max: " + c_un_c_max);
                        double min_value;
                        double max_value;
                        if (intStats) {
                            min_value = BigDecimal.valueOf(Double.parseDouble(c_un_c_min)).setScale(0, RoundingMode.DOWN).doubleValue();
                            max_value = BigDecimal.valueOf(Double.parseDouble(c_un_c_max)).setScale(0, RoundingMode.UP).doubleValue();
                        } else {
                            min_value = BigDecimal.valueOf(Double.parseDouble(c_un_c_min)).setScale(1, RoundingMode.DOWN).doubleValue();
                            max_value = BigDecimal.valueOf(Double.parseDouble(c_un_c_max)).setScale(1, RoundingMode.UP).doubleValue();
                        }
                        if (File.isDebug())
                            MythItem.getMythItem().getLogger().log(Level.WARNING, "min_value: " + min_value);
                        if (File.isDebug())
                            MythItem.getMythItem().getLogger().log(Level.WARNING, "max_value: " + max_value);
                        if (Double.parseDouble(c_un_c_min) > 0 && Double.parseDouble(c_un_c_max) > 0) {
                            config.getConfig().set("MM_" + name + "_" + i + ".base." + stats + ".min", min_value);
                            config.getConfig().set("MM_" + name + "_" + i + ".base." + stats + ".max", max_value);
                            min.put(stats + "_" + i, min_value);
                            max.put(stats + "_" + i, max_value);
                            stats_generate.put(type_name + "_" + stats + "_min_" + i, min_value);
                            stats_generate.put(type_name + "_" + stats + "_max_" + i, max_value);
                            if (File.isDebug())
                                MythItem.getMythItem().getLogger().log(Level.WARNING, "MM_" + name + "_" + i + ".base." + stats + ".min: " + min_value);
                            if (File.isDebug())
                                MythItem.getMythItem().getLogger().log(Level.WARNING, "MM_" + name + "_" + i + ".base." + stats + ".max: " + max_value);
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
