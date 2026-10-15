package com.frikifest.frikijobs.manager;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;

public class EconomyManager {
    private Economy economy;

    public boolean setupEconomy() {
        if (Bukkit.getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = Bukkit.getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        economy = rsp.getProvider();
        return economy != null;
    }

    public void depositPlayer(OfflinePlayer player, double amount) {
        if (economy != null) {
            economy.depositPlayer(player, amount);
        }
    }

    public boolean withdrawPlayer(OfflinePlayer player, double amount) {
        if (economy != null) {
            return economy.withdrawPlayer(player, amount).transactionSuccess();
        }
        return false;
    }

    public double getBalance(OfflinePlayer player) {
        if (economy != null) {
            return economy.getBalance(player);
        }
        return 0.0;
    }
}
