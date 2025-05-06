package com.thomas.minigame.core;

public class Game {

    protected final Arena arena;

    public Game(Arena arena) {
        this.arena = arena;
    }

    public void start() {
        arena.sendMessage("Game starting...");
    }

    public void stop() {
        arena.sendMessage("Game ended.");
    }

    public Arena getArena() {
        return arena;
    }
}
