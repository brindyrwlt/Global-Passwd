package fr.brindy.globalpasswd;

import fr.brindy.globalpasswd.services.AuthService;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.logging.Level;

public final class Main extends JavaPlugin {

    private ComponentLogger logger = this.getComponentLogger();

    @Override
    public void onEnable() {


        try {
            AuthService authService = new AuthService(this);

            authService.savePassword("test");
            getLogger().log(Level.INFO, String.valueOf(authService.compare("erfzerh")));
        } catch (NoSuchAlgorithmException | IOException | InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

}
