package de.fallenstar.core.registry;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * Registry für Plot-Typen und deren zugeordnete NPCs.
 *
 * Verwaltet:
 * - Plot-Type Definitionen (embassy, bank, market, etc.)
 * - NPC-Zuordnungen zu Plots
 * - Plot-Type Validierung
 *
 * Pattern: Registry Pattern
 *
 * @author FallenStar
 * @version 1.0
 */
public class PlotTypeRegistry {

    private final Logger logger;

    // Bekannte Plot-Typen (von Towny)
    private final Set<String> validPlotTypes;

    // UUID des Plots -> Liste von NPC UUIDs
    private final Map<UUID, List<UUID>> plotNPCs;

    // Plot-Type -> Standard-NPC-Type (z.B. "embassy" -> "ambassador")
    private final Map<String, String> defaultNPCTypes;

    /**
     * Erstellt eine neue PlotTypeRegistry.
     *
     * @param logger Logger für Debug-Ausgaben
     */
    public PlotTypeRegistry(Logger logger) {
        this.logger = logger;
        this.validPlotTypes = new HashSet<>();
        this.plotNPCs = new ConcurrentHashMap<>();
        this.defaultNPCTypes = new HashMap<>();

        initializeDefaultTypes();
    }

    /**
     * Initialisiert Standard-Plot-Typen aus Towny.
     */
    private void initializeDefaultTypes() {
        // Towny Standard-Typen
        validPlotTypes.add("default");
        validPlotTypes.add("shop");
        validPlotTypes.add("embassy");
        validPlotTypes.add("arena");
        validPlotTypes.add("wilds");
        validPlotTypes.add("inn");
        validPlotTypes.add("jail");
        validPlotTypes.add("farm");
        validPlotTypes.add("bank");

        // Standard-NPC-Zuordnungen
        defaultNPCTypes.put("embassy", "ambassador");
        defaultNPCTypes.put("bank", "banker");

        logger.info("PlotTypeRegistry initialized with " + validPlotTypes.size() + " plot types");
    }

    /**
     * Registriert einen neuen Plot-Typ.
     *
     * @param plotType Der Plot-Typ
     */
    public void registerPlotType(String plotType) {
        if (plotType != null && !plotType.isEmpty()) {
            validPlotTypes.add(plotType.toLowerCase());
            logger.fine("Registered plot type: " + plotType);
        }
    }

    /**
     * Prüft ob ein Plot-Typ gültig ist.
     *
     * @param plotType Der zu prüfende Plot-Typ
     * @return true wenn gültig
     */
    public boolean isValidPlotType(String plotType) {
        if (plotType == null) {
            return false;
        }
        return validPlotTypes.contains(plotType.toLowerCase());
    }

    /**
     * Gibt alle registrierten Plot-Typen zurück.
     *
     * @return Set aller Plot-Typen
     */
    public Set<String> getPlotTypes() {
        return Collections.unmodifiableSet(validPlotTypes);
    }

    /**
     * Registriert einen NPC für ein Plot.
     *
     * @param plotId UUID des Plots
     * @param npcId UUID des NPCs
     */
    public void registerNPCForPlot(UUID plotId, UUID npcId) {
        plotNPCs.computeIfAbsent(plotId, k -> new ArrayList<>()).add(npcId);
        logger.fine("Registered NPC " + npcId + " for plot " + plotId);
    }

    /**
     * Entfernt einen NPC von einem Plot.
     *
     * @param plotId UUID des Plots
     * @param npcId UUID des NPCs
     * @return true wenn entfernt
     */
    public boolean unregisterNPCForPlot(UUID plotId, UUID npcId) {
        List<UUID> npcs = plotNPCs.get(plotId);
        if (npcs != null) {
            boolean removed = npcs.remove(npcId);
            if (npcs.isEmpty()) {
                plotNPCs.remove(plotId);
            }
            return removed;
        }
        return false;
    }

    /**
     * Gibt alle NPCs für ein Plot zurück.
     *
     * @param plotId UUID des Plots
     * @return Liste der NPC-UUIDs
     */
    public List<UUID> getNPCsForPlot(UUID plotId) {
        return plotNPCs.getOrDefault(plotId, Collections.emptyList());
    }

    /**
     * Gibt den Standard-NPC-Typ für einen Plot-Typ zurück.
     *
     * @param plotType Der Plot-Typ
     * @return NPC-Typ oder null wenn keiner definiert
     */
    public String getDefaultNPCType(String plotType) {
        if (plotType == null) {
            return null;
        }
        return defaultNPCTypes.get(plotType.toLowerCase());
    }

    /**
     * Setzt den Standard-NPC-Typ für einen Plot-Typ.
     *
     * @param plotType Der Plot-Typ
     * @param npcType Der NPC-Typ
     */
    public void setDefaultNPCType(String plotType, String npcType) {
        if (plotType != null && npcType != null) {
            defaultNPCTypes.put(plotType.toLowerCase(), npcType.toLowerCase());
            logger.fine("Set default NPC type for '" + plotType + "': " + npcType);
        }
    }

    /**
     * Prüft ob ein Plot NPCs hat.
     *
     * @param plotId UUID des Plots
     * @return true wenn NPCs vorhanden
     */
    public boolean hasNPCs(UUID plotId) {
        List<UUID> npcs = plotNPCs.get(plotId);
        return npcs != null && !npcs.isEmpty();
    }

    /**
     * Entfernt alle NPCs für ein Plot.
     *
     * @param plotId UUID des Plots
     * @return Anzahl der entfernten NPCs
     */
    public int removeAllNPCsForPlot(UUID plotId) {
        List<UUID> npcs = plotNPCs.remove(plotId);
        return (npcs != null) ? npcs.size() : 0;
    }

    /**
     * Gibt die Anzahl der registrierten Plot-Typen zurück.
     *
     * @return Anzahl Plot-Typen
     */
    public int getPlotTypeCount() {
        return validPlotTypes.size();
    }

    /**
     * Gibt die Anzahl der Plots mit NPCs zurück.
     *
     * @return Anzahl Plots
     */
    public int getPlotCount() {
        return plotNPCs.size();
    }
}
