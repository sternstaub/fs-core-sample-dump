package de.fallenstar.plot.manager;

import de.fallenstar.core.provider.Plot;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Registry für Plot-gebundene NPCs.
 *
 * Verwaltet die Zuordnung von NPCs zu Grundstücken für das PlotBoundNPC-System.
 *
 * **Wichtig:** Die Bindung ist für **Verwaltung und Ownership**, nicht für Position!
 * Ein PlotBoundNPC kann auf anderen Grundstücken stehen, gehört aber immer zu einem
 * spezifischen Plot für Management-Zwecke.
 *
 * **Verwendung:**
 * ```java
 * PlotBoundNPCRegistry registry = plotsModule.getNPCRegistry();
 *
 * // NPC registrieren
 * registry.registerNPC(plotId, npcId, "guild_trader", location);
 *
 * // NPCs für Plot abrufen
 * List<NPCInfo> npcs = registry.getNPCsForPlot(plotId);
 *
 * // Plot für NPC ermitteln
 * Optional<UUID> plotId = registry.getPlotForNPC(npcId);
 * ```
 *
 * **Config-Persistierung:**
 * ```yaml
 * plot-npcs:
 *   plot-uuid-123:
 *     - npc-id: npc-uuid-456
 *       type: guild_trader
 *       world: world
 *       x: 100.5
 *       y: 64.0
 *       z: 200.5
 *       yaw: 0.0
 *       pitch: 0.0
 *       spawn-time: 1234567890
 * ```
 *
 * @author FallenStar
 * @version 1.0
 */
public class PlotBoundNPCRegistry {

    /**
     * Informationen über einen Plot-gebundenen NPC.
     *
     * @param npcId NPC-UUID
     * @param npcType NPC-Typ (z.B. "guild_trader", "player_trader")
     * @param spawnLocation Spawn-Position
     * @param spawnTime Zeitpunkt des Spawns (Unix timestamp)
     */
    public record NPCInfo(UUID npcId, String npcType, Location spawnLocation, long spawnTime) {

        /**
         * Serialisiert die NPC-Info in eine Config-Section.
         *
         * @param section Ziel-Section
         */
        public void saveToConfig(ConfigurationSection section) {
            section.set("npc-id", npcId.toString());
            section.set("type", npcType);

            if (spawnLocation != null && spawnLocation.getWorld() != null) {
                section.set("world", spawnLocation.getWorld().getName());
                section.set("x", spawnLocation.getX());
                section.set("y", spawnLocation.getY());
                section.set("z", spawnLocation.getZ());
                section.set("yaw", spawnLocation.getYaw());
                section.set("pitch", spawnLocation.getPitch());
            }

            section.set("spawn-time", spawnTime);
        }

        /**
         * Deserialisiert NPC-Info aus einer Config-Section.
         *
         * @param section Quell-Section
         * @return NPCInfo oder null bei Fehler
         */
        public static NPCInfo loadFromConfig(ConfigurationSection section) {
            try {
                UUID npcId = UUID.fromString(section.getString("npc-id"));
                String npcType = section.getString("type");
                long spawnTime = section.getLong("spawn-time");

                Location location = null;
                if (section.contains("world")) {
                    World world = Bukkit.getWorld(section.getString("world"));
                    if (world != null) {
                        double x = section.getDouble("x");
                        double y = section.getDouble("y");
                        double z = section.getDouble("z");
                        float yaw = (float) section.getDouble("yaw");
                        float pitch = (float) section.getDouble("pitch");
                        location = new Location(world, x, y, z, yaw, pitch);
                    }
                }

                return new NPCInfo(npcId, npcType, location, spawnTime);

            } catch (Exception e) {
                return null;
            }
        }
    }

    private final Plugin plugin;
    private final Logger logger;

    /**
     * Plot-UUID → Liste von NPCs
     */
    private final Map<UUID, List<NPCInfo>> plotNPCs;

    /**
     * NPC-UUID → Plot-UUID (Reverse Lookup)
     */
    private final Map<UUID, UUID> npcToPlot;

    /**
     * Erstellt eine neue PlotBoundNPCRegistry.
     *
     * @param plugin Plugin-Instanz
     * @param logger Logger
     */
    public PlotBoundNPCRegistry(Plugin plugin, Logger logger) {
        this.plugin = Objects.requireNonNull(plugin, "plugin cannot be null");
        this.logger = Objects.requireNonNull(logger, "logger cannot be null");
        this.plotNPCs = new HashMap<>();
        this.npcToPlot = new HashMap<>();
    }

    // ==================== Registry-Methoden ====================

