package com.frikifest.frikijobs.manager;

import com.frikifest.frikijobs.FrikiJobs;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerJobManager {
    private final FrikiJobs plugin;
    private File file;
    private FileConfiguration config;
    
    private final Map<UUID, String> activeJobs = new HashMap<>();
    private final Map<String, String> jobTiers = new HashMap<>();
    private final Map<String, Double> jobEarnings = new HashMap<>();

    public PlayerJobManager(FrikiJobs plugin) {
        this.plugin = plugin;
        createFile();
    }

    private void createFile() {
        file = new File(plugin.getDataFolder(), "player_jobs.yml");
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            try {
                file.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().warning("No se pudo crear player_jobs.yml");
            }
        }
        config = YamlConfiguration.loadConfiguration(file);
        for (String key : config.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(key);
                if (config.isConfigurationSection(key)) {
                    String active = config.getString(key + ".active_job");
                    if (active != null) {
                        activeJobs.put(uuid, active.toLowerCase());
                    }
                    
                    org.bukkit.configuration.ConfigurationSection jobsSec = config.getConfigurationSection(key + ".jobs_data");
                    if (jobsSec != null) {
                        for (String jobId : jobsSec.getKeys(false)) {
                            String prefix = key + ".jobs_data." + jobId;
                            String tier = config.getString(prefix + ".tier", "TI");
                            double earnings = config.getDouble(prefix + ".earnings", 0.0);
                            
                            jobTiers.put(uuid.toString() + "." + jobId.toLowerCase(), tier);
                            jobEarnings.put(uuid.toString() + "." + jobId.toLowerCase(), earnings);
                        }
                    } else {
                        String oldJob = config.getString(key + ".job");
                        if (oldJob != null) {
                            activeJobs.put(uuid, oldJob.toLowerCase());
                            String oldTier = config.getString(key + ".tier", "TI");
                            double oldEarn = config.getDouble(key + ".earnings", 0.0);
                            jobTiers.put(uuid.toString() + "." + oldJob.toLowerCase(), oldTier);
                            jobEarnings.put(uuid.toString() + "." + oldJob.toLowerCase(), oldEarn);
                        }
                    }
                } else {
                    String oldJob = config.getString(key);
                    if (oldJob != null) {
                        activeJobs.put(uuid, oldJob.toLowerCase());
                        jobTiers.put(uuid.toString() + "." + oldJob.toLowerCase(), "TI");
                        jobEarnings.put(uuid.toString() + "." + oldJob.toLowerCase(), 0.0);
                    }
                }
            } catch (IllegalArgumentException ignored) {}
        }
    }

    public String getPlayerJob(UUID uuid) {
        return activeJobs.get(uuid);
    }

    public String getPlayerTier(UUID uuid) {
        String job = activeJobs.get(uuid);
        if (job == null) return "TI";
        return jobTiers.getOrDefault(uuid.toString() + "." + job, "TI");
    }

    public double getPlayerEarnings(UUID uuid) {
        String job = activeJobs.get(uuid);
        if (job == null) return 0.0;
        return jobEarnings.getOrDefault(uuid.toString() + "." + job, 0.0);
    }

    public double addPlayerEarnings(UUID uuid, double amount) {
        String job = activeJobs.get(uuid);
        if (job == null) return 0.0;
        
        String keyMap = uuid.toString() + "." + job;
        double current = jobEarnings.getOrDefault(keyMap, 0.0);
        double updated = current + amount;
        jobEarnings.put(keyMap, updated);

        config.set(uuid.toString() + ".jobs_data." + job + ".earnings", updated);
        save();
        return updated;
    }

    public void setPlayerJob(UUID uuid, String jobId) {
        if (jobId == null) {
            activeJobs.remove(uuid);
            config.set(uuid.toString() + ".active_job", null);
        } else {
            String formattedJob = jobId.toLowerCase();
            activeJobs.put(uuid, formattedJob);
            config.set(uuid.toString() + ".active_job", formattedJob);
            
            // REQUISITO NUEVO: Cada vez que un jugador se une a un trabajo (incluso si ya lo había tenido antes),
            // su tier se reinicia a Tier I y sus ganancias a 0.0.
            String keyMap = uuid.toString() + "." + formattedJob;
            jobTiers.put(keyMap, "TI");
            jobEarnings.put(keyMap, 0.0);
            config.set(uuid.toString() + ".jobs_data." + formattedJob + ".tier", "TI");
            config.set(uuid.toString() + ".jobs_data." + formattedJob + ".earnings", 0.0);
        }
        save();
    }

    public void setPlayerTier(UUID uuid, String tierId) {
        String job = activeJobs.get(uuid);
        if (job == null) return;
        
        String formattedTier = tierId.toUpperCase();
        String keyMap = uuid.toString() + "." + job;
        jobTiers.put(keyMap, formattedTier);
        config.set(uuid.toString() + ".jobs_data." + job + ".tier", formattedTier);
        save();
    }

    public void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning("No se pudo guardar player_jobs.yml");
        }
    }
}
