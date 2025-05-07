package com.thomas.minigame.games.tnttag;

import com.thomas.minigame.core.Arena;
import com.thomas.minigame.core.Game; // Make sure Game is imported
import org.bukkit.Bukkit;

public class TNTTagArena extends Arena {

    // REMOVE THIS FIELD: private TNTTagGame game;

    public TNTTagArena(String name, double x, double y, double z) {
        super(name, x, y, z);
    }

    @Override
    public void startGame() {
        Bukkit.getLogger().info("[TNTTagArena " + getName() + "] Attempting to start game.");
        // Check if a game isn't already set in the superclass
        if (super.getGame() == null) {
            TNTTagGame newTntTagGame = new TNTTagGame(this); // 'this' is the TNTTagArena instance

            super.setGame(newTntTagGame); // CRUCIAL: Assign to the Arena's 'game' field

            if (super.getGame() != null) {
                super.getGame().start(); // Start the game logic (ensure Game has a start() method)
                Bukkit.getLogger()
                        .info("[TNTTagArena " + getName() + "] Game set in superclass and started successfully.");
            } else {
                // This case should ideally not happen if setGame works
                Bukkit.getLogger().severe(
                        "[TNTTagArena " + getName() + "] FAILED TO SET GAME in superclass after instantiation!");
            }
        } else {
            Bukkit.getLogger().warning("[TNTTagArena " + getName()
                    + "] startGame called, but a game (from super.getGame()) already exists.");
        }
    }

    @Override
    public void endGame() {
        Bukkit.getLogger().info("[TNTTagArena " + getName() + "] endGame called.");
        // The super.endGame() will handle stopping the game and setting super.game to
        // null via super.setGame(null)
        super.endGame();
        // No need for 'this.game = null;' if the TNTTagArena specific 'game' field is
        // removed.
    }

    // REMOVE or RENAME this method to avoid confusion with Arena.getGame()
    // If you need a type-specific getter:
    public TNTTagGame getSpecificGame() {
        Game currentGame = super.getGame();
        if (currentGame instanceof TNTTagGame) {
            return (TNTTagGame) currentGame;
        }
        return null;
    }
}