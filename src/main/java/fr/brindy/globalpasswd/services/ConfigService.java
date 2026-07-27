package fr.brindy.globalpasswd.services;

import fr.brindy.globalpasswd.utils.exceptions.DirectoryCreationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

public class ConfigService {
    private SessionService sessionService;
    private final FileConfiguration config;
    private final File configFile;
    private final JavaPlugin plugin;

    public ConfigService(FileConfiguration config, File configFile, JavaPlugin plugin) {
        this.config = config;
        this.configFile = configFile;
        this.plugin = plugin;
    }

    public void setSessionService(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    public boolean getEnabled() {
        return config.getBoolean("enabled");
    }

    public void setEnabled(boolean isEnabled) {
        config.set("enabled", isEnabled);
        saveConfig();
    }

    public long getTimeoutTime() {
        return config.getLong("timeout-duration");
    }

    public boolean getSessionsEnabled() {
        return config.getBoolean("sessions-enabled");
    }

    public void setSessionsEnabled(boolean isEnabled) {
        if(isEnabled) {
            sessionService.enableSessions();
        } else {
            sessionService.disableSessions();
        }

        config.set("sessions-enabled", isEnabled);
        saveConfig();
    }

    public int getSessionDayDuration() {
        return config.getInt("session-duration-days");
    }

    public int getSessionHoursDuration() {
        return config.getInt("session-duration-hours");
    }

    public int getSessionMinutesDuration() {
        return config.getInt("session-duration-minutes");
    }

    public int getSessionSecondsDuration() {
        return config.getInt("session-duration-seconds");
    }

    public void saveConfig() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
