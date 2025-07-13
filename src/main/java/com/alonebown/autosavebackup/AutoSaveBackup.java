/**  Copyrighting (C) 2024 by AloneBown
* This code is free software; 
* you can redistribute it and/or modify it under the terms of the license
* This code is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
* without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.->
*  
* See GNU General Public License v3.0 for more information.
* You should receive a copy of it with code or visit https://www.gnu.org/licenses/gpl-3.0.html
* (do not remove this notice)
*/

package com.alonebown.autosavebackup;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;

public class AutoSaveBackup extends JavaPlugin {

    private Map<String, String> messages = new HashMap<>();

    @Override
    public void onEnable() {
        // Load plugin configuration
        File configFile = new File(getDataFolder(), "config.yml");
        if (!configFile.exists() || configFile.length() == 0) {
            saveResource("config.yml", true);
            reloadConfig();
        } else {
            reloadConfig();
        }
        
        loadMessages();

        // Get settings from configuration
        int backupIntervalMinutes = getConfig().getInt("backup_interval_minutes", 60);
        int maxBackups = getConfig().getInt("max_backups", 5);
        String backupDirectoryPath = "backups";
        int autosaveIntervalMinutes = getConfig().getInt("autosave_interval_minutes", 10);

        // Validate settings
        if (backupIntervalMinutes <= 0 || maxBackups <= 0 || autosaveIntervalMinutes <= 0) {
            getLogger().log(Level.SEVERE, "Invalid settings in config.yml!");
            getLogger().log(Level.SEVERE, "Please check the console for errors.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Create backup directory if it doesn't exist
        File backupDir = new File(getDataFolder(), backupDirectoryPath);
        if (!backupDir.exists()) {
            if (!backupDir.mkdirs()) {
                getLogger().log(Level.SEVERE, "Failed to create backup directory: " + backupDirectoryPath);
                getServer().getPluginManager().disablePlugin(this);
                return;
            }
        }

        getLogger().log(Level.INFO, ChatColor.GREEN + "////-----\\\\");
        getLogger().log(Level.INFO, "AutoSaveBackup enabled!");
        getLogger().log(Level.INFO, "Backup interval set to " + backupIntervalMinutes + " minutes.");
        getLogger().log(Level.INFO, "Max backups set to " + maxBackups + ".");
        getLogger().log(Level.INFO, "Backup directory set to: " + backupDirectoryPath);
        getLogger().log(Level.INFO, "Autosave interval set to " + autosaveIntervalMinutes + " minutes.");
        getLogger().log(Level.INFO, ChatColor.GREEN + "\\\\-----////");

        // Schedule backup task (asynchronous, but world.save() inside will be synchronous)
        Bukkit.getScheduler().runTaskTimerAsynchronously(this,
                new BackupTask(this, backupDir, maxBackups),
                backupIntervalMinutes * 20L * 60L, // Initial delay
                backupIntervalMinutes * 20L * 60L // Interval
        );

        // Schedule autosave task (synchronous)
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            getLogger().log(Level.INFO, "Starting server autosave...");
            Bukkit.broadcastMessage(getMessage("autosave_start_broadcast"));
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "save-all");
            getLogger().log(Level.INFO, "Server autosave complete.");
            Bukkit.broadcastMessage(getMessage("autosave_complete_broadcast"));
        }, autosaveIntervalMinutes * 20L * 60L, autosaveIntervalMinutes * 20L * 60L);
    }
    @Override
    public void onDisable() {
        Bukkit.getScheduler().cancelTasks(this);
        getLogger().log(Level.INFO, "AutoSaveBackup disabled.");
    }
    public void loadMessages() {
        FileConfiguration config = getConfig();
        ConfigurationSection messagesSection = config.getConfigurationSection("messages");

        if (messagesSection == null) {
            getLogger().log(Level.SEVERE, "Messages section not found in config.yml! Plugin may not function correctly.");
            return;
        }

        messages.clear();
        for (String key : messagesSection.getKeys(false)) {
            messages.put(key, ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(messagesSection.getString(key))));
        }
    }
    public String getMessage(String key) {
        return messages.getOrDefault(key, ChatColor.RED + "Message not found: " + key);
    }
}