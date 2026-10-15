package com.frikifest.frikijobs.manager;

import com.frikifest.frikijobs.FrikiJobs;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MessageManager {

    private final FrikiJobs plugin;
    private File file;
    private FileConfiguration config;

    public MessageManager(FrikiJobs plugin) {
        this.plugin = plugin;
        loadMessages();
    }

    public void loadMessages() {
        file = new File(plugin.getDataFolder(), "messages.yml");
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            plugin.saveResource("messages.yml", false);
        }
        config = YamlConfiguration.loadConfiguration(file);
    }

    public String getMessage(String path) {
        String msg = config.getString(path, "");
        return ChatColor.translateAlternateColorCodes('&', msg);
    }

    public List<String> getMessageList(String path) {
        List<String> raw = config.getStringList(path);
        List<String> formatted = new ArrayList<>();
        for (String line : raw) {
            formatted.add(ChatColor.translateAlternateColorCodes('&', line));
        }
        return formatted;
    }
}
