package de.fallenstar.core.database.impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import de.fallenstar.core.database.DataStore;
import org.bukkit.configuration.ConfigurationSection;

import java.sql.*;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Basis-Implementation für SQL-Datenbanken mit HikariCP.
 *
 * Unterstützt MySQL und PostgreSQL.
 * Verwendet Connection-Pooling für Performance.
 *
 * @author FallenStar
 * @version 1.0
 */
public abstract class HikariDataStore implements DataStore {

    protected final Logger logger;
    protected final Gson gson;
    protected final ExecutorService executor;
    protected HikariDataSource dataSource;

    protected HikariDataStore(Logger logger) {
        this.logger = logger;
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.executor = Executors.newFixedThreadPool(4);
    }

    /**
     * Initialisiert den HikariCP DataSource.
     *
     * @param config Konfiguration aus config.yml
     * @param jdbcUrl JDBC URL (z.B. jdbc:mysql://localhost:3306/fallenstar)
     */
    protected void initialize(ConfigurationSection config, String jdbcUrl) {
        try {
            HikariConfig hikariConfig = new HikariConfig();

            hikariConfig.setJdbcUrl(jdbcUrl);
            hikariConfig.setUsername(config.getString("username", "root"));
            hikariConfig.setPassword(config.getString("password", "password"));
            hikariConfig.setMaximumPoolSize(config.getInt("pool-size", 10));
            hikariConfig.setConnectionTimeout(config.getInt("connection-timeout", 30) * 1000L);

            // Performance-Optimierungen
            hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
            hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");
            hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
            hikariConfig.addDataSourceProperty("useServerPrepStmts", "true");

            dataSource = new HikariDataSource(hikariConfig);

            // Schema erstellen
            createSchema();

            logger.info("✓ HikariDataStore initialized: " + jdbcUrl);

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to initialize HikariCP", e);
        }
    }

    /**
     * Erstellt das Datenbankschema.
     */
    private void createSchema() throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS data_store (
                namespace VARCHAR(255) NOT NULL,
                key VARCHAR(255) NOT NULL,
                value TEXT NOT NULL,
                type VARCHAR(255) NOT NULL,
                created_at BIGINT NOT NULL,
                updated_at BIGINT NOT NULL,
                PRIMARY KEY (namespace, key)
            )
            """;

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        }

        // Index für schnellere Abfragen
        String indexSql = """
            CREATE INDEX IF NOT EXISTS idx_namespace
            ON data_store(namespace)
            """;

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(indexSql);
        }
    }

    @Override
    public CompletableFuture<Boolean> save(String namespace, String key, Object data) {
        return CompletableFuture.supplyAsync(() -> saveSync(namespace, key, data), executor);
    }

    @Override
    public <T> CompletableFuture<Optional<T>> load(String namespace, String key, Class<T> type) {
        return CompletableFuture.supplyAsync(() -> loadSync(namespace, key, type), executor);
    }

    @Override
    public CompletableFuture<Boolean> delete(String namespace, String key) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "DELETE FROM data_store WHERE namespace = ? AND key = ?";

            try (Connection conn = dataSource.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

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

            try (Connection conn = dataSource.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

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
            INSERT INTO data_store (namespace, key, value, type, created_at, updated_at)
            VALUES (?, ?, ?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE value = ?, type = ?, updated_at = ?
            """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, namespace);
            stmt.setString(2, key);
            stmt.setString(3, json);
            stmt.setString(4, typeName);
            stmt.setLong(5, now);
            stmt.setLong(6, now);
            // UPDATE values
            stmt.setString(7, json);
            stmt.setString(8, typeName);
            stmt.setLong(9, now);

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            logger.log(Level.WARNING, "Failed to save data: " + namespace + "/" + key, e);
            return false;
        }
    }

    @Override
    public <T> Optional<T> loadSync(String namespace, String key, Class<T> type) {
        String sql = "SELECT value, type FROM data_store WHERE namespace = ? AND key = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

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
        logger.info("Shutting down HikariDataStore...");

        // Executor beenden
        executor.shutdown();

        // DataSource schließen
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            logger.info("✓ HikariDataStore shutdown complete");
        }
    }
}