    /**
     * Registriert einen NPC für ein Plot.
     *
     * @param plot Das Plot
     * @param npcId NPC-UUID
     * @param npcType NPC-Typ
     * @param spawnLocation Spawn-Position
     */
    public void registerNPC(Plot plot, UUID npcId, String npcType, Location spawnLocation) {
        Objects.requireNonNull(plot, "plot cannot be null");
        Objects.requireNonNull(npcId, "npcId cannot be null");
        Objects.requireNonNull(npcType, "npcType cannot be null");

        UUID plotId = plot.getUuid();

        // Prüfe ob NPC bereits registriert ist
        if (npcToPlot.containsKey(npcId)) {
            UUID existingPlot = npcToPlot.get(npcId);
            if (!existingPlot.equals(plotId)) {
                logger.warning("NPC " + npcId + " ist bereits an Plot " + existingPlot + " gebunden! Entferne alte Bindung.");
                unregisterNPC(npcId);
            } else {
                logger.warning("NPC " + npcId + " ist bereits an Plot " + plotId + " gebunden!");
                return;
            }
        }

        // Erstelle NPCInfo
        NPCInfo info = new NPCInfo(npcId, npcType, spawnLocation, System.currentTimeMillis());

        // Füge zur Plot-Liste hinzu
        plotNPCs.computeIfAbsent(plotId, k -> new ArrayList<>()).add(info);

        // Füge Reverse-Lookup hinzu
        npcToPlot.put(npcId, plotId);

        logger.info("NPC " + npcId + " (Typ: " + npcType + ") an Plot " + plotId + " gebunden");
    }

    /**
     * Entfernt einen NPC aus der Registry.
     *
     * @param npcId NPC-UUID
     * @return true wenn entfernt, false wenn nicht gefunden
     */
    public boolean unregisterNPC(UUID npcId) {
        Objects.requireNonNull(npcId, "npcId cannot be null");

        UUID plotId = npcToPlot.remove(npcId);
        if (plotId == null) {
            return false;
        }

        List<NPCInfo> npcs = plotNPCs.get(plotId);
        if (npcs != null) {
            npcs.removeIf(info -> info.npcId().equals(npcId));

            // Entferne leere Liste
            if (npcs.isEmpty()) {
                plotNPCs.remove(plotId);
            }
        }

        logger.info("NPC " + npcId + " von Plot " + plotId + " entfernt");
        return true;
    }

    /**
     * Gibt alle NPCs für ein Plot zurück.
     *
     * @param plot Das Plot
     * @return Liste aller NPCs (unveränderlich)
     */
    public List<NPCInfo> getNPCsForPlot(Plot plot) {
        Objects.requireNonNull(plot, "plot cannot be null");

        List<NPCInfo> npcs = plotNPCs.get(plot.getUuid());
        return npcs != null ? Collections.unmodifiableList(npcs) : Collections.emptyList();
    }

    /**
     * Gibt die Plot-UUID für einen NPC zurück.
     *
     * @param npcId NPC-UUID
     * @return Plot-UUID oder Empty
     */
    public Optional<UUID> getPlotForNPC(UUID npcId) {
        Objects.requireNonNull(npcId, "npcId cannot be null");
        return Optional.ofNullable(npcToPlot.get(npcId));
    }

    /**
     * Prüft ob ein NPC an ein Plot gebunden ist.
     *
     * @param npcId NPC-UUID
     * @return true wenn gebunden
     */
    public boolean isNPCBound(UUID npcId) {
        return npcToPlot.containsKey(npcId);
    }

    /**
     * Prüft ob ein NPC an ein bestimmtes Plot gebunden ist.
     *
     * @param plot Das Plot
     * @param npcId NPC-UUID
     * @return true wenn an dieses Plot gebunden
     */
    public boolean isNPCBoundToPlot(Plot plot, UUID npcId) {
        Objects.requireNonNull(plot, "plot cannot be null");
        Objects.requireNonNull(npcId, "npcId cannot be null");

        UUID boundPlot = npcToPlot.get(npcId);
        return boundPlot != null && boundPlot.equals(plot.getUuid());
    }

    /**
     * Entfernt alle NPCs für ein Plot.
     *
     * @param plot Das Plot
     * @return Anzahl entfernter NPCs
     */
    public int clearPlotNPCs(Plot plot) {
        Objects.requireNonNull(plot, "plot cannot be null");

        List<NPCInfo> npcs = plotNPCs.remove(plot.getUuid());
        if (npcs == null) {
            return 0;
        }

        // Entferne Reverse-Lookups
        for (NPCInfo info : npcs) {
            npcToPlot.remove(info.npcId());
        }

        logger.info("Alle " + npcs.size() + " NPCs von Plot " + plot.getUuid() + " entfernt");
        return npcs.size();
    }

    /**
     * Gibt die Anzahl registrierter NPCs zurück.
     *
     * @return Anzahl NPCs
     */
    public int getNPCCount() {
        return npcToPlot.size();
    }

