package com.clutch.particle;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public final class ParticleGui {
    private static final String TITLE = "§8파티클";

    private final DatabaseManager databaseManager;
    private final ParticleKeys keys;

    public ParticleGui(DatabaseManager databaseManager, ParticleKeys keys) {
        this.databaseManager = databaseManager;
        this.keys = keys;
    }

    public void open(Player player) throws SQLException {
        List<DatabaseManager.OwnedParticle> ownedParticles = databaseManager.getOwnedParticles(player.getUniqueId());
        int size = Math.max(9, Math.min(54, ((ownedParticles.size() + 8) / 9) * 9));
        Inventory inventory = Bukkit.createInventory(null, size, TITLE);
        for (DatabaseManager.OwnedParticle ownedParticle : ownedParticles) {
            inventory.addItem(createGuiItem(ownedParticle));
        }
        player.openInventory(inventory);
    }

    public void handleClick(InventoryClickEvent event) throws SQLException {
        if (!event.getView().getTitle().equals(TITLE)) {
            return;
        }
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        ItemStack currentItem = event.getCurrentItem();
        if (currentItem == null || !currentItem.hasItemMeta()) {
            return;
        }
        String particleName = currentItem.getItemMeta().getPersistentDataContainer()
                .get(keys.particleName, PersistentDataType.STRING);
        if (particleName == null) {
            return;
        }
        boolean currentlyEquipped = currentItem.getType() == Material.LIME_DYE;
        databaseManager.setEquipped(player.getUniqueId(), particleName, !currentlyEquipped);
        player.sendMessage(currentlyEquipped
                ? "§8[CLUTCH] §f파티클을 해제했습니다: §e" + particleName
                : "§8[CLUTCH] §f파티클을 장착했습니다: §e" + particleName);
        open(player);
    }

    private ItemStack createGuiItem(DatabaseManager.OwnedParticle ownedParticle) {
        ParticleDefinition definition = ownedParticle.definition();
        ItemStack itemStack = new ItemStack(ownedParticle.equipped() ? Material.LIME_DYE : Material.GRAY_DYE);
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName((ownedParticle.equipped() ? "§a" : "§e") + definition.name());
        List<String> lore = new ArrayList<>();
        lore.add("§7타입: §f" + definition.type());
        lore.add("§7색상: §f" + definition.color());
        lore.add(ownedParticle.equipped() ? "§a장착 중" : "§7클릭하여 장착");
        if (ownedParticle.equipped()) {
            lore.add("§7클릭하여 해제");
        }
        itemMeta.setLore(lore);
        itemMeta.getPersistentDataContainer().set(keys.particleName, PersistentDataType.STRING, definition.name());
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }
}
