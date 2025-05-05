package com.thomas.minigame;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.ChatColor;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.*;

public class GameManager implements Listener {

    private final MinigamePlugin plugin;
    // private final SQLManager sqlManager;

    private final List<Player> waitingPlayers = new ArrayList<>();
    private final Map<UUID, Integer> playerArenaMap = new HashMap<>();

    private final int ARENA_SIZE = 6;

    public GameManager(MinigamePlugin plugin) {
        this.plugin = plugin;
        // this.sqlManager = sqlManager;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        event.setJoinMessage(null);
        Player player = event.getPlayer();

        Location spawn = new Location(Bukkit.getWorld("world"), 5, -60, -5); // Replace with your desired spawn
        player.teleport(spawn);

        player.getInventory().clear();

        waitingPlayers.add(player);
        player.sendMessage(ChatColor.GREEN + "Voce foi adicionado a fila!");

        Bukkit.broadcastMessage(ChatColor.AQUA + player.getName() + " entrou! ("
                + waitingPlayers.size() + "/8 Players esperando...)");

        new BukkitRunnable() {
            @Override
            public void run() {
                tryStartGame();
            }
        }.runTaskLater(plugin, 80L); // 5 seconds delay
    }

    private void tryStartGame() {
        if (waitingPlayers.size() < 2)
            return;

        List<Player> playersToAssign = new ArrayList<>(waitingPlayers);
        waitingPlayers.clear();

        int arenaId = 1;
        while (!playersToAssign.isEmpty()) {
            List<Player> arenaPlayers = new ArrayList<>();
            for (int i = 0; i < ARENA_SIZE && !playersToAssign.isEmpty(); i++) {
                arenaPlayers.add(playersToAssign.remove(0));
            }

            Location spawn = getArenaSpawn(arenaId);
            final int currentArenaId = arenaId; // Make arenaId final for inner class use

            // Send countdown title
            for (Player p : arenaPlayers) {
                p.sendTitle(ChatColor.GOLD + "Ja vai começar!", "Se prepare!", 10, 40, 10);
                p.sendMessage(ChatColor.AQUA + "Prepare-se...");
            }

            new BukkitRunnable() {
                @Override
                public void run() {
                    for (Player p : arenaPlayers) {
                        p.teleport(spawn);
                        playerArenaMap.put(p.getUniqueId(), currentArenaId);
                        p.sendMessage(ChatColor.GREEN + "Jogo começou!");
                    }

                    startGame(arenaPlayers, currentArenaId);
                }
            }.runTaskLater(plugin, 120L);

            arenaId++; // Safe to increment now
        }
    }

    private Location getArenaSpawn(int arenaId) {
        int y = -40 + (arenaId - 1) * (35 + 5); // y = -40, -0, +40, etc.
        return new Location(Bukkit.getWorld("world"), 0, y, 0);
    }

    private void startGame(List<Player> players, int arenaId) {
        // Send time left via action bar
        new BukkitRunnable() {
            int timeLeft = 30;

            @Override
            public void run() {
                if (timeLeft <= 0) {
                    cancel();
                    return;
                }
                for (Player p : players) {
                    p.sendActionBar(ChatColor.YELLOW + "Tempo: " + timeLeft + "s");
                }
                timeLeft--;
            }
        }.runTaskTimer(plugin, 0L, 20L);

        // End game after 30 seconds
        new BukkitRunnable() {
            @Override
            public void run() {
                Player winner = players.get(0); // Simulated winner

                for (Player p : players) {
                    p.sendTitle(ChatColor.GOLD + "Winner: " + winner.getName(), "", 10, 60, 10);
                    p.sendMessage(ChatColor.GREEN + winner.getName() + " Ganhou!");
                }

                // Save to DB (optional)
                // sqlManager.saveWinner(winner, arenaId);

                // Return players to hub after 3 seconds
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        for (Player p : players) {
                            sendToServer(p, "hub");
                            playerArenaMap.remove(p.getUniqueId());
                        }
                    }
                }.runTaskLater(plugin, 60L); // 3 seconds
            }
        }.runTaskLater(plugin, 600L); // Game runs for 30 seconds
    }

    private void sendToServer(Player player, String serverName) {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(b);

        try {
            out.writeUTF("Connect");
            out.writeUTF(serverName);
        } catch (IOException e) {
            e.printStackTrace();
        }

        player.sendPluginMessage(plugin, "BungeeCord", b.toByteArray());
    }
}
