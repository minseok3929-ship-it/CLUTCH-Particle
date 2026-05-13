package com.clutch.particle;

import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;

public final class ParticleKeys {
    public final NamespacedKey ticket;
    public final NamespacedKey particleName;
    public final NamespacedKey particleType;
    public final NamespacedKey particleColor;

    public ParticleKeys(JavaPlugin plugin) {
        this.ticket = new NamespacedKey(plugin, "clutch_particle_ticket");
        this.particleName = new NamespacedKey(plugin, "clutch_particle_name");
        this.particleType = new NamespacedKey(plugin, "clutch_particle_type");
        this.particleColor = new NamespacedKey(plugin, "clutch_particle_color");
    }
}
