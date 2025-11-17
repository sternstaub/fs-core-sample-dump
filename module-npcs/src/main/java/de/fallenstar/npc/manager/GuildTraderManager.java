package de.fallenstar.npc.manager;

import de.fallenstar.core.provider.Plot;
import de.fallenstar.core.provider.PlotProvider;
import de.fallenstar.npc.npctype.GuildTraderNPC;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;
import java.util.logging.Logger;

/**
 * Manager für Gildenhändler-NPCs.
 *
 * Funktionalität:
 * - Spawn/Despawn von Gildenhändlern
 * - Zuordnung NPC → Grundstück
 * - Persistierung in Config
 * - Cache-Invalidierung bei Storage-Änderungen
 *
 * Persistierung:
 * <pre>
 * guild-traders:
 *   npc-uuid-1:
 *     plot-uuid: "plot-uuid-123"
 *     location:
 *       world: "world"
 *       x: 100.5
 *       y: 64.0
 *       z: 200.5
 *       yaw: 0.0
 *       pitch: 0.0
 *   npc-uuid-2:
 *     # ...
 * </pre>
 *
 * @author FallenStar
 * @version 1.0
 */
public class GuildTraderManager {

    private final NPCManager npcManager;
    private final PlotProvider plotProvider;
    private final GuildTraderNPC guildTraderType;
    private final Logger logger;

    /**
     * Zuordnung: NPC-UUID → Plot-UUID
     */
    private final Map<UUID, UUID> npcPlotMapping;

    /**
     * Zuordnung: Plot-UUID → Liste von NPC-UUIDs
     * (Ein Plot kann mehrere Händler haben)
     */
    private final Map<UUID, List<UUID>> plotNPCMapping;

    /**
     * Erstellt einen neuen GuildTraderManager.
     *
     * @param npcManager NPCManager
     * @param plotProvider PlotProvider
     * @param guildTraderType GuildTraderNPC-Instanz
     * @param logger Logger
     */
    public GuildTraderManager(
            NPCManager npcManager,
            PlotProvider plotProvider,
            GuildTraderNPC guildTraderType,
            Logger logger
    ) {
        this.npcManager = npcManager;
        this.plotProvider = plotProvider;
        this.guildTraderType = guildTraderType;
        this.logger = logger;

        this.npcPlotMapping = new HashMap<>();
        this.plotNPCMapping = new HashMap<>();
    }

