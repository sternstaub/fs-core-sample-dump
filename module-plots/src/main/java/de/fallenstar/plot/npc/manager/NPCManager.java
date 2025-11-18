package de.fallenstar.plot.npc.manager;

import de.fallenstar.plot.npc.model.PlotNPCType;
import de.fallenstar.plot.npc.model.PlotNPC;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Manager für Plot-NPCs mit Persistenz.
 *
 * Funktionen:
 * - NPC Spawn/Despawn
 * - NPC-Verwaltung pro Plot
 * - Persistenz via Config
 * - SourcePlot-Zuordnung
 *
 * **Persistenz:**
 * NPCs werden in der config.yml gespeichert und beim Server-Start geladen.
 *
 * @author FallenStar
 * @version 1.0
 */
public class NPCManager {

    private final Logger logger;
    private final Map<UUID, PlotNPC> npcMap;           // npcId -> PlotNPC
    private final Map<UUID, List<UUID>> plotNPCMap;    // plotId -> List<npcId>

    /**
     * Erstellt einen neuen NPCManager.
     *
     * @param logger Logger-Instanz
     */
    public NPCManager(Logger logger) {
        this.logger = logger;
        this.npcMap = new HashMap<>();
        this.plotNPCMap = new HashMap<>();
    }

    /**
     * Lädt NPCs aus der Config.
     *
     * Format:
     * ```yaml
     * npcs:
     *   npc-uuid-1:
     *     type: GUILD_TRADER
     *     host-plot: plot-uuid
     *     source-plot: plot-uuid
     *     location:
     *       world: world
     *       x: 100.5
     *       y: 64.0
     *       z: 200.5
     *       yaw: 0.0
     *       pitch: 0.0
     *     owner: player-uuid (optional)
     *     custom-name: "Mein Händler" (optional)
     * ```
     *
     * @param config Die Config
     */
    public void loadFromConfig(FileConfiguration config) {
        ConfigurationSection npcsSection = config.getConfigurationSection("npcs");
        if (npcsSection == null) {
            logger.info("Keine NPCs in Config gefunden");
            return;
        }

        int loadedCount = 0;

        for (String npcIdString : npcsSection.getKeys(false)) {
            try {
                UUID npcId = UUID.fromString(npcIdString);
                ConfigurationSection npcSection = npcsSection.getConfigurationSection(npcIdString);

                // Parse NPC-Daten
                PlotNPCType type = PlotNPCType.valueOf(npcSection.getString("type"));
                UUID hostPlotId = UUID.fromString(npcSection.getString("host-plot"));
                UUID sourcePlotId = UUID.fromString(npcSection.getString("source-plot"));

                // Parse Location
                ConfigurationSection locSection = npcSection.getConfigurationSection("location");
                World world = Bukkit.getWorld(locSection.getString("world"));
                if (world == null) {
                    logger.warning("Welt nicht gefunden für NPC: " + npcId);
                    continue;
                }

                Location location = new Location(
                        world,
                        locSection.getDouble("x"),
                        locSection.getDouble("y"),
                        locSection.getDouble("z"),
                        (float) locSection.getDouble("yaw"),
                        (float) locSection.getDouble("pitch")
                );

                // Optional: Owner
                UUID ownerUUID = null;
                if (npcSection.contains("owner")) {
                    ownerUUID = UUID.fromString(npcSection.getString("owner"));
                }

                // Optional: Custom-Name
                String customName = npcSection.getString("custom-name", null);

                // Erstelle PlotNPC
                PlotNPC plotNPC = new PlotNPC(
                        npcId,
                        type,
                        hostPlotId,
                        sourcePlotId,
                        location,
                        ownerUUID,
                        customName
                );

                // Registriere NPC
                registerNPC(plotNPC);
                loadedCount++;

            } catch (Exception e) {
                logger.warning("Fehler beim Laden von NPC " + npcIdString + ": " + e.getMessage());
            }
        }

        logger.info("NPCs geladen: " + loadedCount + " Einträge");
    }

    /**
     * Speichert NPCs zurück in die Config.
     *
     * WICHTIG: Muss nach JEDER NPC-Änderung aufgerufen werden!
     *
     * @param config Die Config
     */
    public void saveToConfig(FileConfiguration config) {
        // Lösche alte NPCs
        config.set("npcs", null);

        // Speichere alle NPCs
        for (PlotNPC npc : npcMap.values()) {
            String path = "npcs." + npc.getNpcId().toString();

            config.set(path + ".type", npc.getType().name());
            config.set(path + ".host-plot", npc.getHostPlotId().toString());
            config.set(path + ".source-plot", npc.getSourcePlotId().toString());

            // Location
            Location loc = npc.getLocation();
            config.set(path + ".location.world", loc.getWorld().getName());
            config.set(path + ".location.x", loc.getX());
            config.set(path + ".location.y", loc.getY());
            config.set(path + ".location.z", loc.getZ());
            config.set(path + ".location.yaw", loc.getYaw());
            config.set(path + ".location.pitch", loc.getPitch());

            // Optional: Owner
            if (npc.hasOwner()) {
                config.set(path + ".owner", npc.getOwnerUUID().toString());
            }

            // Optional: Custom-Name
            if (npc.getCustomName() != null) {
                config.set(path + ".custom-name", npc.getCustomName());
            }
        }

        logger.info("NPCs gespeichert: " + npcMap.size() + " Einträge");
    }

