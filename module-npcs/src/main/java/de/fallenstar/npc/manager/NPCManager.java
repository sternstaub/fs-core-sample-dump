package de.fallenstar.npc.manager;

import de.fallenstar.core.provider.NPCProvider;
import de.fallenstar.core.registry.PlotTypeRegistry;
import de.fallenstar.core.registry.ProviderRegistry;
import de.fallenstar.npc.NPCModule;
import de.fallenstar.npc.npctype.NPCType;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * Manager für NPC-Verwaltung.
 *
 * Verantwortlich für:
 * - Registrierung von NPC-Typen
 * - Zuordnung NPC-UUID -> NPC-Typ
 * - Click-Handler für NPCs
 * - Lifecycle-Management
 *
 * @author FallenStar
 * @version 1.0
 */
public class NPCManager {

    private final NPCModule plugin;
    private final ProviderRegistry providers;
    private final PlotTypeRegistry plotTypeRegistry;
    private final Logger logger;

    // NPC-Typ-Name -> NPCType-Instanz
    private final Map<String, NPCType> npcTypes;

    // NPC-UUID -> NPC-Typ-Name
    private final Map<UUID, String> npcTypeMap;

    /**
     * Erstellt einen neuen NPCManager.
     *
     * @param plugin Plugin-Instanz
     * @param providers ProviderRegistry
     * @param plotTypeRegistry PlotTypeRegistry
     * @param logger Logger
     */
    public NPCManager(
            NPCModule plugin,
            ProviderRegistry providers,
            PlotTypeRegistry plotTypeRegistry,
            Logger logger
    ) {
        this.plugin = plugin;
        this.providers = providers;
        this.plotTypeRegistry = plotTypeRegistry;
        this.logger = logger;

        this.npcTypes = new HashMap<>();
        this.npcTypeMap = new HashMap<>();
    }

    /**
     * Registriert einen NPC-Typ.
     *
     * @param typeName Typ-Name (z.B. "ambassador")
     * @param npcType NPCType-Instanz
     */
    public void registerNPCType(String typeName, NPCType npcType) {
        npcTypes.put(typeName.toLowerCase(), npcType);

        // Initialisiere NPC-Typ
        npcType.initialize();

        logger.info("Registered NPC type: " + typeName);
    }

    /**
     * Registriert einen NPC für einen bestimmten Typ.
     *
     * @param npcId UUID des NPCs
     * @param typeName Typ-Name
     */
    public void registerNPC(UUID npcId, String typeName) {
        npcTypeMap.put(npcId, typeName.toLowerCase());

        // Setze Click-Handler
        NPCProvider npcProvider = providers.getNpcProvider();
        try {
            npcProvider.setClickHandler(npcId, player -> handleNPCClick(player, npcId));
            logger.fine("Registered NPC " + npcId + " with type " + typeName);
        } catch (Exception e) {
            logger.warning("Failed to set click handler for NPC " + npcId + ": " + e.getMessage());
        }
    }

    /**
     * Entfernt einen NPC.
     *
     * @param npcId UUID des NPCs
     */
    public void unregisterNPC(UUID npcId) {
        npcTypeMap.remove(npcId);
        logger.fine("Unregistered NPC " + npcId);
    }

    /**
     * Handhabt Klicks auf NPCs.
     *
     * @param player Der Spieler
     * @param npcId UUID des geklickten NPCs
     */
    private void handleNPCClick(Player player, UUID npcId) {
        // Hole NPC-Typ
        String typeName = npcTypeMap.get(npcId);
        if (typeName == null) {
            player.sendMessage("§cDieser NPC ist nicht korrekt konfiguriert!");
            logger.warning("NPC " + npcId + " has no type registered!");
            return;
        }

        // Hole NPCType-Instanz
        NPCType npcType = npcTypes.get(typeName);
        if (npcType == null) {
            player.sendMessage("§cNPC-Typ '" + typeName + "' nicht gefunden!");
            logger.warning("NPC type '" + typeName + "' not found for NPC " + npcId);
            return;
        }

        // Prüfe ob NPC-Typ verfügbar ist
        if (!npcType.isAvailable()) {
            player.sendMessage("§cDieser NPC ist momentan nicht verfügbar!");
            return;
        }

        // Führe onClick aus
        try {
            npcType.onClick(player, npcId);
        } catch (Exception e) {
            player.sendMessage("§cEin Fehler ist beim Interagieren mit dem NPC aufgetreten!");
            logger.severe("Error handling NPC click: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Gibt einen NPC-Typ zurück.
     *
     * @param typeName Typ-Name
     * @return NPCType oder null
     */
    public NPCType getNPCType(String typeName) {
        return npcTypes.get(typeName.toLowerCase());
    }

    /**
     * Prüft ob ein NPC-Typ registriert ist.
     *
     * @param typeName Typ-Name
     * @return true wenn registriert
     */
    public boolean isNPCTypeRegistered(String typeName) {
        return npcTypes.containsKey(typeName.toLowerCase());
    }

    /**
     * Gibt alle registrierten NPC-Typen zurück.
     *
     * @return Map von Typ-Name -> NPCType
     */
    public Map<String, NPCType> getNPCTypes() {
        return new HashMap<>(npcTypes);
    }

    /**
     * Gibt den Logger zurück.
     *
     * @return Logger
     */
    public Logger getLogger() {
        return logger;
    }

    /**
     * Shutdown - räumt alle NPCs auf.
     */
    public void shutdown() {
        // Cleanup alle NPC-Typen
        for (NPCType npcType : npcTypes.values()) {
            try {
                npcType.shutdown();
            } catch (Exception e) {
                logger.warning("Error shutting down NPC type: " + e.getMessage());
            }
        }

        npcTypes.clear();
        npcTypeMap.clear();

        logger.info("NPCManager shutdown complete");
    }
}
