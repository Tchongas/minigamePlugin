package com.thomas.minigame.core;

import org.bukkit.Bukkit; // For logging

// Consider making this abstract if it's never instantiated directly,
// and specific games like TNTTagGame will always be used.
// public abstract class Game {
public class Game {

    protected final Arena arena;
    protected boolean active = false; // Tracks if the game logic is currently active

    public Game(Arena arena) {
        this.arena = arena;
    }

    /**
     * Starts the game logic.
     * Sets the game to active and sends a starting message.
     * Subclasses should call super.start() and then add their specific start logic.
     */
    public void start() {
        this.active = true;
        arena.sendMessage("Jogo começando...");
        Bukkit.getLogger().info("Game [" + this.getClass().getSimpleName() + "] in Arena [" + arena.getName()
                + "] has been set to active.");
    }

    /**
     * Stops the game logic.
     * Sets the game to inactive and sends an ended message.
     * Subclasses should call super.stop() and then add their specific stop logic.
     */
    public void stop() {
        this.active = false;
        Bukkit.getLogger().info("Game [" + this.getClass().getSimpleName() + "] in Arena [" + arena.getName()
                + "] has been set to inactive.");
    }

    /**
     * Checks if the game is currently active.
     * 
     * @return true if the game is active, false otherwise.
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Gets the arena this game is running in.
     * 
     * @return The Arena instance.
     */
    public Arena getArena() {
        return arena;
    }
}