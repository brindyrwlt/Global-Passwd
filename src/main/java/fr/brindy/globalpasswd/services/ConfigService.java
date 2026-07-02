package fr.brindy.globalpasswd.services;

import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.io.IOException;

public class ConfigService {
    private final FileConfiguration config;
    private final File configFile;

    public ConfigService(FileConfiguration config, File configFile) {
        this.config = config;
        this.configFile = configFile;
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
