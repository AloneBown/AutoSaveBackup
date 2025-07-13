# AutoSaveBackup Plugin

AutoSaveBackup is a lightweight Paper/Spigot plugin designed to automatically save your Minecraft server worlds and create compressed backups at configurable intervals. It also includes an autosave feature to ensure your server data is regularly saved to disk, preventing data loss.

## Features

* **Configurable Backup Intervals:** Set how often your server worlds are backed up.
* **Configurable Autosave Intervals:** Define how frequently the server performs a full `save-all` command.
* **Maximum Backup Limit:** Automatically manage disk space by setting a limit on the number of backups to retain. Oldest backups are removed when the limit is exceeded.
* **Efficient Compression:** Utilizes `TAR.XZ` format for efficient and lossless compression of your world data, saving disk space.
* **Player Broadcasts:** Notifies players in chat when an autosave is initiated and completed.
* **Console-Only Logs:** Detailed technical logs are displayed only in the server console.
* **Easy Localization:** All broadcast messages can be easily customized directly in the `config.yml` file.

## Installation

1.  **Download the Plugin:** Obtain the `AutoSaveBackup-1.0.0.jar` from the [releases page](https://github.com/alonebown/AutoSaveBackup/releases) (once available).
2.  **Place on Server:** Put the `AutoSaveBackup-1.0.0.jar` file into your server's `plugins/` folder.
3.  **Start Server:** Start your Paper/Spigot server. The plugin will generate a `config.yml` file in `plugins/AutoSaveBackup/`.
4.  **Configure:** Edit the `config.yml` file to adjust intervals, backup directory, and messages as needed.
5.  **Restart Server:** Restart your server to apply the configuration changes.

## Configuration (`config.yml`)

```yaml
# Interval between backups in minutes
backup_interval_minutes: 60

# Maximum number of backups to keep
max_backups: 5

# Server autosave interval in minutes
autosave_interval_minutes: 10

# Plugin messages.
messages:
  autosave_start_broadcast: "&e[AutoSaveBackup] Server is saving all worlds..."
  autosave_complete_broadcast: "&a[AutoSaveBackup] Server autosave complete!"
```

## Building from Source

If you wish to build the plugin from its source code:

1.  **Clone the Repository:**
    ```bash
    git clone [https://github.com/alonebown/AutoSaveBackup.git](https://github.com/alonebown/AutoSaveBackup.git)
    cd AutoSaveBackup
    ```
2.  **Compile with Maven:**
    ```bash
    mvn clean package
    ```
    The compiled `.jar` file will be located in the `target/` directory.

## Support

If you encounter any issues or have suggestions, please open an issue on the [GitHub repository](https://github.com/alonebown/AutoSaveBackup/issues).

---
**Author:** AloneBown
**Version:** 1.0.0
