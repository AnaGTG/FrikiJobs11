package com.frikifest.frikijobs.manager;

import com.frikifest.frikijobs.FrikiJobs;
import com.frikifest.frikijobs.model.Job;
import com.frikifest.frikijobs.model.JobTier;
import org.bukkit.configuration.ConfigurationSection;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JobManager {
    private final FrikiJobs plugin;
    private final Map<String, Job> jobs = new HashMap<>();

    public JobManager(FrikiJobs plugin) {
        this.plugin = plugin;
    }

    public void loadJobs() {
        jobs.clear();
        ConfigurationSection jobsSection = plugin.getConfig().getConfigurationSection("jobs");
        if (jobsSection == null) return;

        for (String jobId : jobsSection.getKeys(false)) {
            ConfigurationSection jobSec = jobsSection.getConfigurationSection(jobId);
            if (jobSec == null) continue;

            String name = jobSec.getString("name", jobId);
            String description = jobSec.getString("description", "");
            String maxRewardInfo = jobSec.getString("max_reward_info", "");
            Map<String, JobTier> tiers = new HashMap<>();

            ConfigurationSection tiersSec = jobSec.getConfigurationSection("tiers");
            if (tiersSec != null) {
                for (String tierId : tiersSec.getKeys(false)) {
                    ConfigurationSection tierSec = tiersSec.getConfigurationSection(tierId);
                    if (tierSec == null) continue;

                    int requiredXp = tierSec.getInt("required_xp", 0);
                    double rewardMoney = tierSec.getDouble("reward_money", 0.0);
                    int rewardXp = tierSec.getInt("reward_xp", 1);
                    List<String> materials = tierSec.getStringList("materials");
                    String info = tierSec.getString("info", "");

                    tiers.put(tierId, new JobTier(requiredXp, rewardMoney, rewardXp, materials, info));
                }
            }

            jobs.put(jobId.toLowerCase(), new Job(jobId.toLowerCase(), name, description, maxRewardInfo, tiers));
        }
    }

    public Job getJob(String jobId) {
        return jobs.get(jobId.toLowerCase());
    }

    public Map<String, Job> getJobs() {
        return jobs;
    }
}
