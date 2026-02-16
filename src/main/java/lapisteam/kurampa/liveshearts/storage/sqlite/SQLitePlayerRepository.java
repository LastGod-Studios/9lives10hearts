package lapisteam.kurampa.liveshearts.storage.sqlite;

import lapisteam.kurampa.liveshearts.storage.PlayerRepository;
import org.bukkit.plugin.java.JavaPlugin;
import org.sqlite.SQLiteDataSource;

import java.io.File;
import java.sql.*;
import java.util.Optional;
import java.util.UUID;

public class SQLitePlayerRepository implements PlayerRepository {

    private final SQLiteDataSource ds = new SQLiteDataSource();
    private final JavaPlugin plugin;
    private boolean initialized = false;

    public SQLitePlayerRepository(JavaPlugin plugin) {
        this.plugin = plugin;
        File dbFile = new File(plugin.getDataFolder(), "data.db");
        ds.setUrl("jdbc:sqlite:" + dbFile.getAbsolutePath());
        try (Connection conn = ds.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS players (
                  uuid   TEXT PRIMARY KEY,
                  hearts INTEGER NOT NULL
                )
                """);
            initialized = true;
        } catch (SQLException ex) {
            plugin.getLogger().severe("Не удалось инициализировать базу данных: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    @Override
    public Optional<Integer> findHearts(UUID playerId) {
        if (!initialized) {
            plugin.getLogger().severe("БД не инициализирована, невозможно прочитать данные игрока " + playerId);
            return Optional.empty();
        }
        String sql = "SELECT hearts FROM players WHERE uuid = ?";
        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, playerId.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(rs.getInt("hearts"));
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public void saveHearts(UUID playerId, int hearts) {
        if (!initialized) {
            plugin.getLogger().severe("БД не инициализирована, невозможно сохранить данные игрока " + playerId);
            return;
        }
        String sql = """
            INSERT INTO players (uuid, hearts)
            VALUES (?, ?)
            ON CONFLICT(uuid) DO UPDATE SET hearts = excluded.hearts
            """;
        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, playerId.toString());
            ps.setInt(2, hearts);
            ps.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
}
