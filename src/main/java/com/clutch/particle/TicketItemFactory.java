package com.clutch.particle;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

public final class TicketItemFactory {
    private final ParticleKeys keys;
    private final int customModelData;

    public TicketItemFactory(ParticleKeys keys, int customModelData) {
        this.keys = keys;
        this.customModelData = customModelData;
    }

    public ItemStack createTicket(ParticleDefinition definition, int amount) {
        ItemStack itemStack = new ItemStack(Material.PAPER, amount);
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName("§6파티클 획득권");
        if (customModelData > 0) {
            itemMeta.setCustomModelData(customModelData);
        }
        itemMeta.setLore(List.of(
                "§7우클릭 시 파티클을 획득합니다.",
                "§f획득 파티클:",
                "§e" + definition.name()
        ));
        itemMeta.getPersistentDataContainer().set(keys.ticket, PersistentDataType.BYTE, (byte) 1);
        itemMeta.getPersistentDataContainer().set(keys.particleName, PersistentDataType.STRING, definition.name());
        itemMeta.getPersistentDataContainer().set(keys.particleType, PersistentDataType.STRING, definition.type());
        itemMeta.getPersistentDataContainer().set(keys.particleColor, PersistentDataType.STRING, definition.color());
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }
}
