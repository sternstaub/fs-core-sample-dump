package de.fallenstar.plot.registry;

import de.fallenstar.core.provider.Plot;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Registry für spezielle Grundstückstypen (Handelsgilden, Botschaften, etc.).
 *
 * Features:
 * - Auto-Registration via Towny-Events
 * - Auto-Deregistration bei Plot-Typ-Änderung oder Löschung
 * - Suche nach Grundstückstyp
 * - Persistent (in Config gespeichert)
 *
 * **Verwendung:**
 * <pre>
 * List<Plot> guilds = plotRegistry.getPlotsByType(PlotType.MERCHANT_GUILD);
 * </pre>
 *
 * **Towny-Integration:**
 * - PlotRegistryListener überwacht TownBlock-Events
 * - Automatische Registration bei Plot-Typ-Änderung (COMMERCIAL → MERCHANT_GUILD)
 * - Automatische Deregistration bei Plot-Löschung
 *
 * **Persistierung:**
 * - Config: plot-registry.yml
 * - Format: plot-types.<type>: [plot-id-1, plot-id-2, ...]
 *
 * @author FallenStar
 * @version 1.0
 */
public class PlotRegistry {

    private final Logger logger;
    private final Map<PlotType, Set<String>> registeredPlots;  // PlotType → Plot-IDs

    /**
     * Grundstücks-Typen für verschiedene Verwendungszwecke.
     */
    public enum PlotType {
        /**
         * Handelsgilde - Händler-Slots.
         */
        MERCHANT_GUILD("Handelsgilde"),

        /**
         * Botschaft - Botschafter-Slots.
         */
        EMBASSY("Botschaft"),

        /**
         * Bank - Bankier-Slots.
         */
        BANK("Bank"),

        /**
         * Werkstatt - Handwerker-Slots.
         */
        WORKSHOP("Werkstatt");

        private final String displayName;

        PlotType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * Konstruktor für PlotRegistry.
     *
     * @param logger Logger
     */
    public PlotRegistry(Logger logger) {
        this.logger = logger;
        this.registeredPlots = new ConcurrentHashMap<>();

        // Initialisiere Maps für alle Typen
        for (PlotType type : PlotType.values()) {
            registeredPlots.put(type, ConcurrentHashMap.newKeySet());
        }

        logger.info("PlotRegistry initialisiert");
    }

    /**
     * Registriert ein Grundstück.
     *
     * @param plot Der Plot
     * @param type Der Plot-Typ
     * @return true wenn erfolgreich registriert, false wenn bereits registriert
     */
    public boolean registerPlot(Plot plot, PlotType type) {
        if (plot == null || type == null) {
            logger.warning("Versuch ein null-Plot oder null-Type zu registrieren!");
            return false;
        }

        String plotId = plot.getPlotId();
        Set<String> plots = registeredPlots.get(type);

        if (plots.add(plotId)) {
            logger.info("Plot registriert: " + plotId + " als " + type.getDisplayName());
            return true;
        }

        logger.fine("Plot bereits registriert: " + plotId + " als " + type.getDisplayName());
        return false;
    }

    /**
     * De-registriert ein Grundstück.
     *
     * Entfernt das Plot aus ALLEN Typen.
     *
     * @param plot Der Plot
     * @return true wenn erfolgreich deregistriert
     */
    public boolean unregisterPlot(Plot plot) {
        if (plot == null) {
            logger.warning("Versuch ein null-Plot zu deregistrieren!");
            return false;
        }

        String plotId = plot.getPlotId();
        boolean removed = false;

        for (Map.Entry<PlotType, Set<String>> entry : registeredPlots.entrySet()) {
            if (entry.getValue().remove(plotId)) {
                logger.info("Plot deregistriert: " + plotId + " von " + entry.getKey().getDisplayName());
                removed = true;
            }
        }

        return removed;
    }

