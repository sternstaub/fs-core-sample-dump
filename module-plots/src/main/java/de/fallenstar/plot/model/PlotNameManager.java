package de.fallenstar.plot.model;

import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Verwaltet benutzerdefinierte Plot-Namen.
 *
 * Features:
 * - Zentrale Verwaltung aller Custom-Namen
 * - Persistent in Config gespeichert
 * - Cache für Performance
 *
 * **Verwendung:**
 * <pre>
 * plotNameManager.setCustomName(plotId, "Meine Handelsgilde");
 * Optional<String> name = plotNameManager.getCustomName(plotId);
 * </pre>
 *
 * **Persistierung:**
 * - Config: plot-names.yml
 * - Format: custom-names.<plot-id>: "Name"
 *
 * @author FallenStar
 * @version 1.0
 */
public class PlotNameManager {

    private final Logger logger;
    private final Map<String, String> customNames;  // PlotID → Custom-Name

    /**
     * Konstruktor für PlotNameManager.
     *
     * @param logger Logger
     */
    public PlotNameManager(Logger logger) {
        this.logger = logger;
        this.customNames = new HashMap<>();

        logger.info("PlotNameManager initialisiert");
    }

    /**
     * Setzt einen Custom-Namen für ein Grundstück.
     *
     * @param plotId Plot-ID
     * @param name Custom-Name
     * @return true wenn erfolgreich gesetzt
     */
    public boolean setCustomName(String plotId, String name) {
        if (plotId == null || plotId.isEmpty()) {
            logger.warning("Ungültige Plot-ID: " + plotId);
            return false;
        }

        if (name == null || name.isEmpty()) {
            logger.warning("Ungültiger Name: " + name);
            return false;
        }

        // Validiere Namen
        if (!isValidName(name)) {
            logger.warning("Name ungültig: " + name + " (zu lang oder Sonderzeichen)");
            return false;
        }

        customNames.put(plotId, name);
        logger.info("Custom-Name gesetzt: " + plotId + " → " + name);
        return true;
    }

    /**
     * Gibt den Custom-Namen für ein Grundstück zurück.
     *
     * @param plotId Plot-ID
     * @return Optional mit Custom-Namen
     */
    public Optional<String> getCustomName(String plotId) {
        if (plotId == null || plotId.isEmpty()) {
            return Optional.empty();
        }

        return Optional.ofNullable(customNames.get(plotId));
    }

    /**
     * Entfernt den Custom-Namen für ein Grundstück.
     *
     * @param plotId Plot-ID
     * @return true wenn erfolgreich entfernt
     */
    public boolean clearCustomName(String plotId) {
        if (plotId == null || plotId.isEmpty()) {
            return false;
        }

        String removed = customNames.remove(plotId);

        if (removed != null) {
            logger.info("Custom-Name entfernt: " + plotId + " (war: " + removed + ")");
            return true;
        }

        return false;
    }

    /**
     * Prüft ob ein Grundstück einen Custom-Namen hat.
     *
     * @param plotId Plot-ID
     * @return true wenn Custom-Name vorhanden
     */
    public boolean hasCustomName(String plotId) {
        return customNames.containsKey(plotId);
    }

    /**
     * Gibt die Anzahl gesetzter Custom-Namen zurück.
     *
     * @return Anzahl
     */
    public int getCustomNameCount() {
        return customNames.size();
    }

    /**
     * Validiert einen Namen.
     *
     * @param name Der zu validierende Name
     * @return true wenn gültig
     */
    private boolean isValidName(String name) {
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
     * Lädt Custom-Namen aus Config.
     *
     * @param config FileConfiguration
     */
    public void loadFromConfig(FileConfiguration config) {
        if (!config.contains("custom-names")) {
            logger.info("Keine Custom-Namen in Config gefunden");
            return;
        }

        Map<String, Object> names = config.getConfigurationSection("custom-names").getValues(false);

        customNames.clear();

        for (Map.Entry<String, Object> entry : names.entrySet()) {
            String plotId = entry.getKey();
            String name = entry.getValue().toString();

            customNames.put(plotId, name);
        }

        logger.info("Custom-Namen geladen: " + customNames.size() + " Namen");
    }

    /**
     * Speichert Custom-Namen in Config.
     *
     * @param config FileConfiguration
     */
    public void saveToConfig(FileConfiguration config) {
        // Lösche alte Daten
        config.set("custom-names", null);

        // Schreibe alle Namen
        for (Map.Entry<String, String> entry : customNames.entrySet()) {
            String plotId = entry.getKey();
            String name = entry.getValue();

            config.set("custom-names." + plotId, name);
        }

        logger.info("Custom-Namen gespeichert: " + customNames.size() + " Namen");
    }

    /**
     * Gibt Debug-Informationen zurück.
     *
     * @return Debug-String
     */
    public String getDebugInfo() {
        return "PlotNameManager{" +
                "customNames=" + customNames.size() +
                '}';
    }
}
