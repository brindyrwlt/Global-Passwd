package fr.brindy.globalpasswd;

import fr.brindy.globalpasswd.commands.PasswdCommand;
import fr.brindy.globalpasswd.events.PlayerConnectionEvent;
import fr.brindy.globalpasswd.services.AuthService;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
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

        logger.info(Component.text("The Global Passwd plugin is enabled. Your server is now protected!").color(TextColor.color(0xFFFFFF)));
        logger.info(Component.text("If you want to change your password, please enter 'passwd change' in your server console."));
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    private void registerEvent(Listener event) {
        getServer().getPluginManager().registerEvents(event, this);
    }
}
