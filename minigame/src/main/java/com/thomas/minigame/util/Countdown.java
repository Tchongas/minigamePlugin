package com.thomas.minigame.util;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.function.Consumer;

public class Countdown {

    private int seconds;
    private final Consumer<Integer> onTick;
    private final Runnable onFinish;

    public Countdown(int seconds, Consumer<Integer> onTick, Runnable onFinish) {
        this.seconds = seconds;
        this.onTick = onTick;
        this.onFinish = onFinish;
    }

    public void start() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (seconds <= 0) {
                    cancel();
                    onFinish.run();
                } else {
                    onTick.accept(seconds);
                    seconds--;
                }
            }
        }.runTaskTimer(com.thomas.minigame.MinigamesPlugin.getInstance(), 0L, 20L);
    }
}
