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
import org.bukkit.World;
import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;

// Imports for Apache Commons Compress
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.xz.XZCompressorOutputStream;

public class BackupTask implements Runnable {

    private final AutoSaveBackup plugin;
    private final File backupDir;
    private final int maxBackups;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");

    public BackupTask(AutoSaveBackup plugin, File backupDir, int maxBackups) {
        this.plugin = plugin;
        this.backupDir = backupDir;
        this.maxBackups = maxBackups;
    }

    @Override
    public void run() {
        plugin.getLogger().log(Level.INFO, "Starting world backup...");

        List<World> worlds = Bukkit.getWorlds();
        int worldsBackedUp = 0;

        for (World world : worlds) {
            try {
                plugin.getLogger().log(Level.INFO, "Saving world '" + world.getName() + "'...");
                try {
                    Bukkit.getScheduler().callSyncMethod(plugin, (Callable<Void>) () -> {
                        world.save();
                        return null;
                    }).get();
                    plugin.getLogger().log(Level.INFO, "World '" + world.getName() + "' saved.");
                } catch (InterruptedException | ExecutionException e) {
                    plugin.getLogger().log(Level.SEVERE, "Failed to save world '" + world.getName() + "': " + e.getMessage(), e);
                    continue;
                }

                File worldFolder = world.getWorldFolder();
                if (!worldFolder.exists() || !worldFolder.isDirectory()) {
                    plugin.getLogger().log(Level.WARNING, "World folder not found for '" + world.getName() + "'. Skipping backup for this world.");
                    continue;
                }

                String backupFileName = world.getName() + "-" + dateFormat.format(new Date()) + ".tar.xz";
                File backupFile = new File(backupDir, backupFileName);

                createTarXzArchive(worldFolder.toPath(), backupFile.toPath());
                worldsBackedUp++;
                plugin.getLogger().log(Level.INFO, "Backup for world '" + world.getName() + "' created: " + backupFileName);

            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Error backing up world '" + world.getName() + "': " + e.getMessage(), e);
            }
        }

        if (worldsBackedUp > 0) {
            plugin.getLogger().log(Level.INFO, "Backup complete. " + worldsBackedUp + " worlds backed up.");
        } else {
            plugin.getLogger().log(Level.WARNING, "No worlds were backed up. Check logs for errors.");
        }

        cleanOldBackups();
    }

    private void createTarXzArchive(Path sourceFolderPath, Path tarXzFilePath) throws IOException {
        try (OutputStream fos = Files.newOutputStream(tarXzFilePath);
             XZCompressorOutputStream xzOs = new XZCompressorOutputStream(fos);
             TarArchiveOutputStream tarOs = new TarArchiveOutputStream(xzOs)) {

            tarOs.setLongFileMode(TarArchiveOutputStream.LONGFILE_POSIX); // Allows long file names

            Files.walkFileTree(sourceFolderPath, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) throws IOException {
                    if (attributes.isRegularFile()) {
                        Path relativePath = sourceFolderPath.relativize(file);
                        TarArchiveEntry entry = new TarArchiveEntry(file.toFile(), relativePath.toString());
                        tarOs.putArchiveEntry(entry);
                        Files.copy(file, tarOs);
                        tarOs.closeArchiveEntry();
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    if (!sourceFolderPath.equals(dir)) {
                        Path relativePath = sourceFolderPath.relativize(dir);
                        TarArchiveEntry entry = new TarArchiveEntry(dir.toFile(), relativePath.toString() + "/");
                        tarOs.putArchiveEntry(entry);
                        tarOs.closeArchiveEntry();
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        }
    }

    /**
     * Deletes the oldest backups if their count exceeds maxBackups.
     */
    private void cleanOldBackups() {
        File[] backupFiles = backupDir.listFiles((dir, name) -> name.endsWith(".tar.xz"));
        if (backupFiles == null || backupFiles.length <= maxBackups) {
            return;
        }

        Arrays.sort(backupFiles, Comparator.comparingLong(File::lastModified));

        for (int i = 0; i < backupFiles.length - maxBackups; i++) {
            File oldBackup = backupFiles[i];
            if (oldBackup.delete()) {
                plugin.getLogger().log(Level.INFO, "Deleted old backup: " + oldBackup.getName());
            } else {
                plugin.getLogger().log(Level.WARNING, "Failed to delete old backup: " + oldBackup.getName());
            }
        }
    }
}