    /**
     * Registriert einen NPC im Manager.
     *
     * @param npc Der NPC
     */
    private void registerNPC(PlotNPC npc) {
        // In NPC-Map eintragen
        npcMap.put(npc.getNpcId(), npc);

        // In Plot-NPC-Map eintragen
        plotNPCMap.computeIfAbsent(npc.getHostPlotId(), k -> new ArrayList<>())
                  .add(npc.getNpcId());
    }

    /**
     * Erstellt einen neuen NPC.
     *
     * @param type NPC-Typ
     * @param hostPlotId Host-Plot ID
     * @param sourcePlotId Source-Plot ID
     * @param location Location
     * @param ownerUUID Owner-UUID (null für System-NPCs)
     * @param customName Custom-Name (null für Default-Namen)
     * @return Der erstellte NPC
     */
    public PlotNPC createNPC(
            PlotNPCType type,
            UUID hostPlotId,
            UUID sourcePlotId,
            Location location,
            UUID ownerUUID,
            String customName
    ) {
        UUID npcId = UUID.randomUUID();

        PlotNPC npc = new PlotNPC(
                npcId,
                type,
                hostPlotId,
                sourcePlotId,
                location,
                ownerUUID,
                customName
        );

        registerNPC(npc);

        logger.fine("NPC erstellt: " + npc.getDisplayName() + " (" + npc.getNpcId() + ")");

        return npc;
    }

    /**
     * Entfernt einen NPC.
     *
     * @param npcId NPC-ID
     * @return true wenn erfolgreich entfernt
     */
    public boolean removeNPC(UUID npcId) {
        PlotNPC npc = npcMap.remove(npcId);
        if (npc == null) {
            return false;
        }

        // Entferne aus Plot-NPC-Map
        List<UUID> plotNPCs = plotNPCMap.get(npc.getHostPlotId());
        if (plotNPCs != null) {
            plotNPCs.remove(npcId);
            if (plotNPCs.isEmpty()) {
                plotNPCMap.remove(npc.getHostPlotId());
            }
        }

        logger.fine("NPC entfernt: " + npc.getDisplayName() + " (" + npcId + ")");

        return true;
    }

    /**
     * Gibt einen NPC zurück.
     *
     * @param npcId NPC-ID
     * @return Der NPC oder null
     */
    public PlotNPC getNPC(UUID npcId) {
        return npcMap.get(npcId);
    }

    /**
     * Gibt alle NPCs auf einem Plot zurück.
     *
     * @param plotId Plot-ID
     * @return Liste aller NPCs auf dem Plot
     */
    public List<PlotNPC> getPlotNPCs(UUID plotId) {
        List<UUID> npcIds = plotNPCMap.get(plotId);
        if (npcIds == null || npcIds.isEmpty()) {
            return Collections.emptyList();
        }

        return npcIds.stream()
                .map(npcMap::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * Gibt alle NPCs eines bestimmten Typs zurück.
     *
     * @param type NPC-Typ
     * @return Liste aller NPCs dieses Typs
     */
    public List<PlotNPC> getNPCsByType(PlotNPCType type) {
        return npcMap.values().stream()
                .filter(npc -> npc.getType() == type)
                .collect(Collectors.toList());
    }

    /**
     * Gibt alle NPCs eines Spielers zurück.
     *
     * @param ownerUUID Owner-UUID
     * @return Liste aller NPCs des Spielers
     */
    public List<PlotNPC> getPlayerNPCs(UUID ownerUUID) {
        return npcMap.values().stream()
                .filter(npc -> npc.hasOwner() && npc.getOwnerUUID().equals(ownerUUID))
                .collect(Collectors.toList());
    }

    /**
     * Gibt die Anzahl aller NPCs zurück.
     *
     * @return Anzahl NPCs
     */
    public int getNPCCount() {
        return npcMap.size();
    }

    /**
     * Gibt die Anzahl NPCs auf einem Plot zurück.
     *
     * @param plotId Plot-ID
     * @return Anzahl NPCs
     */
    public int getPlotNPCCount(UUID plotId) {
        List<UUID> npcIds = plotNPCMap.get(plotId);
        return (npcIds != null) ? npcIds.size() : 0;
    }
}
