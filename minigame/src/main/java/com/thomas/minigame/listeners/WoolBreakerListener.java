package com.thomas.minigame.listeners;

import com.thomas.minigame.MinigamesPlugin; // Assuming this is your main plugin class
import org.bukkit.Material;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.block.Block;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.entity.Player;
import org.bukkit.Effect;

public class WoolBreakerListener implements Listener {

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Block block = event.getBlockPlaced();
        Material type = block.getType();

        if (type == Material.WHITE_WOOL) {
            // Schedule to simulate block breaking after 5 seconds (100 ticks)
            new BukkitRunnable() {
                @Override
                public void run() {
                    // Check if it's still white wool (player might have changed it)
                    if (block.getType() == Material.WHITE_WOOL) {
                        // Send block-breaking animation to all players around the placed block
                        for (Player player : block.getWorld().getPlayers()) {
                            // Simulate block breaking animation here
                            // This requires sending a packet to the player
                        }
                        // Actually break the block
                        block.breakNaturally();
                    }
                }
            }.runTaskLater(MinigamesPlugin.getInstance(), 100L); // 100 ticks = 5 seconds
        }
    }
}