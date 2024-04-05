package ru.rncyplugin.rewardexp;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RewardingExperience extends JavaPlugin implements Listener {

    private final Map<UUID, Long> afkPlayers = new HashMap<>();
    private final Map<UUID, BukkitRunnable> tasks = new HashMap<>();
    private int expAmount;
    private int afkTimeInSeconds;
    private int rewardIntervalInSeconds; // Новое поле для хранения интервала выдачи опыта
    private boolean commandEnabled;

    @Override
    public void onEnable() {
        // Создание папки и конфигурационного файла при первом запуске
        saveDefaultConfig();

        // Загрузка конфигурации при включении плагина
        loadConfig();

        getServer().getPluginManager().registerEvents(this, this);
        getCommand("rwexpad").setExecutor(new Command1());
        getLogger().info("RewardingExperience включен!");
    }

    private void loadConfig() {
        // Загрузка параметров из конфигурационного файла
        FileConfiguration config = getConfig();
        expAmount = config.getInt("expAmount");
        afkTimeInSeconds = config.getInt("afkTimeInSeconds");
        rewardIntervalInSeconds = config.getInt("rewardIntervalInSeconds"); // Загружаем новый параметр
        commandEnabled = config.getBoolean("commandEnabled");
    }

    public class Command1 implements CommandExecutor {
        @Override
        public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
            if (!commandEnabled) {
                sender.sendMessage("§e§lRWExp §7> §fКоманда отключена, негодяй!");
                return true;
            }
            if (!(sender instanceof Player)) {
                sender.sendMessage("§cКоманда доступна только игрокам!");
                return true;
            }

            Player player = (Player) sender;
            if (cmd.getName().equalsIgnoreCase("rwexpad")) {
                // Выдаем игроку опыт
                player.giveExp(expAmount);
                player.sendMessage("§e§lRWExp §7> §aНаграда получена заранее");
                return true;
            }

            return false;
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        afkPlayers.put(playerId, System.currentTimeMillis());
        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                addExperience(player);
            }
        };
        task.runTaskLater(this, rewardIntervalInSeconds * 20L);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID playerId = event.getPlayer().getUniqueId();
        if (tasks.containsKey(playerId)) {
            tasks.get(playerId).cancel();
            tasks.remove(playerId);
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();

        if (!afkPlayers.containsKey(playerId)) {
            afkPlayers.put(playerId, System.currentTimeMillis());
            return;
        }

        long lastMoveTime = afkPlayers.get(playerId);
        long currentTime = System.currentTimeMillis();

        if (currentTime - lastMoveTime > afkTimeInSeconds * 1000) {
            // Игрок афк на слишком долгое время
            if (tasks.containsKey(playerId)) {
                BukkitRunnable task = tasks.get(playerId);
                if (task != null) {
                    task.cancel();
                }
                tasks.remove(playerId);
            }
            afkPlayers.remove(playerId);
            player.sendMessage("§e§lRWExp §7> §fТы вышел из AFK и снова можешь получать опыт :)");
        }
    }

    public void addExperience(Player player) {
        UUID playerId = player.getUniqueId();
        if (tasks.containsKey(playerId)) {
            tasks.get(playerId).cancel();
        }

        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                if (player.isOnline()) {
                    player.giveExp(expAmount);
                } else {
                    this.cancel();
                    tasks.remove(playerId);
                }
            }
        };

        task.runTaskTimer(this, 0L, rewardIntervalInSeconds * 20L); // Умножаем на 20, чтобы перевести секунды в тики
        tasks.put(playerId, task);
    }

    @Override
    public void onDisable() {
        getLogger().info("RewardingExperience выключен!");
    }
}