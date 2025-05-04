package com.thomas.minigame;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.ChatColor;

import java.util.*;

/**
 * GameManager handles player joins, auto-starts games,
 * assigns players to two arenas, and manages game flow.
 */
public class GameManager implements Listener {

    private final MinigamePlugin plugin;
    // private final SQLManager sqlManager;

    // List to hold players waiting to play
    private final List<Player> waitingPlayers = new ArrayList<>();

    // Map to track which player belongs to which arena
    private final Map<UUID, Integer> playerArenaMap = new HashMap<>();

    // Arena positions in the same world (update coords as needed)
    private final Location arena1Spawn = new Location(Bukkit.getWorld("world"), 100, 64, 100);
    private final Location arena2Spawn = new Location(Bukkit.getWorld("world"), 200, 64, 100);

    // public GameManager(MinigamePlugin plugin, SQLManager sqlManager)
    public GameManager(MinigamePlugin plugin) {
        this.plugin = plugin;
        // this.sqlManager = sqlManager;
    }

    // Called when a player joins the server
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        waitingPlayers.add(player);

        // Schedule a check to possibly start the game after 5 seconds (100 ticks)
        new BukkitRunnable() {
            @Override
            public void run() {
                tryStartGame();
            }
        }.runTaskLater(plugin, 100L);
    }

    // Check if we have enough players to start the game
    private static final int ARENA_SIZE = 6;

    private void tryStartGame() {
        if (waitingPlayers.size() < 2)
            return; // Minimum 2 players required

        // Prepare a copy of the waiting list to avoid modifying during iteration
        List<Player> playersToAssign = new ArrayList<>(waitingPlayers);
        waitingPlayers.clear(); // Clear the waiting list

        int arenaId = 1;
        while (!playersToAssign.isEmpty()) {
            // Get up to ARENA_SIZE players for this arena
            List<Player> arenaPlayers = new ArrayList<>();
            for (int i = 0; i < ARENA_SIZE && !playersToAssign.isEmpty(); i++) {
                arenaPlayers.add(playersToAssign.remove(0));
            }

            // Determine the spawn location for this arena
            Location spawn = getArenaSpawn(arenaId);

            // Teleport players and assign to arena
            for (Player p : arenaPlayers) {
                p.teleport(spawn);
                playerArenaMap.put(p.getUniqueId(), arenaId);
            }

            // Start the game for this group
            startGame(arenaPlayers, arenaId);
            arenaId++;
        }
    }

    private Location getArenaSpawn(int arenaId) {
        int x = 100 + ((arenaId - 1) * 100); // Arena 1 at 100, 2 at 200, etc.
        return new Location(Bukkit.getWorld("world"), x, 64, 100);
    }

    private void startGame(List<Player> players, int arenaId) {
        // This runs the game logic for a single arena
        new BukkitRunnable() {
            @Override
            public void run() {
                // Simulate a winner (first player in list for now)
                Player winner = players.get(0);

                // Send message to all players in this arena
                for (Player p : players) {
                    p.sendMessage(
                            ChatColor.GREEN + "[Arena " + arenaId + "] " + winner.getName() + " Ganhou!");
                }

                // Save winner to MySQL
                // sqlManager.saveWinner(winner, arenaId);

                // Add short delay before sending players back
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        for (Player p : players) {
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "/server hub");
                            playerArenaMap.remove(p.getUniqueId());
                        }
                    }
                }.runTaskLater(plugin, 60L); // Delay return by 3 seconds (60 ticks)

            }
        }.runTaskLater(plugin, 600L); // Run game after 30 seconds
    }

}