    /**
     * Spawnt einen Gildenhändler auf einem Grundstück.
     *
     * @param plot Das Grundstück
     * @param location Spawn-Position
     * @return UUID des NPCs, oder null wenn Spawn fehlgeschlagen
     */
    public UUID spawnGuildTrader(Plot plot, Location location) {
        try {
            // Erstelle NPC-UUID
            UUID npcId = UUID.randomUUID();

            // TODO: Spawn via NPCProvider (Citizens)
            // NPCProvider npcProvider = providers.getNpcProvider();
            // npcProvider.createNPC(npcId, location, guildTraderType.getDisplayName());

            // Registriere NPC beim NPCManager
            npcManager.registerNPC(npcId, "guildtrader");

            // Registriere NPC beim GuildTraderType
            guildTraderType.registerNPCForPlot(npcId, plot);

            // Speichere Zuordnung
            npcPlotMapping.put(npcId, plot.getUuid());
            plotNPCMapping.computeIfAbsent(plot.getUuid(), k -> new ArrayList<>()).add(npcId);

            logger.info("Spawned GuildTrader " + npcId + " on plot " + plot.getUuid());

            return npcId;

        } catch (Exception e) {
            logger.severe("Failed to spawn GuildTrader: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Entfernt einen Gildenhändler.
     *
     * @param npcId UUID des NPCs
     * @return true wenn erfolgreich entfernt
     */
    public boolean removeGuildTrader(UUID npcId) {
        try {
            // Hole Plot
            UUID plotId = npcPlotMapping.get(npcId);
            if (plotId == null) {
                logger.warning("GuildTrader " + npcId + " not found in mapping");
                return false;
            }

            // TODO: Despawn via NPCProvider (Citizens)
            // NPCProvider npcProvider = providers.getNpcProvider();
            // npcProvider.removeNPC(npcId);

            // Unregistriere NPC
            npcManager.unregisterNPC(npcId);
            guildTraderType.unregisterNPC(npcId);

            // Entferne Zuordnung
            npcPlotMapping.remove(npcId);
            List<UUID> plotNPCs = plotNPCMapping.get(plotId);
            if (plotNPCs != null) {
                plotNPCs.remove(npcId);
                if (plotNPCs.isEmpty()) {
                    plotNPCMapping.remove(plotId);
                }
            }

            logger.info("Removed GuildTrader " + npcId);

            return true;

        } catch (Exception e) {
            logger.severe("Failed to remove GuildTrader: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Gibt alle Gildenhändler auf einem Grundstück zurück.
     *
     * @param plot Das Grundstück
     * @return Liste von NPC-UUIDs
     */
    public List<UUID> getGuildTradersOnPlot(Plot plot) {
        return plotNPCMapping.getOrDefault(plot.getUuid(), Collections.emptyList());
    }

    /**
     * Gibt das Grundstück eines Gildenhändlers zurück.
     *
     * @param npcId UUID des NPCs
     * @return Plot oder null
     */
    public Plot getPlotForTrader(UUID npcId) {
        UUID plotId = npcPlotMapping.get(npcId);
        if (plotId == null) {
            return null;
        }

        try {
            // TODO: Hole Plot via PlotProvider
            // return plotProvider.getPlot(plotId);
            return null;  // Vorläufig

        } catch (Exception e) {
            logger.warning("Failed to get plot for trader " + npcId + ": " + e.getMessage());
            return null;
        }
    }

    /**
     * Invalidiert den TradeSet-Cache für alle NPCs auf einem Grundstück.
     *
     * Sollte aufgerufen werden wenn sich das Plot-Storage-Inventar ändert.
     *
     * @param plot Das Grundstück
     */
    public void invalidateCacheForPlot(Plot plot) {
        guildTraderType.invalidateCacheForPlot(plot);
        logger.fine("Invalidated TradeSet cache for plot " + plot.getUuid());
    }

    /**
     * Lädt Gildenhändler aus der Config.
     *
     * @param config FileConfiguration
     */
    public void loadFromConfig(FileConfiguration config) {
        ConfigurationSection section = config.getConfigurationSection("guild-traders");
        if (section == null) {
            logger.info("No guild traders in config");
            return;
        }

        int loaded = 0;
        for (String key : section.getKeys(false)) {
            try {
                UUID npcId = UUID.fromString(key);
                UUID plotId = UUID.fromString(section.getString(key + ".plot-uuid"));

                // Location laden
                ConfigurationSection locSection = section.getConfigurationSection(key + ".location");
                if (locSection == null) {
                    logger.warning("No location for guild trader " + npcId);
                    continue;
                }

                // TODO: Erstelle Location
                // Location location = new Location(...)

                // Registriere Zuordnung (NPC wird beim Server-Start via Citizens geladen)
                npcPlotMapping.put(npcId, plotId);
                plotNPCMapping.computeIfAbsent(plotId, k -> new ArrayList<>()).add(npcId);

                loaded++;

            } catch (Exception e) {
                logger.warning("Failed to load guild trader " + key + ": " + e.getMessage());
            }
        }

        logger.info("Loaded " + loaded + " guild traders from config");
    }

    /**
     * Speichert Gildenhändler in die Config.
     *
     * @param config FileConfiguration
     */
    public void saveToConfig(FileConfiguration config) {
        // Lösche alte Daten
        config.set("guild-traders", null);

        // Speichere alle NPCs
        for (Map.Entry<UUID, UUID> entry : npcPlotMapping.entrySet()) {
            UUID npcId = entry.getKey();
            UUID plotId = entry.getValue();

            String path = "guild-traders." + npcId;

            config.set(path + ".plot-uuid", plotId.toString());

            // TODO: Location speichern
            // config.set(path + ".location.world", location.getWorld().getName());
            // config.set(path + ".location.x", location.getX());
            // ...
        }

        logger.info("Saved " + npcPlotMapping.size() + " guild traders to config");
    }

    /**
     * Gibt die Anzahl aller Gildenhändler zurück.
     *
     * @return Anzahl Gildenhändler
     */
    public int getGuildTraderCount() {
        return npcPlotMapping.size();
    }

    /**
     * Cleanup - entfernt alle Händler.
     */
    public void shutdown() {
        // Cleanup alle NPCs
        new ArrayList<>(npcPlotMapping.keySet()).forEach(this::removeGuildTrader);

        npcPlotMapping.clear();
        plotNPCMapping.clear();

        logger.info("GuildTraderManager shutdown complete");
    }
}
