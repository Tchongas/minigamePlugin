package com.thomas.minigame.games.tnttag;

import com.thomas.minigame.MinigamesPlugin; // Assuming this is your main plugin class
import com.thomas.minigame.core.Arena;
import com.thomas.minigame.core.Game;
import com.thomas.minigame.util.Countdown; // Assuming Countdown is well-defined
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask; // For storing the task

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
// Removed unused import: java.util.stream.Collectors;
// Removed unused import: java.util.Set; - was not present but good to keep imports clean

public class TNTTagGame extends Game {

    private TNTTagPlayer taggedPlayer;
    private final List<TNTTagPlayer> activePlayers = new ArrayList<>();
    private final int eliminationInterval = 30; // seconds (consider making this configurable)
    private BukkitTask eliminationTask; // To store and manage the elimination runnable

    public TNTTagGame(Arena arena) {
        super(arena);
        // The "game will start in X seconds" message is better placed within the
        // start() method's countdown
        // to ensure the game object is fully initialized and associated with the arena.
    }

    @Override
    public void start() {
        super.start(); // CRUCIAL: This sets this.active = true in the Game base class
        Bukkit.getLogger().info(
                "[TNTTagGame] Start method called for arena: " + arena.getName() + ". Game active: " + isActive());

        // Initialize activePlayers list from the arena's players
        activePlayers.clear(); // Ensure it's empty before starting
        arena.getPlayers().forEach(playerData -> {
            Player player = playerData.getPlayer(); // Assuming PlayerData has getPlayer()
            if (player != null && player.isOnline()) {
                activePlayers.add(new TNTTagPlayer(player));
                // Prepare player for game (e.g., clear inventory, set gamemode, heal)
                player.setGameMode(GameMode.SURVIVAL); // Or Adventure
                player.getInventory().clear();
                player.setHealth(player.getMaxHealth());
                player.setFoodLevel(20);
                Bukkit.getLogger().info("[TNTTagGame] Added and prepped player: " + player.getName());
            }
        });

        if (activePlayers.size() < 2) {
            arena.sendMessage("Not enough players to start TNT Tag. Game will not begin.");
            Bukkit.getLogger().warning("[TNTTagGame] Not enough players (" + activePlayers.size() + ") in arena "
                    + arena.getName() + ". Stopping game.");
            // Since super.start() was called, we need to properly stop if conditions aren't
            // met.
            // This will call arena.endGame() which in turn calls this.stop()
            this.arena.endGame(); // This will also set game.active to false
            return;
        }

        arena.sendMessage("§aBatata Quente vai começar em 10 segundos!");
        new Countdown(10, // Countdown duration
                secondsLeft -> arena.sendMessage("§eJogo começando em " + secondsLeft + "s..."),
                this::beginActualGameLogic // Method to call when countdown finishes
        ).start(); // Pass plugin instance to Countdown if it needs it for scheduling
    }

    private void beginActualGameLogic() {
        if (!isActive()) { // Double check if game was stopped during countdown
            Bukkit.getLogger()
                    .info("[TNTTagGame] beginActualGameLogic called, but game is no longer active. Aborting.");
            return;
        }
        arena.sendMessage("§cJogo Começou! CORRA!");
        Bukkit.getLogger().info("[TNTTagGame] beginActualGameLogic called for arena: " + arena.getName());

        pickRandomTagged();

        // Schedule the elimination task
        if (eliminationTask != null) { // Cancel any existing task, just in case
            eliminationTask.cancel();
        }
        eliminationTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!isActive() || activePlayers.size() <= 1) { // Check if game should still be running
                    Bukkit.getLogger().info(
                            "[TNTTagGame] Elimination task: Game no longer active or too few players. Cancelling task.");
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
        if (!isActive() || activePlayers.isEmpty()) {
            Bukkit.getLogger().info("[TNTTagGame] pickRandomTagged: Game not active or no players left.");
            return;
        }

        if (taggedPlayer != null) {
            taggedPlayer.setTagged(false); // Untag previous player
        }

        // Ensure there are players to choose from
        List<TNTTagPlayer> choices = new ArrayList<>(activePlayers);
        if (choices.isEmpty()) {
            Bukkit.getLogger().warning("[TNTTagGame] pickRandomTagged: No active players to choose from.");
            // This might indicate the game should end
            if (isActive())
                determineWinnerAndEnd();
            return;
        }

        taggedPlayer = choices.get(new Random().nextInt(choices.size()));
        taggedPlayer.setTagged(true);
        arena.sendMessage("§c" + taggedPlayer.getPlayer().getName() + " Esta com a Batata Quente!");
        Bukkit.getLogger().info(
                "[TNTTagGame] " + taggedPlayer.getPlayer().getName() + " is now tagged in arena: " + arena.getName());
    }

