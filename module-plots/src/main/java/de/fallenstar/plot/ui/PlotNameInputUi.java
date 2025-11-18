package de.fallenstar.plot.ui;

import de.fallenstar.core.provider.Plot;
import de.fallenstar.plot.model.PlotNameManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

import java.util.function.Consumer;

/**
 * UI zur Eingabe eines Plot-Namens.
 *
 * Features:
 * - Namen-Eingabe via Chat (vereinfacht)
 * - Validierung (max. 32 Zeichen, Sonderzeichen)
 * - Callback bei Erfolg
 *
 * **Verwendung:**
 * <pre>
 * PlotNameInputUi.openNameInput(player, plot, plotNameManager, name -> {
 *     player.sendMessage("Name gesetzt: " + name);
 * });
 * </pre>
 *
 * **Hinweis:**
 * Dies ist eine vereinfachte Implementierung via Chat.
 * Für eine vollständige Implementierung sollte ein
 * AnvilUi oder SignUi verwendet werden (UI-Framework).
 *
 * @author FallenStar
 * @version 1.0
 */
public class PlotNameInputUi {

    /**
     * Öffnet die Namen-Eingabe für einen Spieler.
     *
     * @param player Der Spieler
     * @param plot Der Plot
     * @param plotNameManager PlotNameManager
     * @param onSuccess Callback bei Erfolg
     */
    public static void openNameInput(
            Player player,
            Plot plot,
            PlotNameManager plotNameManager,
            Consumer<String> onSuccess
    ) {
        // Zeige Anleitung
        player.sendMessage(Component.empty());
        player.sendMessage(Component.text("═══════════════════════════════════", NamedTextColor.GOLD));
        player.sendMessage(Component.text("         Plot-Namen setzen", NamedTextColor.YELLOW));
        player.sendMessage(Component.text("═══════════════════════════════════", NamedTextColor.GOLD));
        player.sendMessage(Component.empty());
        player.sendMessage(Component.text("Aktueller Name: ", NamedTextColor.GRAY)
                .append(Component.text(plot.getIdentifier(), NamedTextColor.YELLOW)));
        player.sendMessage(Component.empty());
        player.sendMessage(Component.text("Schreibe den neuen Namen in den Chat:", NamedTextColor.YELLOW));
        player.sendMessage(Component.text("  • Max. 32 Zeichen", NamedTextColor.GRAY));
        player.sendMessage(Component.text("  • Buchstaben, Zahlen, Leerzeichen, -, _", NamedTextColor.GRAY));
        player.sendMessage(Component.text("  • Schreibe 'cancel' zum Abbrechen", NamedTextColor.GRAY));
        player.sendMessage(Component.empty());
        player.sendMessage(Component.text("═══════════════════════════════════", NamedTextColor.GOLD));
        player.sendMessage(Component.empty());

        // TODO: Chat-Listener registrieren
        // Für vollständige Implementierung:
        // 1. AsyncPlayerChatEvent-Listener registrieren
        // 2. Bei Eingabe validieren
        // 3. plotNameManager.setCustomName() aufrufen
        // 4. onSuccess-Callback ausführen
        // 5. Listener de-registrieren

        // Vereinfachte Version: Nur Info-Nachricht
        player.sendMessage(Component.text("✗ Chat-basierte Eingabe noch nicht implementiert!", NamedTextColor.RED));
        player.sendMessage(Component.text("Nutze vorerst: /plot setname <name>", NamedTextColor.YELLOW));
    }

    /**
     * Validiert einen Plot-Namen.
     *
     * @param name Der zu validierende Name
     * @return true wenn gültig
     */
    public static boolean isValidName(String name) {
        if (name == null || name.isEmpty()) {
            return false;
        }

        // Max. 32 Zeichen
        if (name.length() > 32) {
            return false;
        }

        // Keine Sonderzeichen außer Leerzeichen, -, _
        return name.matches("[a-zA-Z0-9äöüÄÖÜß \\-_]+");
    }

    /**
     * Gibt eine Validierungs-Fehlermeldung zurück.
     *
     * @param name Der validierte Name
     * @return Fehlermeldung oder null wenn gültig
     */
    public static String getValidationError(String name) {
        if (name == null || name.isEmpty()) {
            return "Name darf nicht leer sein!";
        }

        if (name.length() > 32) {
            return "Name zu lang! Max. 32 Zeichen erlaubt.";
        }

        if (!name.matches("[a-zA-Z0-9äöüÄÖÜß \\-_]+")) {
            return "Name enthält ungültige Zeichen! Nur Buchstaben, Zahlen, Leerzeichen, -, _ erlaubt.";
        }

        return null;
    }
}
