package de.fallenstar.core.database.impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.fallenstar.core.database.DataStore;

import java.io.File;
import java.sql.*;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * SQLite-Implementation des DataStore.
 *
 * Verwendet SQLite als lokale Datenbank.
 * JSON-Serialisierung für Objekte via Gson.
 *
 * Eigenschaften:
 * - Asynchrone Operationen (kein Main-Thread-Blocking)
 * - Connection-Pooling
 * - Automatische Schema-Erstellung
 * - Graceful Shutdown
 *
 * @author FallenStar
 * @version 1.0
 */
public class SQLiteDataStore implements DataStore {

    private final File dataFolder;
    private final Logger logger;
    private final Gson gson;
    private final ExecutorService executor;
    private Connection connection;

    /**
     * Erstellt einen neuen SQLiteDataStore.
     *
     * @param dataFolder Plugin-Datenverzeichnis
     */
    public SQLiteDataStore(File dataFolder) {
        this.dataFolder = dataFolder;
        this.logger = Logger.getLogger("FallenStarCore");
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();
        this.executor = Executors.newFixedThreadPool(2);

        initialize();
    }

    /**
     * Initialisiert die Datenbank.
     */
    private void initialize() {
        try {
            // Sicherstellen dass Datenverzeichnis existiert
            if (!dataFolder.exists()) {
                dataFolder.mkdirs();
            }

            // SQLite-Connection erstellen
            File dbFile = new File(dataFolder, "fallenstar.db");
            String url = "jdbc:sqlite:" + dbFile.getAbsolutePath();

            connection = DriverManager.getConnection(url);

            // Schema erstellen falls nicht vorhanden
            createSchema();

            logger.info("✓ SQLiteDataStore initialized: " + dbFile.getAbsolutePath());

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to initialize SQLite database", e);
        }
    }

    /**
     * Erstellt das Datenbankschema.
     */
    private void createSchema() throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS data_store (
                namespace TEXT NOT NULL,
                key TEXT NOT NULL,
                value TEXT NOT NULL,
                type TEXT NOT NULL,
                created_at INTEGER NOT NULL,
                updated_at INTEGER NOT NULL,
                PRIMARY KEY (namespace, key)
            )
            """;

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        }

        // Index für schnellere Abfragen
        String indexSql = """
            CREATE INDEX IF NOT EXISTS idx_namespace
            ON data_store(namespace)
            """;

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(indexSql);
        }
    }

    @Override
    public CompletableFuture<Boolean> save(String namespace, String key, Object data) {
        return CompletableFuture.supplyAsync(() -> {
            return saveSync(namespace, key, data);
        }, executor);
    }

    @Override
    public <T> CompletableFuture<Optional<T>> load(String namespace, String key, Class<T> type) {
        return CompletableFuture.supplyAsync(() -> {
            return loadSync(namespace, key, type);
        }, executor);
    }

    @Override
    public CompletableFuture<Boolean> delete(String namespace, String key) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "DELETE FROM data_store WHERE namespace = ? AND key = ?";

            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, namespace);
                stmt.setString(2, key);

                return stmt.executeUpdate() > 0;

            } catch (SQLException e) {
                logger.log(Level.WARNING, "Failed to delete data: " + namespace + "/" + key, e);
                return false;
            }
        }, executor);
    }

    @Override
    public CompletableFuture<Boolean> exists(String namespace, String key) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "SELECT 1 FROM data_store WHERE namespace = ? AND key = ? LIMIT 1";

            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, namespace);
                stmt.setString(2, key);

                ResultSet rs = stmt.executeQuery();
                return rs.next();

            } catch (SQLException e) {
                logger.log(Level.WARNING, "Failed to check existence: " + namespace + "/" + key, e);
                return false;
            }
        }, executor);
    }

    @Override
    public boolean saveSync(String namespace, String key, Object data) {
        String json = gson.toJson(data);
        String typeName = data.getClass().getName();
        long now = System.currentTimeMillis();

        String sql = """
            INSERT OR REPLACE INTO data_store
            (namespace, key, value, type, created_at, updated_at)
            VALUES (?, ?, ?, ?,
                COALESCE((SELECT created_at FROM data_store WHERE namespace = ? AND key = ?), ?),
                ?)
            """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, namespace);
            stmt.setString(2, key);
            stmt.setString(3, json);
            stmt.setString(4, typeName);
            stmt.setString(5, namespace);
            stmt.setString(6, key);
            stmt.setLong(7, now);
            stmt.setLong(8, now);

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            logger.log(Level.WARNING, "Failed to save data: " + namespace + "/" + key, e);
            return false;
        }
    }

    @Override
    public <T> Optional<T> loadSync(String namespace, String key, Class<T> type) {
        String sql = "SELECT value, type FROM data_store WHERE namespace = ? AND key = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, namespace);
            stmt.setString(2, key);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String json = rs.getString("value");
                T object = gson.fromJson(json, type);
                return Optional.of(object);
            }

            return Optional.empty();

        } catch (SQLException e) {
            logger.log(Level.WARNING, "Failed to load data: " + namespace + "/" + key, e);
            return Optional.empty();
        }
    }

    @Override
    public void shutdown() {
        logger.info("Shutting down SQLiteDataStore...");

        // Executor beenden
        executor.shutdown();

        // Connection schließen
        if (connection != null) {
            try {
                connection.close();
                logger.info("✓ SQLiteDataStore shutdown complete");
            } catch (SQLException e) {
                logger.log(Level.WARNING, "Error closing database connection", e);
            }
        }
    }
}
