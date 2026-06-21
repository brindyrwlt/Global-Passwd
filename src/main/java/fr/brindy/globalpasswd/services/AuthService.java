package fr.brindy.globalpasswd.services;

import fr.brindy.globalpasswd.utils.Constants;
import fr.brindy.globalpasswd.utils.DataFolder;
import org.bukkit.plugin.java.JavaPlugin;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Arrays;
import java.util.Objects;

public class AuthService {

    private static final int ITERATION_COUNT = 10;
    private static final int LENGTH = 50;

    private final JavaPlugin plugin;

    public AuthService(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean compare(String given) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        return Arrays.equals(getPassword(), getHash(given));
    }

    private byte[] getPassword() throws IOException {
        Path path = getKeyFile();
        return Files.readAllBytes(path);
    }

    public void savePassword(String password) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        Path path = getKeyFile();
        Files.write(path, getHash(password));
    }

    private byte[] getHash(String password) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] salt;

        try (InputStream saltFile = this.getClass().getClassLoader().getResourceAsStream("salt")) {
            salt = Objects.requireNonNull(saltFile).readAllBytes();
        }

        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATION_COUNT, LENGTH);
        SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        SecretKey key = skf.generateSecret(spec);
        return key.getEncoded();
    }

    private Path getKeyFile() throws IOException {
        File folder = DataFolder.getDataFolder(this.plugin);
        return Paths.get(folder.getCanonicalPath() + File.separator + Constants.KEY_FILE_NAME);
    }
}
