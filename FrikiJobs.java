package com.frikifest.frikijobs;

import com.frikifest.frikijobs.gui.JobsGUI;
import com.frikifest.frikijobs.listener.GUIListener;
import com.frikifest.frikijobs.listener.JobActionListener;
import com.frikifest.frikijobs.manager.EconomyManager;
import com.frikifest.frikijobs.manager.JobManager;
import com.frikifest.frikijobs.manager.MessageManager;
import com.frikifest.frikijobs.manager.PlayerJobManager;
import com.frikifest.frikijobs.model.Job;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.List;

public final class FrikiJobs extends JavaPlugin {

    private static FrikiJobs instance;
    private JobManager jobManager;
    private EconomyManager economyManager;
    private PlayerJobManager playerJobManager;
    private MessageManager messageManager;

    private static final List<String> VALID_TIERS = Arrays.asList("TI", "TII", "TIII", "TIV", "TV", "TVI", "TVII");

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        this.messageManager = new MessageManager(this);

        this.economyManager = new EconomyManager();
        if (!economyManager.setupEconomy()) {
            getLogger().warning("Vault no encontrado. La integración económica se deshabilitó.");
        }

        this.jobManager = new JobManager(this);
        jobManager.loadJobs();

        this.playerJobManager = new PlayerJobManager(this);

        getServer().getPluginManager().registerEvents(new JobActionListener(this), this);
        getServer().getPluginManager().registerEvents(new GUIListener(), this);

