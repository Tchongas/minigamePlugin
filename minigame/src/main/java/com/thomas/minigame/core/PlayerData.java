package com.thomas.minigame.core;

import org.bukkit.entity.Player;

public class PlayerData {
    private final Player player;

    public PlayerData(Player player) {
        this.player = player;
    }

    public Player getPlayer() {
        return player;
    }
}
