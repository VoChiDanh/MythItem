package net.danh.mythitem.Events;

import com.destroystokyo.paper.event.player.PlayerRecipeBookClickEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

public class RecipeBook implements Listener {

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onClick(@NotNull PlayerRecipeBookClickEvent e) {
        e.setCancelled(true);
    }
}
