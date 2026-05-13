package com.clutch.particle;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;
import java.util.Optional;

public final class ParticleDisplayTask implements Runnable {
    private static final Color DEFAULT_DUST_COLOR = Color.fromRGB(255, 255, 255);

    private final JavaPlugin plugin;
    private final DatabaseManager databaseManager;

    public ParticleDisplayTask(JavaPlugin plugin, DatabaseManager databaseManager) {
        this.plugin = plugin;
        this.databaseManager = databaseManager;
    }

    @Override
    public void run() {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            try {
                Optional<ParticleDefinition> equippedParticle = databaseManager.getEquippedParticle(player.getUniqueId());
                equippedParticle.ifPresent(definition -> spawn(player, definition));
            } catch (SQLException exception) {
                exception.printStackTrace();
            }
        }
    }

    private void spawn(Player player, ParticleDefinition definition) {
        Optional<Particle> optionalParticle = ParticleTypeSupport.toBukkitParticle(definition.type());
        if (optionalParticle.isEmpty()) {
            return;
        }
        Particle particle = optionalParticle.get();
        Location location = player.getLocation().add(0, 1.1, 0);
        if (ParticleTypeSupport.normalize(definition.type()).equals("DUST")) {
            Color color = ParticleTypeSupport.parseHexColor(definition.color()).orElse(DEFAULT_DUST_COLOR);
            Particle.DustOptions dustOptions = new Particle.DustOptions(color, 1.0F);
            player.getWorld().spawnParticle(particle, location, 8, 0.35, 0.45, 0.35, 0.0, dustOptions);
            return;
        }
        player.getWorld().spawnParticle(particle, location, 6, 0.35, 0.45, 0.35, 0.01);
    }
}
