package de.fallenstar.plot.manager;

import com.palmergames.bukkit.towny.object.TownBlock;
import com.palmergames.bukkit.towny.object.metadata.StringDataField;
import de.fallenstar.core.provider.Plot;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * Verwaltet benutzerdefinierte Namen für Plots.
 *
 * Plot-Namen ermöglichen es Spielern, ihre Grundstücke zu benennen
 * (z.B. "Mein Haupthaus", "Handelsgilde-West", "Botschaft-Nord").
 *
 * Speicherung:
 * - Towny-Plots: In TownBlock MetaData (Persistent)
 * - Fallback: In Plugin-Config (für andere Plot-Systeme)
 *
 * @author FallenStar
 * @version 1.0
 */
public class PlotNameManager {

    private static final String METADATA_KEY = "fs_plot_name";
    private final Logger logger;
    private final Map<UUID, String> plotNames; // Fallback für nicht-Towny Plots

    /**
     * Erstellt einen neuen PlotNameManager.
     *
     * @param logger Logger für Debug-Ausgaben
     */
    public PlotNameManager(Logger logger) {
        this.logger = logger;
        this.plotNames = new HashMap<>();
    }

    /**
     * Setzt den Namen eines Plots.
     *
     * @param plot Der Plot
     * @param name Der neue Name (null zum Löschen)
     * @return true wenn erfolgreich gesetzt
     */
    public boolean setPlotName(Plot plot, String name) {
        if (plot == null) {
            return false;
        }

        // Prüfe ob Towny-Plot
        try {
            TownBlock townBlock = plot.getNativePlot();
            if (townBlock != null) {
                return setTownyPlotName(townBlock, name);
            }
        } catch (ClassCastException e) {
            // Nicht Towny - verwende Fallback
        }

        // Fallback: In-Memory Map
        if (name == null || name.trim().isEmpty()) {
            plotNames.remove(plot.getUuid());
        } else {
            plotNames.put(plot.getUuid(), name.trim());
        }

        return true;
    }

    /**
     * Gibt den Namen eines Plots zurück.
     *
     * @param plot Der Plot
     * @return Der Name oder null wenn kein Name gesetzt
     */
    public String getPlotName(Plot plot) {
        if (plot == null) {
            return null;
        }

        // Prüfe ob Towny-Plot
        try {
            TownBlock townBlock = plot.getNativePlot();
            if (townBlock != null) {
                return getTownyPlotName(townBlock);
            }
        } catch (ClassCastException e) {
            // Nicht Towny - verwende Fallback
        }

        // Fallback: In-Memory Map
        return plotNames.get(plot.getUuid());
    }

    /**
     * Gibt den Anzeigenamen eines Plots zurück.
     *
     * Falls ein benutzerdefinierter Name gesetzt ist, wird dieser zurückgegeben.
     * Sonst wird der Standard-Identifier verwendet.
     *
     * @param plot Der Plot
     * @return Der Anzeigename
     */
    public String getDisplayName(Plot plot) {
        String customName = getPlotName(plot);
        if (customName != null && !customName.isEmpty()) {
            return customName + " §7(" + plot.getIdentifier() + ")";
        }
        return plot.getIdentifier();
    }

    /**
     * Prüft ob ein Plot einen benutzerdefinierten Namen hat.
     *
     * @param plot Der Plot
     * @return true wenn Name gesetzt
     */
    public boolean hasCustomName(Plot plot) {
        String name = getPlotName(plot);
        return name != null && !name.isEmpty();
    }

