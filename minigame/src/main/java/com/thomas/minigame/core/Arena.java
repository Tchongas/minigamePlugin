package com.thomas.minigame.core;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.thomas.minigame.MinigamesPlugin;

import java.util.HashSet;
import java.util.Set;

public abstract class Arena {

    private final String name;
    private final Location spawnLocation;
    private final Set<PlayerData> players = new HashSet<>();
    private Game game; // This is the single source of truth for the current game

    public Arena(String name, double x, double y, double z) {
        this.name = name;
        World world = Bukkit.getWorld("world"); // Consider making world configurable or passed in
        if (world == null) {
            Bukkit.getLogger()
                    .severe("Failed to find world 'world' for arena " + name + ". Spawn location will be invalid.");
            // Handle this error appropriately, maybe throw an exception or use a default
            // spawn
            this.spawnLocation = null; // Or some default
        } else {
            this.spawnLocation = new Location(world, x, y, z);
        }
    }

    public void addPlayer(Player player) {
        if (spawnLocation == null) {
            player.sendMessage("Arena " + name + " is not properly configured (spawn location missing).");
            Bukkit.getLogger()
                    .warning("Cannot add player " + player.getName() + " to arena " + name + " due to missing spawn.");
            return;
        }
        PlayerData data = new PlayerData(player);
        players.add(data);
        checkStart();
    }

    public void removePlayer(Player player) {
        players.removeIf(p -> p.getPlayer().equals(player));
        // Fazer testar se so tem 1 player, se tiver fazer ganhar
    }

    public abstract void startGame(); // To be implemented by subclasses like TNTTagArena

    protected void checkStart() {
        // Ensure there are enough players (>= 2) and no game has been started yet
        if (players.size() >= 2 && this.game == null) {
            Bukkit.getLogger().info("[Arena " + name + "] checkStart: Conditions met. Calling startGame().");
            startGame(); // This will call the subclass's implementation
        } else {
            if (players.size() < 2) {
                Bukkit.getLogger().info("[Arena " + name + "] checkStart: Not enough players (" + players.size()
                        + "). Need at least 2.");
            }
            if (this.game != null) {
                Bukkit.getLogger().info("[Arena " + name + "] checkStart: Game already exists.");
            }
        }
    }

    public void waitAndRestartGame() {
        new BukkitRunnable() {
            @Override
            public void run() {
                checkStart();
            }
        }.runTaskLater(MinigamesPlugin.getInstance(), 400L); // 20 seconds
    }

    public void endGame() {
        Bukkit.getLogger().info("[Arena " + name + "] endGame called.");
        if (this.game != null) {
            this.game.stop(); // Ensure your Game objects have a stop() method
            this.setGame(null); // Use the setter to clear the game
        } else {
            Bukkit.getLogger().info("[Arena " + name + "] endGame called, but no game was running.");
        }
    }

    public boolean isRunning() {
        // A game is running if the game object exists AND that game considers itself
        // active.
        boolean running = this.game != null && this.game.isActive();
        // Optional: finer logging for debugging, can be removed or set to a lower log
        // level
        Bukkit.getLogger().finest("[Arena " + name + "] isRunning() check: " + running +
                (this.game == null ? " (Game object is null)" : " (Game active: " + this.game.isActive() + ")"));
        return running;
    }

    public Set<PlayerData> getPlayers() {
        return players;
    }

    public void sendMessage(String message) {
        players.forEach(p -> p.getPlayer().sendMessage(message));
    }

    public String getName() {
        return name;
    }

    public Game getGame() {
        return this.game;
    }

    // Setter for the game, to be used by subclasses in their startGame()
    public void setGame(Game game) {
        this.game = game;
        if (game != null) {
            Bukkit.getLogger().info("[Arena " + name + "] Game instance set to: " + game.getClass().getSimpleName());
        } else {
            Bukkit.getLogger().info("[Arena " + name + "] Game instance set to null.");
        }
    }

    public Location getSpawnLocation() {
        return spawnLocation;
    }
}