        getLogger().info("FrikiJobs inicializado correctamente con mensajes configurables (messages.yml).");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("jobs")) {
            if (args.length > 0) {
                String subCommand = args[0].toLowerCase();

                if (subCommand.equals("info") || subCommand.equals("stats") || subCommand.equals("status")) {
                    if (!(sender instanceof Player player)) {
                        sender.sendMessage(messageManager.getMessage("must_be_player"));
                        return true;
                    }

                    String currentJobId = playerJobManager.getPlayerJob(player.getUniqueId());
                    if (currentJobId == null) {
                        for (String line : messageManager.getMessage("job_info_none").split("\n")) {
                            player.sendMessage(line);
                        }
                        return true;
                    }

                    Job job = jobManager.getJob(currentJobId);
                    String jobName = job != null ? job.getName() : currentJobId;
                    String currentTier = playerJobManager.getPlayerTier(player.getUniqueId());

                    player.sendMessage(messageManager.getMessage("job_info_header"));
                    player.sendMessage(messageManager.getMessage("job_info_name").replace("{job_name}", jobName));
                    player.sendMessage(messageManager.getMessage("job_info_tier").replace("{tier}", currentTier));
                    player.sendMessage(messageManager.getMessage("job_info_status"));
                    player.sendMessage(messageManager.getMessage("job_info_footer"));
                    return true;
                }

                if (subCommand.equals("reload")) {
                    if (!sender.hasPermission("jobs.admin")) {
                        sender.sendMessage(messageManager.getMessage("no_permission"));
                        return true;
                    }

                    reloadConfig();
                    messageManager.loadMessages();
                    jobManager.loadJobs();
                    sender.sendMessage(messageManager.getMessage("config_reloaded"));
                    return true;
                }

                if (!sender.hasPermission("jobs.admin")) {
                    sender.sendMessage(messageManager.getMessage("no_permission"));
                    return true;
                }

                if (subCommand.equals("settier") || subCommand.equals("tier")) {
                    if (args.length < 3) {
                        sender.sendMessage(ChatColor.RED + "Uso correcto: /jobs settier <jugador> <tier>");
                        return true;
                    }

                    OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
                    if (target == null || (!target.hasPlayedBefore() && !target.isOnline())) {
                        sender.sendMessage(messageManager.getMessage("player_not_found").replace("{player}", args[1]));
                        return true;
                    }

                    String currentJob = playerJobManager.getPlayerJob(target.getUniqueId());
                    if (currentJob == null) {
                        sender.sendMessage(messageManager.getMessage("no_job_active").replace("{player}", target.getName()));
                        return true;
                    }

                    String targetTier = args[2].toUpperCase();
                    if (!VALID_TIERS.contains(targetTier)) {
                        sender.sendMessage(messageManager.getMessage("invalid_tier").replace("{tier}", args[2]));
                        return true;
                    }

                    playerJobManager.setPlayerTier(target.getUniqueId(), targetTier);
                    sender.sendMessage(ChatColor.GREEN + "[FrikiJobs] Se ha establecido el Tier de " + target.getName() + " a " + targetTier + ".");

                    if (target.isOnline()) {
                        ((Player) target).sendMessage(ChatColor.GOLD + "[FrikiJobs] Un administrador ha cambiado tu nivel de empleo a " + targetTier + ".");
                    }
                    return true;
                }

                if (subCommand.equals("set")) {
                    if (args.length < 3) {
                        sender.sendMessage(ChatColor.RED + "Uso correcto: /jobs set <jugador> <trabajo>");
                        return true;
                    }

                    OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
                    if (target == null || (!target.hasPlayedBefore() && !target.isOnline())) {
                        sender.sendMessage(messageManager.getMessage("player_not_found").replace("{player}", args[1]));
                        return true;
                    }

                    String jobId = args[2].toLowerCase();
                    Job job = jobManager.getJob(jobId);
                    if (job == null) {
                        sender.sendMessage(messageManager.getMessage("job_not_found").replace("{job}", args[2]));
                        return true;
                    }

                    playerJobManager.setPlayerJob(target.getUniqueId(), jobId);
                    sender.sendMessage(ChatColor.GREEN + "[FrikiJobs] Se ha asignado el trabajo de " + job.getName() + " a " + target.getName() + ".");

                    if (target.isOnline()) {
                        ((Player) target).sendMessage(ChatColor.GOLD + "[FrikiJobs] Un administrador te ha asignado el trabajo de " + job.getName() + ".");
                    }
                    return true;
                }

                if (subCommand.equals("remove") || subCommand.equals("clear")) {
                    if (args.length < 2) {
                        sender.sendMessage(ChatColor.RED + "Uso correcto: /jobs remove <jugador>");
                        return true;
                    }

                    OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
                    if (target == null || (!target.hasPlayedBefore() && !target.isOnline())) {
                        sender.sendMessage(messageManager.getMessage("player_not_found").replace("{player}", args[1]));
                        return true;
                    }

                    String currentJob = playerJobManager.getPlayerJob(target.getUniqueId());
                    if (currentJob == null) {
                        sender.sendMessage(messageManager.getMessage("no_job_active").replace("{player}", target.getName()));
                        return true;
                    }

                    playerJobManager.setPlayerJob(target.getUniqueId(), null);
                    sender.sendMessage(ChatColor.GREEN + "[FrikiJobs] Se le ha retirado el trabajo a " + target.getName() + ".");

                    if (target.isOnline()) {
                        ((Player) target).sendMessage(ChatColor.RED + "[FrikiJobs] Un administrador te ha retirado tu trabajo actual.");
                    }
                    return true;
                }

                sender.sendMessage(ChatColor.RED + "Comando desconocido. Usa /jobs info, reload, set, settier o remove.");
                return true;
            }

            if (sender instanceof Player player) {
                JobsGUI.openJobsMenu(player);
                return true;
            } else {
                sender.sendMessage("Uso en consola: /jobs reload | /jobs set | /jobs settier | /jobs remove");
            }
        }
        return false;
    }

    public static FrikiJobs getInstance() {
        return instance;
    }

    public JobManager getJobManager() {
        return jobManager;
    }

    public EconomyManager getEconomyManager() {
        return economyManager;
    }

    public PlayerJobManager getPlayerJobManager() {
        return playerJobManager;
    }

    public MessageManager getMessageManager() {
        return messageManager;
    }
}
