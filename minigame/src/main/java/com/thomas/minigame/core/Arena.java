package com.thomas.minigame.core;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;

public abstract class Arena {

    private final String name;
    private final Location spawnLocation;
    private final Set<PlayerData> players = new HashSet<>();
    private Game game;

    public Arena(String name, double x, double y, double z) {
        this.name = name;
        World world = Bukkit.getWorld("world"); // You can improve this later
        this.spawnLocation = new Location(world, x, y, z);
    }

    public void addPlayer(Player player) {
        PlayerData data = new PlayerData(player);
        players.add(data);
        player.teleport(spawnLocation);
        sendMessage(player.getName() + " joined " + name + "!");
        checkStart();
    }

    public void removePlayer(Player player) {
        players.removeIf(p -> p.getPlayer().equals(player));
    }

    protected void checkStart() {
        if (players.size() >= 2 && game == null) {
            startGame(); // Call subclass-implemented method
        }
    }

    public void endGame() {
        if (game != null) {
            game.stop();
            game = null;
        }
    }

    public boolean isRunning() {
        return game != null;
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
        return game;
    }

    public Location getSpawnLocation() {
        return spawnLocation;
    }

    public abstract void startGame();
}
