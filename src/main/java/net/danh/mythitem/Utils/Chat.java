package net.danh.mythitem.Utils;

import net.danh.mythitem.Manager.File;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Chat {

    public static String colorize(String message) {
        return Color.colorize(File.getConfig().getString("prefix") + " " + message);
    }

    public static String colorize_wp(String message) {
        return Color.colorize(message);
    }

    public static List<String> colorize(String... message) {
        return Arrays.stream(message).map(Chat::colorize).collect(Collectors.toList());
    }

    public static List<String> colorize(List<String> message) {
        return message.stream().map(Chat::colorize).collect(Collectors.toList());
    }
}
