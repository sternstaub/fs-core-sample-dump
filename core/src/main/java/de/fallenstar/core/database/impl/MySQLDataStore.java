package de.fallenstar.core.database.impl;

import org.bukkit.configuration.ConfigurationSection;

import java.util.logging.Logger;

/**
 * MySQL-Implementation des DataStore.
 *
 * Verwendet HikariCP für Connection-Pooling.
 * Empfohlen für große Server und Netzwerke.
 *
 * Features:
 * - Auto-Reconnect
 * - Connection-Pooling
 * - Async Operations
 * - Optimierte Performance
 *
 * @author FallenStar
 * @version 1.0
 */
public class MySQLDataStore extends HikariDataStore {

    /**
     * Erstellt einen neuen MySQLDataStore.
     *
     * @param config MySQL-Konfiguration aus config.yml
     * @param logger Logger
     */
    public MySQLDataStore(ConfigurationSection config, Logger logger) {
        super(logger);

        String host = config.getString("host", "localhost");
        int port = config.getInt("port", 3306);
        String database = config.getString("database", "fallenstar");
        boolean useSSL = config.getBoolean("use-ssl", false);

        String jdbcUrl = String.format(
            "jdbc:mysql://%s:%d/%s?useSSL=%s&allowPublicKeyRetrieval=true&serverTimezone=UTC",
            host, port, database, useSSL
        );

        initialize(config, jdbcUrl);
    }
}
