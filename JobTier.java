package com.frikifest.frikijobs.model;

import java.util.List;

public class JobTier {
    private final int requiredXp;
    private final double rewardMoney;
    private final int rewardXp;
    private final List<String> materials;
    private final String info;

    public JobTier(int requiredXp, double rewardMoney, int rewardXp, List<String> materials, String info) {
        this.requiredXp = requiredXp;
        this.rewardMoney = rewardMoney;
        this.rewardXp = rewardXp;
        this.materials = materials;
        this.info = info;
    }

    public int getRequiredXp() {
        return requiredXp;
    }

    public double getRewardMoney() {
        return rewardMoney;
    }

    public int getRewardXp() {
        return rewardXp;
    }

    public List<String> getMaterials() {
        return materials;
    }

    public String getInfo() {
        return info;
    }
}
