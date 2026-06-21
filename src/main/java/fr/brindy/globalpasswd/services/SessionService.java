package fr.brindy.globalpasswd.services;

import fr.brindy.globalpasswd.utils.Constants;
import fr.brindy.globalpasswd.utils.DataFolder;
import fr.brindy.globalpasswd.utils.exceptions.CloseDatabaseConnectionException;
import fr.brindy.globalpasswd.utils.exceptions.DirectoryCreationException;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.sql.*;
import java.util.Calendar;

public class SessionService {
    private final JavaPlugin plugin;
    private final Connection connection;

    private static final String getSessionExistenceQuery = """
        SELECT COUNT(*)
        FROM sessions
        WHERE uuid = ?;
    """;

    private static final String getPlayerSessionQuery = """
        SELECT connection_date
        FROM sessions
        WHERE uuid = ?;
    """;

    private static final String savePlayerSessionQuery = """
        INSERT INTO sessions VALUES (
            ?,
            ?
        );
    """;

    private static final String updatePlayerSessionQuery = """
        UPDATE sessions
        SET connection_date = ?
        WHERE uuid = ?;
    """;

    public SessionService(JavaPlugin plugin) throws SQLException, DirectoryCreationException {
        this.plugin = plugin;
        this.connection = DriverManager.getConnection("jdbc:sqlite:" + getDatabasePath());

        createDatabase();
    }

    public void validateSession(String uuid) {
        if(doesSessionExists(uuid)) {
            updatePlayer(uuid);
        } else {
            savePlayer(uuid);
        }
    }

    public boolean isSessionValid(String uuid) {
        if(!doesSessionExists(uuid)) {
            return false;
        }

        try(PreparedStatement statement = this.connection.prepareStatement(getPlayerSessionQuery)) {
            statement.setString(1, uuid);
            ResultSet result = statement.executeQuery();

            java.util.Date utilCurrentDate = new java.util.Date();
            Date currentDate = new java.sql.Date(utilCurrentDate.getTime());
            java.sql.Date expirationDate = result.getDate(1);

            return expirationDate.after(currentDate);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean doesSessionExists(String uuid) {
        try(PreparedStatement statement = this.connection.prepareStatement(getSessionExistenceQuery)) {
            statement.setString(1, uuid);
            ResultSet result = statement.executeQuery();
            return result.getInt(1) == 1;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void savePlayer(String uuid) {
        try(PreparedStatement statement = this.connection.prepareStatement(savePlayerSessionQuery)) {
            statement.setString(1, uuid);
            statement.setDate(2, getExpirationDate());
            statement.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void updatePlayer(String uuid) {
        try(PreparedStatement statement = this.connection.prepareStatement(updatePlayerSessionQuery)) {
            statement.setDate(1, getExpirationDate());
            statement.setString(2, uuid);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private java.sql.Date getExpirationDate() {
        java.util.Date utilExpirationDate = new java.util.Date();

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(utilExpirationDate);
        calendar.add(Calendar.DAY_OF_YEAR, Constants.SESSION_DURATION_DAY);
        calendar.add(Calendar.HOUR, Constants.SESSION_DURATION_HOURS);
        calendar.add(Calendar.MINUTE, Constants.SESSION_DURATION_MINUTES);
        calendar.add(Calendar.SECOND, Constants.SESSION_DURATION_SECONDS);

        utilExpirationDate = calendar.getTime();

        return new java.sql.Date(utilExpirationDate.getTime());
    }

    private void createDatabase() {
        try(Statement statement = this.connection.createStatement()) {
            statement.execute(
                    """
                    CREATE TABLE IF NOT EXISTS sessions (
                        uuid TEXT PRIMARY KEY,
                        connection_date DATE NOT NULL
                    );
                    """
            );
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private String getDatabasePath() throws DirectoryCreationException {
        File folder = DataFolder.getDataFolder(this.plugin);
        return folder.getAbsolutePath() + File.separator + Constants.SESSIONS_FILE_NAME;
    }

    public void closeConnection() throws CloseDatabaseConnectionException {
        try {
            if(this.connection != null && !this.connection.isClosed()) {
                this.connection.close();
            }
        } catch (SQLException e) {
            throw new CloseDatabaseConnectionException(e.getMessage());
        }

    }
}
