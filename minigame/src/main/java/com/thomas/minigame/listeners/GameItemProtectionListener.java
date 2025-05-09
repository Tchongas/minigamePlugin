package com.thomas.minigame.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import org.bukkit.event.Listener;

import org.bukkit.event.inventory.InventoryClickEvent;

public class GameItemProtectionListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player))
            return;

        // Only handle clicks in the player's inventory (not chest menus, etc.)
        if (event.getClickedInventory() != player.getInventory())
            return;

        // Prevent interaction with slot X (e.g., slot 0 = hotbar slot 1)
        int blockedSlot = 39; // adjust to your desired slot
        if (event.getSlot() == blockedSlot) {
            event.setCancelled(true);
        }
    }

}