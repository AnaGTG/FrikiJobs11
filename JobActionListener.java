package com.frikifest.frikijobs.listener;

import com.frikifest.frikijobs.FrikiJobs;
import com.frikifest.frikijobs.model.Job;
import com.frikifest.frikijobs.model.JobTier;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.PotionMeta;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class JobActionListener implements Listener {

    private final FrikiJobs plugin;
    private final Set<String> playerPlacedBlocks = new HashSet<>();

    public JobActionListener(FrikiJobs plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        Block block = event.getBlock();
        String blockKey = block.getWorld().getName() + "," + block.getX() + "," + block.getY() + "," + block.getZ();
        playerPlacedBlocks.add(blockKey);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        String blockKey = block.getWorld().getName() + "," + block.getX() + "," + block.getY() + "," + block.getZ();

        if (playerPlacedBlocks.contains(blockKey)) {
            playerPlacedBlocks.remove(blockKey);
            return;
        }

        String currentJob = plugin.getPlayerJobManager().getPlayerJob(player.getUniqueId());
        if (currentJob == null) return;

        Job job = plugin.getJobManager().getJob(currentJob);
        if (job == null) return;

        if (currentJob.equals("minero") || currentJob.equals("lenador") || currentJob.equals("granjero")) {
            String tierKey = plugin.getPlayerJobManager().getPlayerTier(player.getUniqueId());
            JobTier tier = job.getTiers().get(tierKey);

            if (tier != null && tier.getMaterials() != null && tier.getMaterials().contains(block.getType().name())) {
                payReward(player, job.getName(), tierKey, tier.getRewardMoney(), tier.getRewardXp());
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        Player killer = entity.getKiller();
        if (killer == null) return;

        String currentJob = plugin.getPlayerJobManager().getPlayerJob(killer.getUniqueId());
        if (currentJob == null || !currentJob.equals("guerrero")) return;

        Job job = plugin.getJobManager().getJob("guerrero");
        if (job == null) return;

        String tierKey = plugin.getPlayerJobManager().getPlayerTier(killer.getUniqueId());
        JobTier tier = job.getTiers().get(tierKey);

        if (tier != null && tier.getMaterials() != null) {
            String entityTypeName = entity.getType().name();
            if (tier.getMaterials().contains(entityTypeName)) {
                payReward(killer, job.getName(), tierKey, tier.getRewardMoney(), tier.getRewardXp());
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        
        InventoryType invType = event.getInventory().getType();
        String currentJob = plugin.getPlayerJobManager().getPlayerJob(player.getUniqueId());
        if (currentJob == null) return;

        if (invType == InventoryType.BREWING && currentJob.equals("alquimista")) {
            int rawSlot = event.getRawSlot();
            if (rawSlot != 0 && rawSlot != 1 && rawSlot != 2) return;

            ItemStack item = event.getCurrentItem();
            if (item == null || (item.getType() != Material.POTION && item.getType() != Material.SPLASH_POTION && item.getType() != Material.LINGERING_POTION)) return;

            PotionMeta meta = (PotionMeta) item.getItemMeta();
            if (meta == null || (!meta.hasCustomEffects() && meta.getBasePotionData().getType().name().equals("WATER"))) {
                return;
            }

            String tierKey = plugin.getPlayerJobManager().getPlayerTier(player.getUniqueId());
            Job job = plugin.getJobManager().getJob("alquimista");
            JobTier tier = job != null ? job.getTiers().get(tierKey) : null;

            if (tier != null && job != null) {
                payReward(player, job.getName(), tierKey, tier.getRewardMoney(), tier.getRewardXp());
            }
        }

        if ((invType == InventoryType.ENCHANTING || invType == InventoryType.ANVIL) && currentJob.equals("hechicero")) {
            int resultSlot = invType == InventoryType.ENCHANTING ? 0 : 2;
            if (event.getRawSlot() != resultSlot) return;

            ItemStack item = event.getCurrentItem();
            if (item == null || item.getType() == Material.AIR) return;

            boolean hasEnchants = item.hasItemMeta() && 
                (item.getItemMeta().hasEnchants() || 
                (item.getType() == Material.ENCHANTED_BOOK && ((EnchantmentStorageMeta) item.getItemMeta()).hasStoredEnchants()));

            if (!hasEnchants) return;

            String tierKey = plugin.getPlayerJobManager().getPlayerTier(player.getUniqueId());
            Job job = plugin.getJobManager().getJob("hechicero");
            JobTier tier = job != null ? job.getTiers().get(tierKey) : null;

            if (tier != null && job != null) {
                payReward(player, job.getName(), tierKey, tier.getRewardMoney(), tier.getRewardXp());
            }
        }
    }

    private void payReward(Player player, String jobName, String tierKey, double money, int xp) {
        if (plugin.getEconomyManager() != null) {
            plugin.getEconomyManager().depositPlayer(player, money);
        }

        String actionMsg = plugin.getMessageManager().getMessage("actionbar_reward")
                .replace("{job_name}", jobName)
                .replace("{tier}", tierKey)
                .replace("{money}", String.format("%.2f", money))
                .replace("{xp}", String.valueOf(xp));

        player.sendActionBar(net.kyori.adventure.text.Component.text(actionMsg));

        double prevEarnings = plugin.getPlayerJobManager().getPlayerEarnings(player.getUniqueId());
        double newEarnings = plugin.getPlayerJobManager().addPlayerEarnings(player.getUniqueId(), money);

        double reachedMilestone = checkMilestone(prevEarnings, newEarnings);
        if (reachedMilestone > 0) {
            List<String> lines = plugin.getMessageManager().getMessageList("milestone_message");
            for (String line : lines) {
                player.sendMessage(line.replace("{amount}", String.format("%.0f", reachedMilestone)));
            }
        }
    }

    private double checkMilestone(double prev, double current) {
        for (int m = 100; m <= 1000; m += 100) {
            if (prev < m && current >= m) {
                return m;
            }
        }
        for (int m = 2000; m <= 10000; m += 1000) {
            if (prev < m && current >= m) {
                return m;
            }
        }
        for (int m = 15000; m <= 1000000; m += 5000) {
            if (prev < m && current >= m) {
                return m;
            }
        }
        return -1;
    }
}
