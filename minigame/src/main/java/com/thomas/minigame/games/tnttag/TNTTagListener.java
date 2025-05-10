package com.thomas.minigame.games.tnttag;

import com.thomas.minigame.core.Arena;
import com.thomas.minigame.core.Game;
import com.thomas.minigame.core.GameManager;
import com.thomas.minigame.MinigamesPlugin;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

public class TNTTagListener implements Listener {

    @EventHandler
    public void onTag(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player) || !(event.getEntity() instanceof Player))
            return;

        Player damager = (Player) event.getDamager();
        Player victim = (Player) event.getEntity();

        GameManager manager = MinigamesPlugin.getInstance().getGameManager();

        for (Arena arena : manager.getArenas()) {
            if (!arena.isRunning()) {
                continue;
            }

            boolean damagerInArena = arena.getPlayers().stream()
                    .anyMatch(p -> p.getPlayer().equals(damager));

            if (damagerInArena) {
                Game game = arena.getGame();

                if (game instanceof TNTTagGame tagGame) {
                    tagGame.onPlayerHit(damager, victim);
                } else {
                }
                break;
            }
        }
    }

    @EventHandler
    public void onFallDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player && event.getCause() == EntityDamageEvent.DamageCause.FALL) {
            event.setCancelled(true);
        }
    }
}
