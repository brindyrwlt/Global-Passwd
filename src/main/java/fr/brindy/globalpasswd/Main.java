package fr.brindy.globalpasswd;

import fr.brindy.globalpasswd.commands.PasswdCommand;
import fr.brindy.globalpasswd.events.PlayerConnectionEvent;
import fr.brindy.globalpasswd.services.AuthService;
import fr.brindy.globalpasswd.utils.Constants;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin {

    private final ComponentLogger logger = this.getComponentLogger();

    @Override
    public void onEnable() {
        // Services
        AuthService authService = new AuthService(this);

        // Events
        registerEvent(new PlayerConnectionEvent(authService));

        // Commands
        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, commands -> {
            PasswdCommand passwdCommand = new PasswdCommand(authService);

            commands.registrar().register(passwdCommand.getCommand());
        });

        printStartMessage();
    }

    private void printStartMessage() {
        logger.info(Constants.PLUGIN_START_MESSAGE_1);
        logger.info(Constants.PLUGIN_START_MESSAGE_2);
    }

    private void registerEvent(Listener event) {
        getServer().getPluginManager().registerEvents(event, this);
    }
}
