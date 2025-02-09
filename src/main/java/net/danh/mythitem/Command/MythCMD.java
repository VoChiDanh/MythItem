package net.danh.mythitem.Command;

import net.Indyuce.mmoitems.MMOItems;
import net.danh.mythitem.Item.GeneratorItem;
import net.danh.mythitem.Manager.File;
import net.danh.mythitem.Utils.Chat;
import org.bukkit.command.CommandSender;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ForkJoinPool;

public class MythCMD extends CMDBase {
    public MythCMD(String name) {
        super(name);
    }

    @Override
    public void execute(CommandSender c, String[] args) {
        if (c.hasPermission("mythitem.admin")) {
            if (args.length == 1) {
                if (args[0].equalsIgnoreCase("help")) {
                    ForkJoinPool.commonPool().execute(() -> File.getConfig().getStringList("message.help.admin").forEach(s -> c.sendMessage(Chat.colorize(s))));
                }
                if (args[0].equalsIgnoreCase("reload")) {
                    ForkJoinPool.commonPool().execute(() -> {
                        File.reloadFiles();
                        c.sendMessage(Chat.colorize(File.getConfig().getString("message.reload")));
                    });
                }
                if (args[0].equalsIgnoreCase("create")) {
                    ForkJoinPool.commonPool().execute(() -> {
                        GeneratorItem.generator(c);
                        MMOItems.plugin.getTypes().reload(true);
                        MMOItems.plugin.getTemplates().reload();
                    });
                }
            }
        }
    }

    @Override
    public List<String> TabComplete(CommandSender sender, String[] args) {
        List<String> completions = new ArrayList<>();
        List<String> commands = new ArrayList<>();
        if (sender.hasPermission("mythitem.admin")) {
            if (args.length == 1) {
                commands.add("help");
                commands.add("reload");
                commands.add("create");
                StringUtil.copyPartialMatches(args[0], commands, completions);
            }
        }
        Collections.sort(completions);
        return completions;
    }
}
