package de.fallenstar.core.registry;

import de.fallenstar.core.command.AdminSubcommandHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Registry für Admin-Subcommand-Handler.
 *
 * Module registrieren ihre Handler hier, um eigene Admin-Befehle
 * bereitzustellen ohne dass Core direkte Dependencies benötigt.
 *
 * Pattern: Service Registry + Strategy Pattern
 *
 * @author FallenStar
 * @version 1.0
 */
public class AdminCommandRegistry {

    private final Logger logger;
    private final Map<String, AdminSubcommandHandler> handlers;

    /**
     * Erstellt eine neue AdminCommandRegistry.
     *
     * @param logger Logger für Ausgaben
     */
    public AdminCommandRegistry(Logger logger) {
        this.logger = logger;
        this.handlers = new HashMap<>();
    }

    /**
     * Registriert einen Handler für einen Subcommand.
     *
     * @param subcommand Subcommand-Name (z.B. "economy", "plots")
     * @param handler Handler-Instanz
     */
    public void registerHandler(String subcommand, AdminSubcommandHandler handler) {
        handlers.put(subcommand.toLowerCase(), handler);
        logger.info("✓ Admin-Subcommand registriert: " + subcommand);
    }

    /**
     * Gibt einen Handler für einen Subcommand zurück.
     *
     * @param subcommand Subcommand-Name
     * @return Optional mit Handler
     */
    public Optional<AdminSubcommandHandler> getHandler(String subcommand) {
        return Optional.ofNullable(handlers.get(subcommand.toLowerCase()));
    }

    /**
     * Prüft ob ein Handler für einen Subcommand registriert ist.
     *
     * @param subcommand Subcommand-Name
     * @return true wenn registriert
     */
    public boolean hasHandler(String subcommand) {
        return handlers.containsKey(subcommand.toLowerCase());
    }

    /**
     * Gibt alle registrierten Subcommand-Namen zurück.
     *
     * @return Set von Subcommand-Namen
     */
    public Set<String> getRegisteredSubcommands() {
        return handlers.keySet();
    }

    /**
     * Entfernt einen Handler.
     *
     * @param subcommand Subcommand-Name
     */
    public void unregisterHandler(String subcommand) {
        handlers.remove(subcommand.toLowerCase());
        logger.info("✗ Admin-Subcommand entfernt: " + subcommand);
    }
}
