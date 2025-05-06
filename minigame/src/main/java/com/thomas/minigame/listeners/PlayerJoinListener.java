package com.thomas.minigame.listeners;

import com.thomas.minigame.MinigamesPlugin;
import com.thomas.minigame.core.Arena;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Arena arena = MinigamesPlugin.getInstance().getGameManager().getAvailableArena();
        if (arena != null) {
            arena.addPlayer(event.getPlayer());
        } else {
            event.getPlayer().sendMessage("No available arenas! Try again later.");
        }
    }
}