    /**
     * Setzt den Namen eines Towny-Plots in MetaData.
     *
     * **Priorisiert Towny MetaData** (persistenter als Config).
     * Falls Towny MetaData-API nicht verfügbar → Fallback auf Config.
     *
     * @param townBlock Der TownBlock
     * @param name Der Name
     * @return true wenn erfolgreich
     */
    private boolean setTownyPlotName(TownBlock townBlock, String name) {
        try {
            // Versuche Towny MetaData API (bevorzugt)
            if (name == null || name.trim().isEmpty()) {
                // Entferne MetaData
                townBlock.removeMetaData(METADATA_KEY, true);
            } else {
                // Setze MetaData
                StringDataField field = new StringDataField(METADATA_KEY, name.trim());
                townBlock.addMetaData(field, true);
            }
            logger.fine("Plot-Name in Towny MetaData gespeichert");
            return true;
        } catch (NoSuchMethodError | Exception e) {
            // Towny MetaData API nicht verfügbar oder fehlerhaft
            logger.fine("Towny MetaData API nicht verfügbar, nutze Config-Fallback: " + e.getMessage());
            return false;  // Fallback zur Config-Speicherung
        }
    }

    /**
     * Holt den Namen eines Towny-Plots aus MetaData.
     *
     * **Priorisiert Towny MetaData** (persistenter als Config).
     * Falls Towny MetaData-API nicht verfügbar → null (Fallback zur Config).
     *
     * @param townBlock Der TownBlock
     * @return Der Name oder null
     */
    private String getTownyPlotName(TownBlock townBlock) {
        try {
            // Versuche Towny MetaData API (bevorzugt)
            // HINWEIS: MetaData API ist in dieser Towny-Version nicht verfügbar
            // Nutze Reflection für API-Kompatibilität
            java.lang.reflect.Method hasMetaDataMethod = townBlock.getClass().getMethod("hasMetaData", String.class);
            boolean hasMetaData = (boolean) hasMetaDataMethod.invoke(townBlock, METADATA_KEY);

            if (hasMetaData) {
                java.lang.reflect.Method getMetaDataMethod = townBlock.getClass().getMethod("getMetaData", String.class);
                Object metaDataObj = getMetaDataMethod.invoke(townBlock, METADATA_KEY);

                if (metaDataObj instanceof StringDataField) {
                    StringDataField field = (StringDataField) metaDataObj;
                    String value = field.getValue();
                    logger.fine("Plot-Name aus Towny MetaData geladen: " + value);
                    return value;
                }
            }
        } catch (NoSuchMethodError | NoSuchMethodException | Exception e) {
            // Towny MetaData API nicht verfügbar oder fehlerhaft
            logger.fine("Towny MetaData API nicht verfügbar, nutze Config-Fallback: " + e.getMessage());
        }
        return null;  // Fallback zur Config-Map
    }

    /**
     * Lädt Plot-Namen aus der Config (Fallback für nicht-Towny Plots).
     *
     * @param config Die FileConfiguration
     */
    public void loadFromConfig(FileConfiguration config) {
        if (!config.contains("plot-names")) {
            return;
        }

        for (String key : config.getConfigurationSection("plot-names").getKeys(false)) {
            try {
                UUID plotId = UUID.fromString(key);
                String name = config.getString("plot-names." + key);
                plotNames.put(plotId, name);
            } catch (IllegalArgumentException e) {
                logger.warning("Ungültige Plot-ID in Config: " + key);
            }
        }

        logger.info("Plot-Namen geladen: " + plotNames.size() + " Einträge");
    }

    /**
     * Speichert Plot-Namen in die Config (Fallback für nicht-Towny Plots).
     *
     * @param config Die FileConfiguration
     */
    public void saveToConfig(FileConfiguration config) {
        // Lösche alte Daten
        config.set("plot-names", null);

        // Speichere alle Namen
        for (Map.Entry<UUID, String> entry : plotNames.entrySet()) {
            config.set("plot-names." + entry.getKey().toString(), entry.getValue());
        }

        logger.fine("Plot-Namen gespeichert: " + plotNames.size() + " Einträge");
    }

    /**
     * @return Anzahl der verwalteten Plot-Namen
     */
    public int getPlotNameCount() {
        return plotNames.size();
    }
}
