package com.thomas.minigame.games.tnttag;

import com.thomas.minigame.MinigamesPlugin; // Assuming this is your main plugin class
import com.thomas.minigame.core.Arena;
import com.thomas.minigame.core.Game;
import com.thomas.minigame.util.Countdown; // Assuming Countdown is well-defined
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask; // For storing the task

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TNTTagGame extends Game {

    private TNTTagPlayer taggedPlayer;
    private final List<TNTTagPlayer> activePlayers = new ArrayList<>();
    private final int eliminationInterval = 60; // seconds (consider making this configurable)
    private BukkitTask eliminationTask; // To store and manage the elimination runnable

    public TNTTagGame(Arena arena) {
        super(arena);

    }

    @Override
    public void start() {
        super.start(); // CRUCIAL: This sets this.active = true in the Game base class

        activePlayers.clear(); // Ensure it's empty before starting
        arena.getPlayers().forEach(playerData -> {
            Player player = playerData.getPlayer(); // Assuming PlayerData has getPlayer()
            Location spawnLocation = arena.getSpawnLocation();
            if (player != null && player.isOnline()) {
                activePlayers.add(new TNTTagPlayer(player));
                // Prepare player for game (e.g., clear inventory, set gamemode, heal)
                player.setGameMode(GameMode.ADVENTURE); // Or Adventure
                player.getInventory().clear();

                ItemStack windCharge = new ItemStack(Material.WIND_CHARGE, 64);
                player.getInventory().addItem(windCharge);
                player.setHealth(player.getMaxHealth());
                player.setFallDistance(20);
                player.setFoodLevel(20);
                player.teleport(spawnLocation);
            }
        });

        if (activePlayers.size() < 2) {
            arena.sendMessage("Not enough players to start TNT Tag. Game will not begin.");
            Bukkit.getLogger().warning("[TNTTagGame] Not enough players (" + activePlayers.size() + ") in arena "
                    + arena.getName() + ". Stopping game.");
            this.arena.endGame();
            return;
        }

        arena.sendMessage("§aBatata Quente vai começar em 10 segundos!");
        new Countdown(10, // Countdown duration
                secondsLeft -> arena.sendMessage("§eJogo começando em " + secondsLeft + "s..."),
                this::beginActualGameLogic // Method to call when countdown finishes
        ).start(); // Pass plugin instance to Countdown if it needs it for scheduling
    }

    private void beginActualGameLogic() {
        arena.sendMessage("§cJogo Começou! CORRA!");

        pickRandomTagged();

        if (eliminationTask != null) { // Cancel any existing task, just in case
            eliminationTask.cancel();
        }
        eliminationTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!isActive() || activePlayers.size() <= 1) { // Check if game should still be running
                    if (isActive() && activePlayers.size() <= 1) { // Ensure game ends if not already
                        determineWinnerAndEnd();
                    }
                    cancel(); // Stop this runnable
                    return;
                }

                if (taggedPlayer != null && activePlayers.contains(taggedPlayer)) {
                    arena.sendMessage("§4BOOM! §e" + taggedPlayer.getPlayer().getName() + " Queimou!");
                    eliminatePlayer(taggedPlayer); // Pass the TNTTagPlayer object

                    if (activePlayers.size() <= 1) { // Check again after elimination
                        determineWinnerAndEnd();
                        cancel(); // Stop this runnable
                    } else {
                        // Start countdown for next tag
                        arena.sendMessage("§6A nova Batata Quente vai aparecer em 10 segundos!");
                        new Countdown(10,
                                sec -> arena.sendMessage("§eNova Batata Quente em " + sec + "s..."),
                                TNTTagGame.this::pickRandomTagged).start();
                    }
                } else if (taggedPlayer == null && !activePlayers.isEmpty()) {
                    // Edge case: tagged player somehow became null but game is ongoing
                    Bukkit.getLogger().warning(
                            "[TNTTagGame] Tagged player was null during elimination round. Picking a new one.");
                    pickRandomTagged();
                }
            }
        }.runTaskTimer(MinigamesPlugin.getInstance(), eliminationInterval * 20L, eliminationInterval * 20L);
    }

    private void pickRandomTagged() {

        if (taggedPlayer != null) {
            taggedPlayer.setTagged(false); // Untag previous player
        }

        // Ensure there are players to choose from
        List<TNTTagPlayer> choices = new ArrayList<>(activePlayers);
        if (choices.isEmpty()) {
            // This might indicate the game should end
            if (isActive())
                determineWinnerAndEnd();
            return;
        }

        taggedPlayer = choices.get(new Random().nextInt(choices.size()));
        taggedPlayer.setTagged(true);
        arena.sendMessage("§c" + taggedPlayer.getPlayer().getName() + " está com a Batata Quente!");
    }

    public void onPlayerHit(Player damager, Player victim) {

        // Find the victim in our list of active TNTTagPlayers
        TNTTagPlayer victimAsTNTTagPlayer = activePlayers.stream()
                .filter(p -> p.getPlayer().getUniqueId().equals(victim.getUniqueId()))
                .findFirst().orElse(null);

        if (victimAsTNTTagPlayer != null && victimAsTNTTagPlayer != taggedPlayer) { // Cannot tag self
            arena.sendMessage("§c" + victimAsTNTTagPlayer.getPlayer().getName() + " está com a Batata Quente!");
            taggedPlayer.setTagged(false); // Untag the old player
            victimAsTNTTagPlayer.setTagged(true); // Tag the new player
            taggedPlayer = victimAsTNTTagPlayer; // Update who is currently tagged
            // arena.sendMessage("§e" + damager.getName() + " tagged " + victim.getName() +
            // "! §c" + victim.getName()
            // + " is now IT!");
        } else if (victimAsTNTTagPlayer == taggedPlayer) {
            damager.sendMessage("§e?????");
        } else {
        }
    }

    private void eliminatePlayer(TNTTagPlayer playerToEliminate) {
        if (playerToEliminate == null || !activePlayers.contains(playerToEliminate))
            return;

        Player bukkitPlayer = playerToEliminate.getPlayer();
        bukkitPlayer.sendMessage("§cVoce foi eliminado!");
        bukkitPlayer.getInventory().clear(); // Clear inventory

        if (Bukkit.getWorld("world") != null) { // Null check for world
            bukkitPlayer.teleport(Bukkit.getWorld("world").getSpawnLocation());
        } else {
        }

        activePlayers.remove(playerToEliminate);
        playerToEliminate.setTagged(false); // Ensure they are not marked as tagged if they were

        if (taggedPlayer == playerToEliminate) { // If the eliminated player was the one tagged
            taggedPlayer = null; // No one is tagged until pickRandomTagged is called again
        }
    }

    private void determineWinnerAndEnd() {

        // This initial check is good for handling premature calls or already ended
        // games.
        if (!isActive() && activePlayers.isEmpty() && taggedPlayer == null) {
            if (eliminationTask != null && !eliminationTask.isCancelled()) {
                eliminationTask.cancel();
            }
            // Ensure this.arena.endGame() is called to properly clean up via game.stop()
            // If the game wasn't active, arena.endGame() might not have been called.
            if (this.arena.getGame() != null) { // Check if the arena still thinks this game is set
                this.arena.endGame();
            }
            return;
        }

        if (activePlayers.size() == 1) {
            TNTTagPlayer winner = activePlayers.get(0);
            final Player bukkitPlayer = winner.getPlayer(); // Make final for use in Runnables
            String winnerName = bukkitPlayer.getName();

            // --- WINNER CELEBRATION ---

            // 1. Messages
            String winnerMessage = "§6§lVENCEDOR: §e§l" + winnerName + "§6§l!";
            String subtitleMessage = "§eᴠᴏᴄᴇ ᴠᴇɴᴄᴇᴜ ᴏ ᴊᴏɢᴏ!";
            arena.sendMessage("§aFim de jogo! " + winnerMessage);

            // 2. Winner Title
            bukkitPlayer.sendTitle("§6§lᴠɪᴛóʀɪᴀ!", // Main title text
                    subtitleMessage, // Subtitle text
                    10, // Fade-in time (0.5 seconds)
                    70, // Stay time (3.5 seconds)
                    20); // Fade-out time (1 second)

            // 3. Play Sounds for Winner
            bukkitPlayer.playSound(bukkitPlayer.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (bukkitPlayer.isOnline()) {
                        bukkitPlayer.playSound(bukkitPlayer.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 0.8f,
                                1.2f);
                    }
                }
            }.runTaskLater(MinigamesPlugin.getInstance(), 10L); // 0.5 second delay

            // 4. Spawn Particles for the Winner

            // A. Immediate burst of particles
            bukkitPlayer.getWorld().spawnParticle(
                    Particle.FIREWORK,
                    bukkitPlayer.getLocation().add(0, 1, 0),
                    75,
                    1, 1, 1,
                    0.1);

            // B. Sustained particle effect
            new BukkitRunnable() {
                int durationTicks = 4 * 20; // Effect lasts for 4 seconds
                final Location playerLocationAtWin = bukkitPlayer.getLocation().clone();

                @Override
                public void run() {
                    if (durationTicks <= 0 || !bukkitPlayer.isOnline()
                            || !bukkitPlayer.getWorld().equals(playerLocationAtWin.getWorld())) {
                        this.cancel();
                        return;
                    }

                    // Spawn particles in a celebratory manner around the captured location
                    for (int i = 0; i < 15; i++) { // Spawn 15 particles each time this task runs
                        double angle = Math.random() * Math.PI * 2;
                        double horizontalRadius = Math.random() * 1.5;
                        double x = Math.cos(angle) * horizontalRadius;
                        double z = Math.sin(angle) * horizontalRadius;
                        double y = Math.random() * 2.5; // Particles appear at different heights

                        playerLocationAtWin.getWorld().spawnParticle(
                                Particle.TOTEM_OF_UNDYING,
                                playerLocationAtWin.clone().add(x, y, z),
                                1,
                                0, 0, 0,
                                0);
                    }
                    durationTicks -= 5; // Task runs every 5 ticks
                }
            }.runTaskTimer(MinigamesPlugin.getInstance(), 0L, 5L);

            // --- END CELEBRATION ---

            bukkitPlayer.getInventory().clear(); // Clear inventory (already there)

            new BukkitRunnable() {
                @Override
                public void run() {
                    if (bukkitPlayer.isOnline()) { // Good practice: check if player is still online
                        if (Bukkit.getWorld("world") != null) {
                            bukkitPlayer.teleport(Bukkit.getWorld("world").getSpawnLocation());
                        } else {
                            Bukkit.getLogger()
                                    .severe("[TNTTagGame] World 'world' not found for player teleport after win!");
                        }
                    }
                }
            }.runTaskLater(MinigamesPlugin.getInstance(), 100L); // 5 seconds delay

        } else if (activePlayers.isEmpty()) {
            arena.sendMessage("§aFim de jogo! §eNenhum jogador restante, então nenhum vencedor.");
        } else {
            arena.sendMessage("§aFim de jogo???");
        }
        this.arena.endGame();
    }

    @Override
    public void stop() {
        super.stop(); // CRUCIAL: This sets this.active = false in the Game base class
        Bukkit.getLogger()
                .info("[TNTTagGame] Stop method called for arena: " + arena.getName() + ". Game active: " + isActive());

        // Cancel the BukkitRunnable task if it's running
        if (eliminationTask != null && !eliminationTask.isCancelled()) {
            eliminationTask.cancel();
            Bukkit.getLogger().info("[TNTTagGame] Elimination task cancelled.");
        }
        eliminationTask = null;

        // Clear lists and reset game-specific states
        if (taggedPlayer != null) {
            taggedPlayer.setTagged(false); // Ensure last tagged player is untagged visually
            taggedPlayer = null;
        }
        activePlayers.forEach(p -> {
            if (p.getPlayer().isOnline()) {
                if (Bukkit.getWorld("world") != null && p.getPlayer().getWorld().equals(Bukkit.getWorld("world"))) {
                    p.getPlayer().setGameMode(GameMode.ADVENTURE); // Or your server's default
                    // Teleport, play sound whatever
                }
            }
        });
        activePlayers.clear();

        Bukkit.getLogger().info("[TNTTagGame] TNT Tag specific cleanup complete for arena: " + arena.getName());

        // typical flow is: someCondition -> game.determineWinnerAndEnd() ->
        // arena.endGame() -> game.stop()
    }
}