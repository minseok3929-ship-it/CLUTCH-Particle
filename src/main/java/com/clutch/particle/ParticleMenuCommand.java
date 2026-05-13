package com.clutch.particle;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.SQLException;

public final class ParticleMenuCommand implements CommandExecutor {
    private final ParticleGui particleGui;

    public ParticleMenuCommand(ParticleGui particleGui) {
        this.particleGui = particleGui;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§8[CLUTCH] §c플레이어만 사용할 수 있습니다.");
            return true;
        }
        if (!player.hasPermission("clutch.particle.use")) {
            player.sendMessage("§8[CLUTCH] §c권한이 없습니다.");
            return true;
        }
        try {
            particleGui.open(player);
        } catch (SQLException exception) {
            player.sendMessage("§8[CLUTCH] §cDB 조회 중 오류가 발생했습니다.");
            exception.printStackTrace();
        }
        return true;
    }
}
