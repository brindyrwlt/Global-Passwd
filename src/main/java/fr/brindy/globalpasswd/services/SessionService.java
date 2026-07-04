package fr.brindy.globalpasswd.services;

import fr.brindy.globalpasswd.utils.Constants;
import fr.brindy.globalpasswd.utils.DataFolder;
import fr.brindy.globalpasswd.utils.exceptions.CloseDatabaseConnectionException;
import fr.brindy.globalpasswd.utils.exceptions.DirectoryCreationException;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;

public class SessionService {
    private final JavaPlugin plugin;
    private final ConfigService configService;
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

    public SessionService(JavaPlugin plugin, ConfigService configService) throws SQLException, DirectoryCreationException {
        this.plugin = plugin;
        this.configService = configService;
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

            Date connectionDate = result.getDate(1);
            Date expirationDate = getExpirationDate(connectionDate);

            java.util.Date utilCurrentDate = new java.util.Date();
            Date currentDate = new Date(utilCurrentDate.getTime());

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
            statement.setDate(2, getCurrentDate());
            statement.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void updatePlayer(String uuid) {
        try(PreparedStatement statement = this.connection.prepareStatement(updatePlayerSessionQuery)) {
            statement.setDate(1, getCurrentDate());
            statement.setString(2, uuid);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private Date getExpirationDate(Date connectionDate) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(connectionDate);
        calendar.add(Calendar.DAY_OF_YEAR, configService.getSessionDayDuration());
        calendar.add(Calendar.HOUR, configService.getSessionHoursDuration());
        calendar.add(Calendar.MINUTE, configService.getSessionMinutesDuration());
        calendar.add(Calendar.SECOND, configService.getSessionSecondsDuration());

        java.util.Date utilExpirationDate = calendar.getTime();
        return new Date(utilExpirationDate.getTime());
    }

    private Date getCurrentDate() {
        java.util.Date utilCurrentDate = new java.util.Date();
        return new Date(utilCurrentDate.getTime());
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
