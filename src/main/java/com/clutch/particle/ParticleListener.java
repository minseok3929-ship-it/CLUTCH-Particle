package com.clutch.particle;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.sql.SQLException;

public final class ParticleListener implements Listener {
    private final DatabaseManager databaseManager;
    private final ParticleKeys keys;
    private final ParticleGui particleGui;

    public ParticleListener(DatabaseManager databaseManager, ParticleKeys keys, ParticleGui particleGui) {
        this.databaseManager = databaseManager;
        this.keys = keys;
        this.particleGui = particleGui;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }
        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        ItemStack itemStack = event.getItem();
        if (itemStack == null || !itemStack.hasItemMeta()) {
            return;
        }
        ItemMeta itemMeta = itemStack.getItemMeta();
        PersistentDataContainer container = itemMeta.getPersistentDataContainer();
        Byte isTicket = container.get(keys.ticket, PersistentDataType.BYTE);
        if (isTicket == null || isTicket != (byte) 1) {
            return;
        }
        event.setCancelled(true);

        Player player = event.getPlayer();
        if (!player.hasPermission("clutch.particle.use")) {
            player.sendMessage("§8[CLUTCH] §c권한이 없습니다.");
            return;
        }
        String particleName = container.get(keys.particleName, PersistentDataType.STRING);
        String particleType = container.get(keys.particleType, PersistentDataType.STRING);
        String particleColor = container.get(keys.particleColor, PersistentDataType.STRING);
        if (particleName == null || particleType == null || particleColor == null) {
            player.sendMessage("§8[CLUTCH] §c잘못된 파티클 획득권입니다.");
            return;
        }

        try {
            if (databaseManager.ownsParticle(player.getUniqueId(), particleName)) {
                player.sendMessage("§8[CLUTCH] §c이미 보유 중인 파티클입니다.");
                return;
            }
            ParticleDefinition definition = new ParticleDefinition(particleName, particleType, particleColor);
            databaseManager.saveParticleDefinition(definition);
            databaseManager.addParticleToPlayer(player.getUniqueId(), particleName);
            consumeOne(itemStack, player);
            player.sendMessage("§8[CLUTCH] §f새로운 파티클을 획득했습니다: §e" + particleName);
        } catch (SQLException exception) {
            player.sendMessage("§8[CLUTCH] §cDB 처리 중 오류가 발생했습니다.");
            exception.printStackTrace();
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        try {
            particleGui.handleClick(event);
        } catch (SQLException exception) {
            event.getWhoClicked().sendMessage("§8[CLUTCH] §cDB 처리 중 오류가 발생했습니다.");
            exception.printStackTrace();
        }
    }

    private void consumeOne(ItemStack itemStack, Player player) {
        int amount = itemStack.getAmount();
        if (amount <= 1) {
            player.getInventory().setItemInMainHand(null);
        } else {
            itemStack.setAmount(amount - 1);
        }
    }
}
