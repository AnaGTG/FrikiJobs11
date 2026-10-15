package com.frikifest.frikijobs.gui;

import com.frikifest.frikijobs.FrikiJobs;
import com.frikifest.frikijobs.model.Job;
import com.frikifest.frikijobs.model.JobTier;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class JobsGUI {

    public static void openJobsMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, ChatColor.DARK_GREEN + "💼 Selección de Trabajos");

        String currentJob = FrikiJobs.getInstance().getPlayerJobManager().getPlayerJob(player.getUniqueId());

        inv.setItem(10, createJobItem(player, Material.DIAMOND_PICKAXE, "minero", currentJob));
        inv.setItem(11, createJobItem(player, Material.DIAMOND_HOE, "granjero", currentJob));
        inv.setItem(12, createJobItem(player, Material.ENCHANTING_TABLE, "hechicero", currentJob));
        inv.setItem(13, createJobItem(player, Material.FISHING_ROD, "pescador", currentJob));
        inv.setItem(14, createJobItem(player, Material.NETHERITE_SWORD, "guerrero", currentJob));
        inv.setItem(15, createJobItem(player, Material.NETHERITE_AXE, "lenador", currentJob));
        inv.setItem(16, createJobItem(player, Material.BREWING_STAND, "alquimista", currentJob));

        if (currentJob != null) {
            Job job = FrikiJobs.getInstance().getJobManager().getJob(currentJob);
            String jobName = job != null ? job.getName() : currentJob;
            String currentTier = FrikiJobs.getInstance().getPlayerJobManager().getPlayerTier(player.getUniqueId());
            double penalty = FrikiJobs.getInstance().getConfig().getDouble("quit_penalty", 50.0);

            ItemStack leaveItem = new ItemStack(Material.BARRIER);
            ItemMeta meta = leaveItem.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(ChatColor.RED + "❌ Abandonar Trabajo Actual");
                List<String> lore = new ArrayList<>();
                lore.add(ChatColor.GRAY + "Trabajo: " + ChatColor.YELLOW + jobName + " " + ChatColor.AQUA + "(" + currentTier + ")");
                lore.add("");
                lore.add(ChatColor.RED + "⚠ ¡ADVERTENCIA!");
                lore.add(ChatColor.DARK_RED + "Si abandonas tu trabajo, PERDERÁS tu");
                lore.add(ChatColor.DARK_RED + "nivel " + currentTier + " y volverás a Tier I al ingresar.");
                lore.add("");
                lore.add(ChatColor.RED + "Multa por abandonar: $" + penalty);
                lore.add("");
                lore.add(ChatColor.YELLOW + "▶ Haz clic para renunciar");
                meta.setLore(lore);
                leaveItem.setItemMeta(meta);
            }
            inv.setItem(26, leaveItem);
        }

        player.openInventory(inv);
    }

    public static void openJobDetailMenu(Player player, String jobId) {
        Job job = FrikiJobs.getInstance().getJobManager().getJob(jobId);
        if (job == null) return;

        Inventory inv = Bukkit.createInventory(null, 27, ChatColor.DARK_BLUE + "📋 " + job.getName());

        List<String> orderedTierKeys = Arrays.asList("TI", "TII", "TIII", "TIV", "TV", "TVI", "TVII");
        
        int slot = 10;
        for (String tierId : orderedTierKeys) {
            JobTier tier = job.getTiers().get(tierId);
            if (tier == null) continue;
            if (slot > 16) break;

            Material tierMaterial = getMaterialForTier(tierId);

            ItemStack item = new ItemStack(tierMaterial);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(ChatColor.GOLD + "Nivel " + tierId);
                List<String> lore = new ArrayList<>();
                lore.add(ChatColor.GRAY + "Requisito XP: " + ChatColor.AQUA + tier.getRequiredXp());
                lore.add(ChatColor.GRAY + "Recompensa Dinero: " + ChatColor.GREEN + "$" + tier.getRewardMoney());
                lore.add(ChatColor.GRAY + "Recompensa XP: " + ChatColor.YELLOW + tier.getRewardXp() + " XP");
                lore.add("");
                lore.add(ChatColor.YELLOW + "Especialización:");
                lore.add(ChatColor.WHITE + tier.getInfo());
                meta.setLore(lore);
                meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                item.setItemMeta(meta);
            }
            inv.setItem(slot, item);
            slot++;
        }

        ItemStack infoItem = new ItemStack(Material.NETHER_STAR);
        ItemMeta infoMeta = infoItem.getItemMeta();
        if (infoMeta != null) {
            infoMeta.setDisplayName(ChatColor.LIGHT_PURPLE + "⭐ Recompensa de Nivel Máximo");
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + job.getMaxRewardInfo());
            lore.add("");
            lore.add(ChatColor.YELLOW + "¡Completa todos los Tiers para desbloquearlo!");
            infoMeta.setLore(lore);
            infoMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            infoItem.setItemMeta(infoMeta);
        }
        inv.setItem(26, infoItem);

        String currentJob = FrikiJobs.getInstance().getPlayerJobManager().getPlayerJob(player.getUniqueId());
        ItemStack actionItem;
        ItemMeta actionMeta;

        if (jobId.equalsIgnoreCase(currentJob)) {
            actionItem = new ItemStack(Material.LIME_CONCRETE);
            actionMeta = actionItem.getItemMeta();
            if (actionMeta != null) {
                actionMeta.setDisplayName(ChatColor.GREEN + "✔ Ya tienes este trabajo");
                actionItem.setItemMeta(actionMeta);
            }
        } else {
            actionItem = new ItemStack(Material.EMERALD);
            actionMeta = actionItem.getItemMeta();
            if (actionMeta != null) {
                actionMeta.setDisplayName(ChatColor.GOLD + "✨ Unirse a " + job.getName());
                List<String> lore = new ArrayList<>();
                if (currentJob != null) {
                    double penalty = FrikiJobs.getInstance().getConfig().getDouble("quit_penalty", 50.0);
                    String currentTier = FrikiJobs.getInstance().getPlayerJobManager().getPlayerTier(player.getUniqueId());
                    lore.add(ChatColor.RED + "⚠ Tienes un trabajo activo.");
                    lore.add(ChatColor.RED + "Cambiar te costará $" + penalty + " de multa.");
                    lore.add(ChatColor.DARK_RED + "¡Perderás tu nivel " + currentTier + " en tu trabajo actual!");
                }
                lore.add("");
                lore.add(ChatColor.GREEN + "▶ Haz clic para confirmar");
                actionMeta.setLore(lore);
                actionItem.setItemMeta(actionMeta);
            }
        }
        inv.setItem(22, actionItem);

        ItemStack backItem = new ItemStack(Material.ARROW);
        ItemMeta backMeta = backItem.getItemMeta();
        if (backMeta != null) {
            backMeta.setDisplayName(ChatColor.YELLOW + "⬅ Volver al Menú Principal");
            backItem.setItemMeta(backMeta);
        }
        inv.setItem(18, backItem);

        player.openInventory(inv);
    }

    private static Material getMaterialForTier(String tierId) {
        switch (tierId) {
            case "TI": return Material.STONE;
            case "TII": return Material.IRON_INGOT;
            case "TIII": return Material.GOLD_INGOT;
            case "TIV": return Material.DIAMOND;
            case "TV": return Material.NETHERITE_INGOT;
            case "TVI": return Material.EMERALD;
            case "TVII": return Material.NETHER_STAR;
            default: return Material.PAPER;
        }
    }

    private static ItemStack createJobItem(Player player, Material material, String jobId, String currentJob) {
        Job job = FrikiJobs.getInstance().getJobManager().getJob(jobId);
        String name = job != null ? job.getName() : jobId;
        String desc = job != null ? job.getDescription() : "";

        ItemStack item = new ItemStack(material, 1);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.GOLD + "⚒ " + name);
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + desc);
            lore.add("");
            if (jobId.equalsIgnoreCase(currentJob)) {
                String currentTier = FrikiJobs.getInstance().getPlayerJobManager().getPlayerTier(player.getUniqueId());
                lore.add(ChatColor.GREEN + "✔ Trabajo Actual " + ChatColor.AQUA + "(" + currentTier + ")");
            } else {
                lore.add(ChatColor.YELLOW + "Estado: " + ChatColor.GRAY + "No activo");
            }
            lore.add("");
            lore.add(ChatColor.AQUA + "▶ Haz clic para ver especificaciones y Tiers");
            meta.setLore(lore);
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            item.setItemMeta(meta);
        }
        return item;
    }
}
