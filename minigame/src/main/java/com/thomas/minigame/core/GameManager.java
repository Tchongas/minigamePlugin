package com.thomas.minigame.core;

import com.thomas.minigame.games.tnttag.TNTTagArena;

import java.util.ArrayList;
import java.util.List;

public class GameManager {

    private final List<Arena> arenas = new ArrayList<>();

    public void init() {
        // Initialize TNT Tag arenas with fixed positions
        arenas.add(new TNTTagArena("Arena_1", 0, -40, 0));
        arenas.add(new TNTTagArena("Arena_2", 0, 0, 0));
        arenas.add(new TNTTagArena("Arena_3", 0, 40, 0));
    }

    public Arena getAvailableArena() {
        for (Arena arena : arenas) {
            if (!arena.isRunning()) {
                return arena;
            }
        }
        return null;
    }

    public void shutdown() {
        arenas.forEach(Arena::endGame);
    }

    public List<Arena> getArenas() {
        return arenas;
    }
}
