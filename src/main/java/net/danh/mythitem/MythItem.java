package net.danh.mythitem;

import net.danh.mythitem.Command.MythCMD;
import net.danh.mythitem.Item.GeneratorItem;
import net.danh.mythitem.Item.GeneratorType;
import net.danh.mythitem.Manager.File;
import net.xconfig.bukkit.model.SimpleConfigurationManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.ForkJoinPool;

public final class MythItem extends JavaPlugin {
    private static MythItem mythItem;

    public static MythItem getMythItem() {
        return mythItem;
    }

    @Override
    public void onLoad() {
        mythItem = this;
        SimpleConfigurationManager.register(mythItem);
        File.loadFiles();
    }

    @Override
    public void onEnable() {
        new MythCMD("mythitem");
        ForkJoinPool.commonPool().execute(() -> {
            GeneratorType.generator();
            if (File.getConfig().getBoolean("load_settings.load_all_mob_on_enable")) {
                GeneratorItem.generator(Bukkit.getConsoleSender());
            }
        });
    }

    @Override
    public void onDisable() {
        File.saveFiles();
    }


}
