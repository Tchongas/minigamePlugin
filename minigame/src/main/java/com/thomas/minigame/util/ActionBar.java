package com.thomas.minigame.util;

import com.thomas.minigame.MinigamesPlugin; // Assuming this is your main plugin class
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask; // For storing the task

public class ActionBar {
    private static BukkitTask actionBarTask;

    public static void setPersistentActionBar(Player player, String message) {
        // Cancel existing task if it exists
        if (actionBarTask != null)
            actionBarTask.cancel();

        // Start task to send the message every second
        actionBarTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()) {
                    this.cancel();
                    return;
                }
                player.sendActionBar(message);
            }
        }.runTaskTimer(MinigamesPlugin.getInstance(), 0, 1);
    }
}
