package com.thomas.minigame.listeners;

import com.thomas.minigame.MinigamesPlugin;
import com.thomas.minigame.core.Arena;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerJoinListener implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        event.setJoinMessage(null);
        Player player = event.getPlayer();

        // Ensure player is in the correct minigame world
        if (!player.getWorld().getName().equals("world")) {
            return;
        }

        Arena arena = MinigamesPlugin.getInstance().getGameManager().getAvailableArena();
        if (arena != null) {
            arena.addPlayer(player);
        } else {
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        event.setQuitMessage(null);
        Player player = event.getPlayer();
        for (Arena arena : MinigamesPlugin.getInstance().getGameManager().getArenas()) {
            if (arena.getPlayers().stream().anyMatch(p -> p.getPlayer().equals(player))) {
                arena.removePlayer(player);
                break;
            }
        }
    }

}
