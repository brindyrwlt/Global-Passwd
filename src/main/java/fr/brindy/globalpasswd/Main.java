package fr.brindy.globalpasswd;

import fr.brindy.globalpasswd.commands.PasswdCommand;
import fr.brindy.globalpasswd.events.PlayerConnectionEvent;
import fr.brindy.globalpasswd.services.AuthService;
import fr.brindy.globalpasswd.services.ConfigService;
import fr.brindy.globalpasswd.services.SessionService;
import fr.brindy.globalpasswd.utils.Constants;
import fr.brindy.globalpasswd.utils.exceptions.DirectoryCreationException;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.sql.SQLException;

public final class Main extends JavaPlugin {

    private final ComponentLogger logger = this.getComponentLogger();

    SessionService sessionService;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        // Services
        AuthService authService = new AuthService(this);

        File configFile = new File(this.getDataFolder(), Constants.CONFIG_FILE_NAME);
        ConfigService configService = new ConfigService(this.getConfig(), configFile);

        if(configService.getSessionsEnabled()) {
            try {
                sessionService = new SessionService(this, configService);
            } catch (SQLException | DirectoryCreationException e) {
                throw new RuntimeException(e);
            }
        }

        // Events
        registerEvent(new PlayerConnectionEvent(authService, sessionService, configService));

        // Commands
        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, commands -> {
            PasswdCommand passwdCommand = new PasswdCommand(authService, configService);

            commands.registrar().register(passwdCommand.getCommand());
        });

        printStartMessage();
    }

    @Override
    public void onDisable() {
        this.sessionService.closeConnection();
    }

    private void printStartMessage() {
        logger.info(Constants.PLUGIN_START_MESSAGE_1);
        logger.info(Constants.PLUGIN_START_MESSAGE_2);
    }

    private void registerEvent(Listener event) {
        getServer().getPluginManager().registerEvents(event, this);
    }
}
