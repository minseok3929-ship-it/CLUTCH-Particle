package com.clutch.particle;

import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;

public final class ClutchParticlePlugin extends JavaPlugin {
    private DatabaseManager databaseManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        ParticleKeys keys = new ParticleKeys(this);
        databaseManager = new DatabaseManager(this);
        try {
            databaseManager.connect();
        } catch (SQLException exception) {
            getLogger().severe("파티클 DB 연결에 실패했습니다. 플러그인을 비활성화합니다.");
            exception.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        TicketItemFactory ticketItemFactory = new TicketItemFactory(keys, getConfig().getInt("ticket.custom-model-data", 0));
        ParticleGui particleGui = new ParticleGui(databaseManager, keys);
        ParticleTicketCommand ticketCommand = new ParticleTicketCommand(databaseManager, ticketItemFactory);

        PluginCommand particleMenuCommand = getCommand("파티클");
        if (particleMenuCommand != null) {
            particleMenuCommand.setExecutor(new ParticleMenuCommand(particleGui));
        }
        PluginCommand particleTicketCommand = getCommand("파티클권");
        if (particleTicketCommand != null) {
            particleTicketCommand.setExecutor(ticketCommand);
            particleTicketCommand.setTabCompleter(ticketCommand);
        }

        getServer().getPluginManager().registerEvents(new ParticleListener(databaseManager, keys, particleGui), this);
        getServer().getScheduler().runTaskTimer(this, new ParticleDisplayTask(this, databaseManager), 20L, 10L);
    }

    @Override
    public void onDisable() {
        if (databaseManager != null) {
            try {
                databaseManager.close();
            } catch (SQLException exception) {
                exception.printStackTrace();
            }
        }
    }
}