    /**
     * Gibt alle Grundstücke eines Typs zurück.
     *
     * HINWEIS: Gibt nur Plot-IDs zurück (String).
     * Um Plot-Objekte zu erhalten, nutze PlotProvider.getPlot().
     *
     * @param type Der Plot-Typ
     * @return Liste von Plot-IDs
     */
    public List<String> getPlotIdsByType(PlotType type) {
        if (type == null) {
            return Collections.emptyList();
        }

        return new ArrayList<>(registeredPlots.getOrDefault(type, Collections.emptySet()));
    }

    /**
     * Prüft ob ein Grundstück registriert ist.
     *
     * @param plot Der Plot
     * @return true wenn registriert (in irgendeinem Typ)
     */
    public boolean isRegistered(Plot plot) {
        if (plot == null) {
            return false;
        }

        String plotId = plot.getPlotId();

        for (Set<String> plots : registeredPlots.values()) {
            if (plots.contains(plotId)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Gibt den Typ eines Grundstücks zurück.
     *
     * @param plot Der Plot
     * @return Optional mit PlotType, oder empty wenn nicht registriert
     */
    public Optional<PlotType> getPlotType(Plot plot) {
        if (plot == null) {
            return Optional.empty();
        }

        String plotId = plot.getPlotId();

        for (Map.Entry<PlotType, Set<String>> entry : registeredPlots.entrySet()) {
            if (entry.getValue().contains(plotId)) {
                return Optional.of(entry.getKey());
            }
        }

        return Optional.empty();
    }

    /**
     * Gibt die Anzahl registrierter Plots für einen Typ zurück.
     *
     * @param type Der Plot-Typ
     * @return Anzahl Plots
     */
    public int getPlotCount(PlotType type) {
        return registeredPlots.getOrDefault(type, Collections.emptySet()).size();
    }

    /**
     * Gibt die Gesamtanzahl registrierter Plots zurück.
     *
     * @return Anzahl Plots (über alle Typen)
     */
    public int getTotalPlotCount() {
        return registeredPlots.values().stream()
                .mapToInt(Set::size)
                .sum();
    }

    /**
     * Lädt Plots aus Config.
     *
     * @param config FileConfiguration
     */
    public void loadFromConfig(FileConfiguration config) {
        ConfigurationSection section = config.getConfigurationSection("plot-types");
        if (section == null) {
            logger.warning("Keine Plot-Registry in Config gefunden");
            return;
        }

        int loadedCount = 0;

        for (PlotType type : PlotType.values()) {
            List<String> plotIds = section.getStringList(type.name());
            Set<String> plots = registeredPlots.get(type);

            plots.clear();
            plots.addAll(plotIds);

            loadedCount += plotIds.size();
        }

        logger.info("PlotRegistry geladen: " + loadedCount + " Plots");
    }

    /**
     * Speichert Plots in Config.
     *
     * @param config FileConfiguration
     */
    public void saveToConfig(FileConfiguration config) {
        // Lösche alte Daten
        config.set("plot-types", null);

        // Schreibe alle Typen
        for (Map.Entry<PlotType, Set<String>> entry : registeredPlots.entrySet()) {
            PlotType type = entry.getKey();
            List<String> plotIds = new ArrayList<>(entry.getValue());

            config.set("plot-types." + type.name(), plotIds);
        }

        logger.info("PlotRegistry gespeichert: " + getTotalPlotCount() + " Plots");
    }

    /**
     * Gibt eine Debug-Übersicht zurück.
     *
     * @return Liste von Strings
     */
    public List<String> getDebugInfo() {
        List<String> info = new ArrayList<>();
        info.add("§e§lPlotRegistry Debug-Info:");
        info.add("");

        for (PlotType type : PlotType.values()) {
            int count = getPlotCount(type);
            info.add("§6" + type.getDisplayName() + ": §7" + count + " Plots");

            if (count > 0) {
                List<String> plotIds = getPlotIdsByType(type);
                for (String plotId : plotIds) {
                    info.add("  §8- " + plotId);
                }
            }
        }

        info.add("");
        info.add("§eGesamt: §7" + getTotalPlotCount() + " Plots");

        return info;
    }
}
