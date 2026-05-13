package com.clutch.particle;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class DatabaseManager implements AutoCloseable {
    private final JavaPlugin plugin;
    private Connection connection;

    public DatabaseManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void connect() throws SQLException {
        File databaseFile = new File(plugin.getDataFolder(), "particles.db");
        plugin.getDataFolder().mkdirs();
        connection = DriverManager.getConnection("jdbc:sqlite:" + databaseFile.getAbsolutePath());
        createTables();
    }

    public void saveParticleDefinition(ParticleDefinition definition) throws SQLException {
        String sql = "INSERT INTO particles (particle_name, particle_type, color) VALUES (?, ?, ?) "
                + "ON CONFLICT(particle_name) DO UPDATE SET particle_type = excluded.particle_type, color = excluded.color";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, definition.name());
            statement.setString(2, definition.type());
            statement.setString(3, definition.color());
            statement.executeUpdate();
        }
    }

    public boolean ownsParticle(UUID uuid, String particleName) throws SQLException {
        String sql = "SELECT 1 FROM player_particles WHERE uuid = ? AND particle_name = ? LIMIT 1";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, uuid.toString());
            statement.setString(2, particleName);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    public void addParticleToPlayer(UUID uuid, String particleName) throws SQLException {
        String sql = "INSERT OR IGNORE INTO player_particles (uuid, particle_name, equipped) VALUES (?, ?, 0)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, uuid.toString());
            statement.setString(2, particleName);
            statement.executeUpdate();
        }
    }

    public List<OwnedParticle> getOwnedParticles(UUID uuid) throws SQLException {
        String sql = "SELECT p.particle_name, p.particle_type, p.color, pp.equipped "
                + "FROM player_particles pp JOIN particles p ON p.particle_name = pp.particle_name "
                + "WHERE pp.uuid = ? ORDER BY p.particle_name COLLATE NOCASE";
        List<OwnedParticle> ownedParticles = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, uuid.toString());
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    ownedParticles.add(new OwnedParticle(
                            new ParticleDefinition(
                                    resultSet.getString("particle_name"),
                                    resultSet.getString("particle_type"),
                                    resultSet.getString("color")
                            ),
                            resultSet.getBoolean("equipped")
                    ));
                }
            }
        }
        return ownedParticles;
    }

    public Optional<ParticleDefinition> getEquippedParticle(UUID uuid) throws SQLException {
        String sql = "SELECT p.particle_name, p.particle_type, p.color "
                + "FROM player_particles pp JOIN particles p ON p.particle_name = pp.particle_name "
                + "WHERE pp.uuid = ? AND pp.equipped = 1 LIMIT 1";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, uuid.toString());
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return Optional.empty();
                }
                return Optional.of(new ParticleDefinition(
                        resultSet.getString("particle_name"),
                        resultSet.getString("particle_type"),
                        resultSet.getString("color")
                ));
            }
        }
    }

    public void setEquipped(UUID uuid, String particleName, boolean equipped) throws SQLException {
        try (PreparedStatement unequip = connection.prepareStatement(
                "UPDATE player_particles SET equipped = 0 WHERE uuid = ?")) {
            unequip.setString(1, uuid.toString());
            unequip.executeUpdate();
        }

        if (equipped) {
            try (PreparedStatement equip = connection.prepareStatement(
                    "UPDATE player_particles SET equipped = 1 WHERE uuid = ? AND particle_name = ?")) {
                equip.setString(1, uuid.toString());
                equip.setString(2, particleName);
                equip.executeUpdate();
            }
        }
    }

    @Override
    public void close() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    private void createTables() throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS particles ("
                    + "particle_name TEXT PRIMARY KEY, "
                    + "particle_type TEXT NOT NULL, "
                    + "color TEXT NOT NULL"
                    + ")");
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS player_particles ("
                    + "uuid TEXT NOT NULL, "
                    + "particle_name TEXT NOT NULL, "
                    + "equipped INTEGER NOT NULL DEFAULT 0, "
                    + "PRIMARY KEY (uuid, particle_name)"
                    + ")");
        }
    }

    public record OwnedParticle(ParticleDefinition definition, boolean equipped) {
    }
}
