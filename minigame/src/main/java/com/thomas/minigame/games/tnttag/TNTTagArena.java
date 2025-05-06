package com.thomas.minigame.games.tnttag;

import com.thomas.minigame.core.Arena;

public class TNTTagArena extends Arena {

    private TNTTagGame game;

    public TNTTagArena(String name, double x, double y, double z) {
        super(name, x, y, z);
    }

    @Override
    public void startGame() {
        this.game = new TNTTagGame(this);
        game.start();
    }

    @Override
    public void endGame() {
        super.endGame();
        this.game = null;
    }

    public TNTTagGame getGame() {
        return game;
    }
}
