package com.thomas.minigame;

import com.thomas.minigame.core.GameManager;
import com.thomas.minigame.games.tnttag.TNTTagListener;
import com.thomas.minigame.listeners.GameItemProtectionListener;
import com.thomas.minigame.listeners.PlayerJoinListener;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.Bukkit;

public class MinigamesPlugin extends JavaPlugin {

    private static MinigamesPlugin instance;
    private GameManager gameManager;

    @Override
    public void onEnable() {
        getServer().getMessenger().registerOutgoingPluginChannel(this, "velocity:player");
        // Register TNT Tag events
        getServer().getPluginManager().registerEvents(new TNTTagListener(), this);

        instance = this;

        this.gameManager = new GameManager();
        gameManager.init();

        Bukkit.getPluginManager().registerEvents(new PlayerJoinListener(), this);
        getServer().getPluginManager().registerEvents(new GameItemProtectionListener(), this);

        getLogger().info("Minigames plugin enabled!");
    }

    @Override
    public void onDisable() {
        gameManager.shutdown();
        getLogger().info("Minigames plugin disabled.");
    }

    public static MinigamesPlugin getInstance() {
        return instance;
    }

    public GameManager getGameManager() {
        return gameManager;
    }
}
