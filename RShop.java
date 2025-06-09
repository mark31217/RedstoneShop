package com.example.rshop;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class RShop extends JavaPlugin implements Listener, TabExecutor {

    private final Map<UUID, PurchaseData> purchaseMap = new HashMap<>();

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        getCommand("rshop").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player player) {
            openMainMenu(player);
        }
        return true;
    }

    private void openMainMenu(Player player) {
        Inventory menu = Bukkit.createInventory(null, 27, "§8RedstoneShop");

        menu.setItem(10, createItem(Material.REDSTONE, "§cRedstone"));
        menu.setItem(12, createItem(Material.PISTON, "§aPiston"));
        menu.setItem(14, createItem(Material.OBSERVER, "§6Observer"));
        menu.setItem(16, createItem(Material.HOPPER, "§fHopper"));

        player.openInventory(menu);
    }

    private void openQuantityMenu(Player player, String itemName) {
        Inventory menu = Bukkit.createInventory(null, 27, "§8RShop");

        PurchaseData data = purchaseMap.get(player.getUniqueId());
        if (data == null || !data.item.equalsIgnoreCase(itemName)) {
            data = new PurchaseData(itemName, 1); // Reset amount if new item
            purchaseMap.put(player.getUniqueId(), data);
        }

        int unitPrice = getItemPrice(itemName);

        menu.setItem(11, createItem(Material.RED_STAINED_GLASS_PANE, "§c-8"));
        menu.setItem(12, createItem(Material.RED_STAINED_GLASS_PANE, "§c-1"));

        menu.setItem(13, createItemWithAmount(getMaterial(itemName), "§e" + capitalize(itemName), data.amount));

        menu.setItem(14, createItem(Material.LIME_STAINED_GLASS_PANE, "§a+1"));
        menu.setItem(15, createItem(Material.LIME_STAINED_GLASS_PANE, "§a+8"));

        menu.setItem(18, createItem(Material.ARROW, "§7Back"));
        menu.setItem(26, createItem(Material.EMERALD_BLOCK, "§aConfirm"));

        player.openInventory(menu);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player player)) return;
        if (e.getCurrentItem() == null || !e.getCurrentItem().hasItemMeta()) return;

        String title = e.getView().getTitle();
        String name = e.getCurrentItem().getItemMeta().getDisplayName();
        e.setCancelled(true);

        if (title.equals("§8RedstoneShop")) {
            if (name.contains("Redstone")) openQuantityMenu(player, "redstone");
            else if (name.contains("Piston")) openQuantityMenu(player, "piston");
            else if (name.contains("Observer")) openQuantityMenu(player, "observer");
            else if (name.contains("Hopper")) openQuantityMenu(player, "hopper");

        } else if (title.equals("§8RShop")) {
            PurchaseData data = purchaseMap.get(player.getUniqueId());
            if (data == null) return;

            switch (name) {
                case "§a+1" -> {
                    data.amount += 1;
                    openQuantityMenu(player, data.item);
                }
                case "§a+8" -> {
                    data.amount += 8;
                    openQuantityMenu(player, data.item);
                }
                case "§c-1" -> {
                    if (data.amount > 1) data.amount -= 1;
                    openQuantityMenu(player, data.item);
                }
                case "§c-8" -> {
                    if (data.amount > 1) data.amount = Math.max(1, data.amount - 8);
                    openQuantityMenu(player, data.item);
                }
                case "§7Back" -> openMainMenu(player);
                case "§aConfirm" -> {
                    Material mat = getMaterial(data.item);
                    int price = getItemPrice(data.item) * data.amount;
                    ItemStack stack = new ItemStack(mat, data.amount);
                    player.getInventory().addItem(stack);
                    player.sendMessage("§aYou purchased: " + data.amount + "x " + capitalize(data.item) + " §e($" + price + ")");
                    player.closeInventory();
                    purchaseMap.remove(player.getUniqueId());
                }
            }
        }
    }

    private ItemStack createItem(Material mat, String name) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createItemWithAmount(Material mat, String name, int amount) {
        ItemStack item = new ItemStack(mat, Math.min(amount, mat.getMaxStackSize())); // Avoid >64
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            item.setItemMeta(meta);
        }
        return item;
    }

    private int getItemPrice(String name) {
        return switch (name.toLowerCase()) {
            case "redstone" -> 10;
            case "piston" -> 50;
            case "observer" -> 30;
            case "hopper" -> 20;
            default -> 0;
        };
    }

    private Material getMaterial(String name) {
        return switch (name.toLowerCase()) {
            case "redstone" -> Material.REDSTONE;
            case "piston" -> Material.PISTON;
            case "observer" -> Material.OBSERVER;
            case "hopper" -> Material.HOPPER;
            default -> Material.STONE;
        };
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }

    static class PurchaseData {
        String item;
        int amount;

        public PurchaseData(String item, int amount) {
            this.item = item;
            this.amount = amount;
        }
    }
}
