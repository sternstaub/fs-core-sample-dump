package de.fallenstar.plot.model;

import de.fallenstar.core.provider.Plot;
import de.fallenstar.plot.manager.PlotNameManager;

import java.util.Optional;

/**
 * Wrapper-Klasse für Plots mit Custom-Namen.
 *
 * Features:
 * - Benutzerdefinierte Namen für Grundstücke
 * - Optional: Fallback zu Default-Namen
 * - Persistent gespeichert
 *
 * **Verwendung:**
 * <pre>
 * Plot plot = plotProvider.getPlot(location);
 * NamedPlot namedPlot = new NamedPlot(plot, plotNameManager);
 * namedPlot.setCustomName("Meine Handelsgilde");
 * String displayName = namedPlot.getDisplayName();  // "Meine Handelsgilde"
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
public class NamedPlot extends Plot {

    private final PlotNameManager nameManager;

    /**
     * Erstellt einen NamedPlot-Wrapper.
     *
     * @param plot Der ursprüngliche Plot
     * @param nameManager Der PlotNameManager
     */
    public NamedPlot(Plot plot, PlotNameManager nameManager) {
        super(plot.getUuid(), plot.getIdentifier(), plot.getLocation(), plot.getNativePlot());
        this.nameManager = nameManager;
    }

    /**
     * Gibt den benutzerdefinierten Namen zurück.
     *
     * @return Optional mit Custom-Namen, oder empty wenn nicht gesetzt
     */
    public Optional<String> getCustomName() {
        String name = nameManager.getPlotName(this);
        return Optional.ofNullable(name);
    }

    /**
     * Setzt den benutzerdefinierten Namen.
     *
     * @param name Der neue Name (max. 32 Zeichen)
     * @throws IllegalArgumentException wenn Name zu lang oder ungültig
     */
    public void setCustomName(String name) {
        if (!isValidName(name)) {
            throw new IllegalArgumentException("Ungültiger Plot-Name: " + name);
        }
        nameManager.setPlotName(this, name);
    }

    /**
     * Entfernt den benutzerdefinierten Namen.
     *
     * Danach wird wieder der Default-Name verwendet.
     */
    public void clearCustomName() {
        nameManager.setPlotName(this, null);
    }

    /**
     * Gibt den Anzeige-Namen zurück (Custom oder Default).
     *
     * Falls Custom-Name gesetzt ist, wird dieser verwendet.
     * Sonst wird der Default-Name zurückgegeben (z.B. "Plot #123").
     *
     * @return Anzeige-Name
     */
    public String getDisplayName() {
        return getCustomName().orElse("Plot #" + getIdentifier());
    }

    /**
     * Prüft ob ein Custom-Name gesetzt ist.
     *
     * @return true wenn Custom-Name vorhanden
     */
    public boolean hasCustomName() {
        return getCustomName().isPresent();
    }

    /**
     * Validiert einen Namen.
     *
     * @param name Der zu validierende Name
     * @return true wenn gültig
     */
    public boolean isValidName(String name) {
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
