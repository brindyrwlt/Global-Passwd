package fr.brindy.globalpasswd.utils;

import fr.brindy.globalpasswd.utils.exceptions.DirectoryCreationException;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class DataFolder {
    public static File getDataFolder(JavaPlugin plugin) throws DirectoryCreationException {
        File folder = plugin.getDataFolder();
        if(!folder.exists()) {
            boolean isCreated = folder.mkdir();
            if(!isCreated) {
                throw new DirectoryCreationException();
            }
        }

        return folder;
    }
}
