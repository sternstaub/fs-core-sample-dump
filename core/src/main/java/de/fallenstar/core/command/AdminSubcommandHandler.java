package de.fallenstar.core.command;

import org.bukkit.command.CommandSender;

/**
 * Interface für Admin-Subcommand-Handler.
 *
 * Module können eigene Handler implementieren und im Core registrieren.
 * Ermöglicht Module-spezifische Admin-Befehle ohne Reflection.
 *
 * Beispiel:
 * - Economy-Modul registriert Handler für "economy" Subcommand
 * - Plots-Modul registriert Handler für "plots" Subcommand
 *
 * @author FallenStar
 * @version 1.0
 */
public interface AdminSubcommandHandler {

    /**
     * Behandelt einen Admin-Subcommand.
     *
     * @param sender Command-Sender
     * @param args Argumente (ohne Subcommand-Name)
     * @return true wenn erfolgreich verarbeitet
     */
    boolean handle(CommandSender sender, String[] args);

    /**
     * Gibt Tab-Completions für diesen Subcommand zurück.
     *
     * @param args Aktuelle Argumente (ohne Subcommand-Name)
     * @return Liste von Completions
     */
    java.util.List<String> getTabCompletions(String[] args);

    /**
     * Gibt die Hilfe-Nachricht für diesen Subcommand zurück.
     *
     * @param sender Command-Sender
     */
    void sendHelp(CommandSender sender);
}
