package de.fallenstar.plot.model;

import de.fallenstar.core.provider.Plot;

import java.util.Optional;

/**
 * Erweitert Plot-Interface um Namen-Feature.
 *
 * Features:
 * - Benutzerdefinierte Namen für Grundstücke
 * - Optional: Fallback zu Default-Namen
 * - Persistent gespeichert
 *
 * **Verwendung:**
 * <pre>
 * NamedPlot plot = (NamedPlot) plotProvider.getPlot(location);
 * plot.setCustomName("Meine Handelsgilde");
 * String displayName = plot.getDisplayName();  // "Meine Handelsgilde"
 * </pre>
 *
 * **Integration:**
 * - HandelsgildeUI: Owner-Button "Plot-Namen setzen"
 * - PlotListUI: Anzeige Custom-Namen in Listen
 * - PlotInfoCommand: Custom-Namen in /plot info
 *
 * @author FallenStar
 * @version 1.0
 */
public interface NamedPlot extends Plot {

    /**
     * Gibt den benutzerdefinierten Namen zurück.
     *
     * @return Optional mit Custom-Namen, oder empty wenn nicht gesetzt
     */
    Optional<String> getCustomName();

    /**
     * Setzt den benutzerdefinierten Namen.
     *
     * @param name Der neue Name (max. 32 Zeichen)
     * @throws IllegalArgumentException wenn Name zu lang oder ungültig
     */
    void setCustomName(String name);

    /**
     * Entfernt den benutzerdefinierten Namen.
     *
     * Danach wird wieder der Default-Name verwendet.
     */
    void clearCustomName();

    /**
     * Gibt den Anzeige-Namen zurück (Custom oder Default).
     *
     * Falls Custom-Name gesetzt ist, wird dieser verwendet.
     * Sonst wird der Default-Name zurückgegeben (z.B. "Plot #123").
     *
     * @return Anzeige-Name
     */
    default String getDisplayName() {
        return getCustomName().orElse("Plot #" + getPlotId());
    }

    /**
     * Prüft ob ein Custom-Name gesetzt ist.
     *
     * @return true wenn Custom-Name vorhanden
     */
    default boolean hasCustomName() {
        return getCustomName().isPresent();
    }

    /**
     * Validiert einen Namen.
     *
     * @param name Der zu validierende Name
     * @return true wenn gültig
     */
    default boolean isValidName(String name) {
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
}
