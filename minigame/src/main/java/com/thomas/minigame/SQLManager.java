package com.thomas.minigame;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.*;
import java.util.logging.Level;

/**
 * SQLManager handles connection to the MySQL database
 * and saves winner data when a game ends.
 */
public class SQLManager {

    private final MinigamePlugin plugin;
    private Connection connection;

    public SQLManager(MinigamePlugin plugin) {
        this.plugin = plugin;
        connect();
        createTable();
    }

    // Establish connection to MySQL using config values
    private void connect() {
        String host = plugin.getConfig().getString("mysql.host");
        String database = plugin.getConfig().getString("mysql.database");
        String username = plugin.getConfig().getString("mysql.username");
        String password = plugin.getConfig().getString("mysql.password");
        int port = plugin.getConfig().getInt("mysql.port");

        try {
            connection = DriverManager.getConnection(
                    "jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false",
                    username,
                    password);
            plugin.getLogger().info("MySQL connection established.");
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not connect to MySQL", e);
        }
    }

    // Create table if it doesn't exist
    private void createTable() {
        String sql = "CREATE TABLE IF NOT EXISTS winners (" +
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "uuid VARCHAR(36)," +
                "username VARCHAR(16)," +
                "arena INT," +
                "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ")";
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to create winners table", e);
        }
    }

    // Save winner info to the database
    public void saveWinner(Player player, int arenaId) {
        String sql = "INSERT INTO winners (uuid, username, arena) VALUES (?, ?, ?)";

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, player.getUniqueId().toString());
                ps.setString(2, player.getName());
                ps.setInt(3, arenaId);
                ps.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to save winner", e);
            }
        });
    }
}
