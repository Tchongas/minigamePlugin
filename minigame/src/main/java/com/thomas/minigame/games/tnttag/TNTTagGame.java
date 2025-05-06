package com.thomas.minigame.games.tnttag;

import com.thomas.minigame.core.Arena;
import com.thomas.minigame.core.Game;
import com.thomas.minigame.util.Countdown;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.stream.Collectors;

public class TNTTagGame extends Game {

    private TNTTagPlayer taggedPlayer;
    private final List<TNTTagPlayer> activePlayers = new ArrayList<>();
    private int eliminationInterval = 30; // seconds

    public TNTTagGame(Arena arena) {
        super(arena);
        arena.sendMessage("TNT Tag game will start in 10 seconds!");
    }

    @Override
    public void start() {
        // Convert to TNTTagPlayers
        arena.getPlayers().forEach(p -> activePlayers.add(new TNTTagPlayer(p.getPlayer())));

        new Countdown(10,
                sec -> arena.sendMessage("Game starting in " + sec + "s..."),
                this::beginGameLoop).start();
    }

    private void beginGameLoop() {
        arena.sendMessage("Game started!");
        pickRandomTagged();

        new BukkitRunnable() {
            @Override
            public void run() {
                if (taggedPlayer != null && activePlayers.contains(taggedPlayer)) {
                    eliminateTaggedPlayer();
                    if (activePlayers.size() <= 1) {
                        endGame();
                        cancel();
                    } else {
                        new Countdown(10,
                                sec -> arena.sendMessage("New TNT in " + sec + "s..."),
                                TNTTagGame.this::pickRandomTagged).start();
                    }
                }
            }
        }.runTaskTimer(com.thomas.minigame.MinigamesPlugin.getInstance(), eliminationInterval * 20L,
                eliminationInterval * 20L);
    }

    private void pickRandomTagged() {
        if (taggedPlayer != null)
            taggedPlayer.setTagged(false);

        List<TNTTagPlayer> choices = new ArrayList<>(activePlayers);
        if (choices.isEmpty())
            return;

        taggedPlayer = choices.get(new Random().nextInt(choices.size()));
        taggedPlayer.setTagged(true);
        arena.sendMessage(taggedPlayer.getPlayer().getName() + " is now tagged!");
    }

    public void onPlayerHit(Player damager, Player victim) {
        if (taggedPlayer == null || !damager.equals(taggedPlayer.getPlayer()))
            return;

        TNTTagPlayer newTagged = activePlayers.stream()
                .filter(p -> p.getPlayer().equals(victim))
                .findFirst().orElse(null);

        if (newTagged != null) {
            taggedPlayer.setTagged(false);
            taggedPlayer = newTagged;
            taggedPlayer.setTagged(true);
            arena.sendMessage(taggedPlayer.getPlayer().getName() + " is now tagged!");
        }
    }

    private void eliminateTaggedPlayer() {
        if (taggedPlayer == null)
            return;

        taggedPlayer.getPlayer().sendMessage("You were eliminated!");
        taggedPlayer.getPlayer().getInventory().clear();
        arena.sendMessage(taggedPlayer.getPlayer().getName() + " has been eliminated!");
        activePlayers.remove(taggedPlayer);
        taggedPlayer.getPlayer().teleport(Bukkit.getWorld("world").getSpawnLocation());
        // You can also use Velocity messaging here to send to hub
        taggedPlayer = null;
    }

    private void endGame() {
        arena.sendMessage("Game over!");
        if (!activePlayers.isEmpty()) {
            Player winner = activePlayers.get(0).getPlayer();
            arena.sendMessage("Winner: " + winner.getName());
        }

        activePlayers.forEach(p -> {
            p.getPlayer().teleport(Bukkit.getWorld("world").getSpawnLocation());
            // You can call Velocity message here
        });

        arena.endGame();
    }
}
