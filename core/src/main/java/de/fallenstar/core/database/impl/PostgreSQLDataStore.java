package de.fallenstar.core.database.impl;

import org.bukkit.configuration.ConfigurationSection;

import java.util.logging.Logger;

/**
 * PostgreSQL-Implementation des DataStore.
 *
 * Verwendet HikariCP für Connection-Pooling.
 * Alternative zu MySQL mit erweiterten Features.
 *
 * Features:
 * - Auto-Reconnect
 * - Connection-Pooling
 * - Async Operations
 * - PostgreSQL-spezifische Optimierungen
 *
 * @author FallenStar
 * @version 1.0
 */
public class PostgreSQLDataStore extends HikariDataStore {

    /**
     * Erstellt einen neuen PostgreSQLDataStore.
     *
     * @param config PostgreSQL-Konfiguration aus config.yml
     * @param logger Logger
     */
    public PostgreSQLDataStore(ConfigurationSection config, Logger logger) {
        super(logger);

        String host = config.getString("host", "localhost");
        int port = config.getInt("port", 5432);
        String database = config.getString("database", "fallenstar");
        boolean useSSL = config.getBoolean("use-ssl", false);

        String jdbcUrl = String.format(
            "jdbc:postgresql://%s:%d/%s?ssl=%s",
            host, port, database, useSSL
        );

        initialize(config, jdbcUrl);
    }
}