    /**
     * Gibt die Anzahl Plots mit NPCs zurück.
     *
     * @return Anzahl Plots
     */
    public int getPlotCount() {
        return plotNPCs.size();
    }

    /**
     * Gibt alle registrierten NPC-IDs zurück.
     *
     * @return Unveränderliche Menge aller NPC-IDs
     */
    public Set<UUID> getAllNPCIds() {
        return Collections.unmodifiableSet(npcToPlot.keySet());
    }

    /**
     * Gibt alle Plot-IDs mit NPCs zurück.
     *
     * @return Unveränderliche Menge aller Plot-IDs
     */
    public Set<UUID> getAllPlotIds() {
        return Collections.unmodifiableSet(plotNPCs.keySet());
    }

    /**
     * Gibt NPCs nach Typ zurück.
     *
     * @param plot Das Plot
     * @param npcType NPC-Typ
     * @return Liste aller NPCs dieses Typs (unveränderlich)
     */
    public List<NPCInfo> getNPCsByType(Plot plot, String npcType) {
        Objects.requireNonNull(plot, "plot cannot be null");
        Objects.requireNonNull(npcType, "npcType cannot be null");

        List<NPCInfo> npcs = plotNPCs.get(plot.getUuid());
        if (npcs == null) {
            return Collections.emptyList();
        }

        return npcs.stream()
                .filter(info -> info.npcType().equals(npcType))
                .collect(Collectors.toUnmodifiableList());
    }

    /**
     * Gibt die NPC-Info für eine bestimmte NPC-ID zurück.
     *
     * @param npcId NPC-UUID
     * @return NPCInfo oder Empty
     */
    public Optional<NPCInfo> getNPCInfo(UUID npcId) {
        Objects.requireNonNull(npcId, "npcId cannot be null");

        UUID plotId = npcToPlot.get(npcId);
        if (plotId == null) {
            return Optional.empty();
        }

        List<NPCInfo> npcs = plotNPCs.get(plotId);
        if (npcs == null) {
            return Optional.empty();
        }

        return npcs.stream()
                .filter(info -> info.npcId().equals(npcId))
                .findFirst();
    }

    // ==================== Config-Persistierung ====================

    /**
     * Lädt Plot-NPCs aus der Config.
     *
     * @param config Die FileConfiguration
     */
    public void loadFromConfig(FileConfiguration config) {
        plotNPCs.clear();
        npcToPlot.clear();

        ConfigurationSection section = config.getConfigurationSection("plot-npcs");
        if (section == null) {
            logger.info("Keine Plot-NPCs in Config gefunden");
            return;
        }

        int loaded = 0;
        for (String plotIdStr : section.getKeys(false)) {
            try {
                UUID plotId = UUID.fromString(plotIdStr);
                ConfigurationSection plotSection = section.getConfigurationSection(plotIdStr);

                if (plotSection != null) {
                    List<NPCInfo> npcs = new ArrayList<>();

                    // Liste von NPCs laden
                    for (String key : plotSection.getKeys(false)) {
                        ConfigurationSection npcSection = plotSection.getConfigurationSection(key);
                        if (npcSection != null) {
                            NPCInfo info = NPCInfo.loadFromConfig(npcSection);
                            if (info != null) {
                                npcs.add(info);
                                npcToPlot.put(info.npcId(), plotId);
                                loaded++;
                            }
                        }
                    }

                    if (!npcs.isEmpty()) {
                        plotNPCs.put(plotId, npcs);
                    }
                }

            } catch (IllegalArgumentException e) {
                logger.warning("Ungültige Plot-UUID in Config: " + plotIdStr);
            }
        }

        logger.info("Geladen: " + loaded + " NPCs auf " + plotNPCs.size() + " Plots");
    }

    /**
     * Speichert Plot-NPCs in die Config.
     *
     * @param config Die FileConfiguration
     */
    public void saveToConfig(FileConfiguration config) {
        // Clear alte Daten
        config.set("plot-npcs", null);

        if (plotNPCs.isEmpty()) {
            return;
        }

        // Speichere NPCs
        int saved = 0;
        for (Map.Entry<UUID, List<NPCInfo>> entry : plotNPCs.entrySet()) {
            String plotIdStr = entry.getKey().toString();
            List<NPCInfo> npcs = entry.getValue();

            int index = 0;
            for (NPCInfo info : npcs) {
                ConfigurationSection npcSection = config.createSection("plot-npcs." + plotIdStr + ".npc-" + index);
                info.saveToConfig(npcSection);
                index++;
                saved++;
            }
        }

        logger.fine("Gespeichert: " + saved + " NPCs auf " + plotNPCs.size() + " Plots");
    }
}