    public void onPlayerHit(Player damager, Player victim) {
        if (!isActive()) { // IMPORTANT: Only process hits if the game is active
            Bukkit.getLogger().finest("[TNTTagGame] onPlayerHit called but game is not active.");
            return;
        }

        Bukkit.getLogger().info("[TNTTagGame] onPlayerHit: " + damager.getName() + " hit " + victim.getName()
                + " in arena " + arena.getName());

        if (taggedPlayer == null) {
            Bukkit.getLogger()
                    .info("[TNTTagGame] onPlayerHit: No tagged player currently. Damager: " + damager.getName());
            // This could happen if tag transfer is very fast or during initial phase before
            // first tag
            return;
        }

        if (!damager.getUniqueId().equals(taggedPlayer.getPlayer().getUniqueId())) {
            Bukkit.getLogger().info("[TNTTagGame] onPlayerHit: Damager (" + damager.getName()
                    + ") is not the tagged player (" + taggedPlayer.getPlayer().getName() + ").");
            return;
        }

        // Find the victim in our list of active TNTTagPlayers
        TNTTagPlayer victimAsTNTTagPlayer = activePlayers.stream()
                .filter(p -> p.getPlayer().getUniqueId().equals(victim.getUniqueId()))
                .findFirst().orElse(null);

        if (victimAsTNTTagPlayer != null && victimAsTNTTagPlayer != taggedPlayer) { // Cannot tag self
            Bukkit.getLogger().info("[TNTTagGame] Tag transfer: " + taggedPlayer.getPlayer().getName() + " -> "
                    + victimAsTNTTagPlayer.getPlayer().getName());
            taggedPlayer.setTagged(false); // Untag the old player
            victimAsTNTTagPlayer.setTagged(true); // Tag the new player
            taggedPlayer = victimAsTNTTagPlayer; // Update who is currently tagged
            // arena.sendMessage("§e" + damager.getName() + " tagged " + victim.getName() +
            // "! §c" + victim.getName()
            // + " is now IT!");
        } else if (victimAsTNTTagPlayer == taggedPlayer) {
            damager.sendMessage("§e?????");
        } else {
            Bukkit.getLogger().info("[TNTTagGame] onPlayerHit: Victim (" + victim.getName()
                    + ") not found in activePlayers or is not a valid target.");
        }
    }

    private void eliminatePlayer(TNTTagPlayer playerToEliminate) {
        if (playerToEliminate == null || !activePlayers.contains(playerToEliminate))
            return;

        Player bukkitPlayer = playerToEliminate.getPlayer();
        bukkitPlayer.sendMessage("§cVoce foi eliminada!");
        bukkitPlayer.getInventory().clear(); // Clear inventory

        // Teleport to a lobby or main spawn after a delay, or immediately
        // For now, teleporting to world spawn. Make this configurable.
        if (Bukkit.getWorld("world") != null) { // Null check for world
            bukkitPlayer.teleport(Bukkit.getWorld("world").getSpawnLocation());
        } else {
            Bukkit.getLogger().severe("[TNTTagGame] World 'world' not found for player elimination teleport!");
        }

        activePlayers.remove(playerToEliminate);
        playerToEliminate.setTagged(false); // Ensure they are not marked as tagged if they were

        if (taggedPlayer == playerToEliminate) { // If the eliminated player was the one tagged
            taggedPlayer = null; // No one is tagged until pickRandomTagged is called again
        }
        Bukkit.getLogger().info("[TNTTagGame] Player " + bukkitPlayer.getName() + " eliminated. Active players: "
                + activePlayers.size());
    }

    private void determineWinnerAndEnd() {
        Bukkit.getLogger().info("[TNTTagGame] Determining winner and ending game in arena: " + arena.getName());
        if (!isActive() && activePlayers.isEmpty() && taggedPlayer == null) {
            // Game might have been stopped externally, avoid duplicate messages if already
            // processed.
            Bukkit.getLogger().info(
                    "[TNTTagGame] determineWinnerAndEnd called but game state indicates it might already be fully stopped or was never properly active.");
            if (eliminationTask != null && !eliminationTask.isCancelled()) {
                eliminationTask.cancel();
            }
            // Ensure arena.endGame() is called if it hasn't been.
            // However, this method is usually called when active, so direct call to
            // arena.endGame()
            // might be redundant if this is the primary end path.
            // If this can be called when not active, it implies an external stop might have
            // occurred.
            // Consider if arena.endGame() is always the final call needed.
            this.arena.endGame(); // This will call this.stop()
            return;
        }

        if (activePlayers.size() == 1) {
            TNTTagPlayer winner = activePlayers.get(0);
            arena.sendMessage("§aFim de jogo! Vencedor: §6" + winner.getPlayer().getName() + "!");
            // Give rewards, teleport, etc.
        } else if (activePlayers.isEmpty()) {
            arena.sendMessage("§aFim de jogo! §eNenhum jogador restante, entao nenhum vencedor.");
        } else {
            // This case (more than 1 player) should ideally not happen if logic is correct,
            // but handle it as a draw or by picking a random winner if desired.
            arena.sendMessage("§aEmpate????");
        }

        // Call arena.endGame() which will trigger this.stop() and cleanup in Arena and
        // Game base classes
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
            // Additional cleanup for players if needed (e.g., remove from scoreboards,
            // restore inventory if saved)
            // Teleporting out or setting gamemode back to default should happen here or in
            // Arena.removePlayer
            if (p.getPlayer().isOnline()) {
                if (Bukkit.getWorld("world") != null && p.getPlayer().getWorld().equals(Bukkit.getWorld("world"))) { // only
                                                                                                                     // if
                                                                                                                     // in
                                                                                                                     // game
                                                                                                                     // world
                    p.getPlayer().setGameMode(GameMode.SURVIVAL); // Or your server's default
                    // Teleport to a lobby or main spawn if not already done by elimination
                }
            }
        });
        activePlayers.clear();

        Bukkit.getLogger().info("[TNTTagGame] TNT Tag specific cleanup complete for arena: " + arena.getName());
        // Arena.endGame() is typically called to initiate the stop sequence.
        // If this stop() is called directly, ensure Arena also knows game is over.
        // However, the typical flow is: someCondition -> game.determineWinnerAndEnd()
        // -> arena.endGame() -> game.stop()
    }
}