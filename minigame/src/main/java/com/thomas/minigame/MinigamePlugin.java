package com.thomas.minigame;

import org.bukkit.plugin.java.JavaPlugin;

public class MinigamePlugin extends JavaPlugin {
    private static MinigamePlugin instance;
    private GameManager gameManager;
    // private SQLManager sqlManager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        // sqlManager = new SQLManager(this);
        // gameManager = new GameManager(this, sqlManager);

        gameManager = new GameManager(this);

        getServer().getPluginManager().registerEvents(gameManager, this);
        getLogger().info("MinigamePlugin enabled.");
    }

    public static MinigamePlugin getInstance() {
        return instance;
    }

    public GameManager getGameManager() {
        return gameManager;
    }

    // public SQLManager getSqlManager() {
    // return sqlManager;
    // }
}
