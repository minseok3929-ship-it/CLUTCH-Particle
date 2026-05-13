package com.clutch.particle;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class ParticleTicketCommand implements CommandExecutor, TabCompleter {
    private final DatabaseManager databaseManager;
    private final TicketItemFactory ticketItemFactory;

    public ParticleTicketCommand(DatabaseManager databaseManager, TicketItemFactory ticketItemFactory) {
        this.databaseManager = databaseManager;
        this.ticketItemFactory = ticketItemFactory;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("clutch.particle.admin")) {
            sender.sendMessage("§8[CLUTCH] §c권한이 없습니다.");
            return true;
        }
        if (args.length != 5 || !args[0].equalsIgnoreCase("생성")) {
            sender.sendMessage("§8[CLUTCH] §c사용법: /파티클권 생성 <파티클이름> <파티클타입> <색상> <개수>");
            return true;
        }
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§8[CLUTCH] §c콘솔에서는 획득권을 받을 수 없습니다.");
            return true;
        }

        String particleName = args[1];
        String particleType = ParticleTypeSupport.normalize(args[2]);
        String color = args[3];
        if (!ParticleTypeSupport.isSupported(particleType)) {
            sender.sendMessage("§8[CLUTCH] §c지원하지 않는 파티클 타입입니다: §f" + ParticleTypeSupport.supportedTypeText());
            return true;
        }
        if (!color.equalsIgnoreCase("none") && ParticleTypeSupport.parseHexColor(color).isEmpty()) {
            sender.sendMessage("§8[CLUTCH] §c색상은 none 또는 #RRGGBB 형식이어야 합니다.");
            return true;
        }
        int amount;
        try {
            amount = Integer.parseInt(args[4]);
        } catch (NumberFormatException exception) {
            sender.sendMessage("§8[CLUTCH] §c개수는 숫자여야 합니다.");
            return true;
        }
        if (amount <= 0 || amount > 64) {
            sender.sendMessage("§8[CLUTCH] §c개수는 1~64 사이여야 합니다.");
            return true;
        }

        ParticleDefinition definition = new ParticleDefinition(particleName, particleType, color.equalsIgnoreCase("none") ? "none" : color.toUpperCase(Locale.ROOT));
        try {
            databaseManager.saveParticleDefinition(definition);
        } catch (SQLException exception) {
            sender.sendMessage("§8[CLUTCH] §cDB 저장 중 오류가 발생했습니다.");
            exception.printStackTrace();
            return true;
        }
        ItemStack ticket = ticketItemFactory.createTicket(definition, amount);
        player.getInventory().addItem(ticket).values().forEach(leftover -> player.getWorld().dropItemNaturally(player.getLocation(), leftover));
        sender.sendMessage("§8[CLUTCH] §f파티클 획득권을 생성했습니다: §e" + particleName);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("clutch.particle.admin")) {
            return List.of();
        }
        if (args.length == 1) {
            return List.of("생성");
        }
        if (args.length == 3) {
            return List.of("DUST", "END_ROD", "ENCHANT", "HEART", "SMOKE", "TOTEM", "FLAME");
        }
        if (args.length == 4) {
            return List.of("none", "#F6C343", "#D10000");
        }
        if (args.length == 5) {
            return List.of("1");
        }
        return new ArrayList<>();
    }
}
