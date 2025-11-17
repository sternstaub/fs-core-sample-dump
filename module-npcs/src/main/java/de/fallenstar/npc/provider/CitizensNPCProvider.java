package de.fallenstar.npc.provider;

import de.fallenstar.core.exception.ProviderFunctionalityNotFoundException;
import de.fallenstar.core.provider.NPCProvider;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCRegistry;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.logging.Logger;

/**
 * Citizens-basierte NPC-Provider-Implementierung.
 *
 * Nutzt Citizens API 2.x für NPC-Verwaltung.
 *
 * Features:
 * - NPC-Erstellung mit Custom-Skins
 * - Click-Handler-Registrierung
 * - NPC-Teleportation
 * - Skin-Änderungen
 *
 * **Integration:**
 * Registriert sich selbst in der ProviderRegistry beim Modul-Start.
 *
 * @author FallenStar
 * @version 1.0
 */
public class CitizensNPCProvider implements NPCProvider {

    private final Logger logger;
    private final NPCRegistry npcRegistry;

    /**
     * Mapping: UUID (unsere ID) → Citizens NPC-ID
     */
    private final Map<UUID, Integer> npcMapping;

    /**
     * Mapping: UUID → Click-Handler
     */
    private final Map<UUID, Consumer<Player>> clickHandlers;

    /**
     * Erstellt einen neuen CitizensNPCProvider.
     *
     * @param logger Logger
     */
    public CitizensNPCProvider(Logger logger) {
        this.logger = logger;
        this.npcRegistry = CitizensAPI.getNPCRegistry();
        this.npcMapping = new HashMap<>();
        this.clickHandlers = new HashMap<>();

        logger.info("CitizensNPCProvider initialized");
    }

    @Override
    public boolean isAvailable() {
        try {
            // Prüfe ob Citizens API verfügbar ist
            return CitizensAPI.getNPCRegistry() != null;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public UUID createNPC(Location location, String name, String skin)
            throws ProviderFunctionalityNotFoundException {
        if (!isAvailable()) {
            throw new ProviderFunctionalityNotFoundException(
                    "NPCProvider", "createNPC", "Citizens not available"
            );
        }

        try {
            // Erstelle Citizens-NPC
            NPC npc = npcRegistry.createNPC(EntityType.PLAYER, name);

            // Setze Skin (falls angegeben)
            if (skin != null && !skin.isEmpty()) {
                npc.data().set(NPC.Metadata.PLAYER_SKIN_UUID, skin);
            }

            // Spawn NPC
            npc.spawn(location);

            // Generiere UUID für unsere Verwaltung
            UUID ourUUID = UUID.randomUUID();

            // Speichere Mapping
            npcMapping.put(ourUUID, npc.getId());

            logger.info("Created Citizens NPC: " + name + " (ID: " + npc.getId() + ", UUID: " + ourUUID + ")");

            return ourUUID;

        } catch (Exception e) {
            logger.severe("Failed to create NPC: " + e.getMessage());
            throw new ProviderFunctionalityNotFoundException(
                    "NPCProvider", "createNPC", "Failed: " + e.getMessage()
            );
        }
    }

    @Override
    public boolean removeNPC(UUID npcId) throws ProviderFunctionalityNotFoundException {
        if (!isAvailable()) {
            throw new ProviderFunctionalityNotFoundException(
                    "NPCProvider", "removeNPC", "Citizens not available"
            );
        }

        Integer citizensId = npcMapping.get(npcId);
        if (citizensId == null) {
            logger.warning("NPC " + npcId + " not found in mapping");
            return false;
        }

        try {
            NPC npc = npcRegistry.getById(citizensId);
            if (npc == null) {
                logger.warning("Citizens NPC " + citizensId + " not found");
                npcMapping.remove(npcId);
                clickHandlers.remove(npcId);
                return false;
            }

            // Despawn und entferne NPC
            npc.despawn();
            npc.destroy();

            // Entferne aus Mappings
            npcMapping.remove(npcId);
            clickHandlers.remove(npcId);

            logger.info("Removed Citizens NPC " + citizensId + " (UUID: " + npcId + ")");

            return true;

        } catch (Exception e) {
            logger.severe("Failed to remove NPC: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean teleportNPC(UUID npcId, Location location)
            throws ProviderFunctionalityNotFoundException {
        if (!isAvailable()) {
            throw new ProviderFunctionalityNotFoundException(
                    "NPCProvider", "teleportNPC", "Citizens not available"
            );
        }

        Integer citizensId = npcMapping.get(npcId);
        if (citizensId == null) {
            return false;
        }

        try {
            NPC npc = npcRegistry.getById(citizensId);
            if (npc == null || !npc.isSpawned()) {
                return false;
            }

            // Teleportiere NPC
            npc.getEntity().teleport(location);

            logger.fine("Teleported NPC " + npcId + " to " + location);

            return true;

        } catch (Exception e) {
            logger.warning("Failed to teleport NPC: " + e.getMessage());
            return false;
        }
    }

    @Override
    public void setClickHandler(UUID npcId, Consumer<Player> handler)
            throws ProviderFunctionalityNotFoundException {
        if (!isAvailable()) {
            throw new ProviderFunctionalityNotFoundException(
                    "NPCProvider", "setClickHandler", "Citizens not available"
            );
        }

        clickHandlers.put(npcId, handler);

        logger.fine("Registered click handler for NPC " + npcId);
    }

    @Override
    public boolean setSkin(UUID npcId, String skin)
            throws ProviderFunctionalityNotFoundException {
        if (!isAvailable()) {
            throw new ProviderFunctionalityNotFoundException(
                    "NPCProvider", "setSkin", "Citizens not available"
            );
        }

        Integer citizensId = npcMapping.get(npcId);
        if (citizensId == null) {
            return false;
        }

        try {
            NPC npc = npcRegistry.getById(citizensId);
            if (npc == null) {
                return false;
            }

            // Setze Skin
            npc.data().set(NPC.Metadata.PLAYER_SKIN_UUID, skin);

            // Respawn NPC um Skin zu aktualisieren
            if (npc.isSpawned()) {
                Location location = npc.getStoredLocation();
                npc.despawn();
                npc.spawn(location);
            }

            logger.fine("Updated skin for NPC " + npcId + " to " + skin);

            return true;

        } catch (Exception e) {
            logger.warning("Failed to set NPC skin: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean npcExists(UUID npcId) throws ProviderFunctionalityNotFoundException {
        if (!isAvailable()) {
            throw new ProviderFunctionalityNotFoundException(
                    "NPCProvider", "npcExists", "Citizens not available"
            );
        }

        Integer citizensId = npcMapping.get(npcId);
        if (citizensId == null) {
            return false;
        }

        NPC npc = npcRegistry.getById(citizensId);
        return npc != null;
    }

    /**
     * Handhabt NPC-Clicks (wird von Citizens-Event aufgerufen).
     *
     * @param npcId UUID des geklickten NPCs
     * @param player Der klickende Spieler
     */
    public void handleNPCClick(UUID npcId, Player player) {
        Consumer<Player> handler = clickHandlers.get(npcId);
        if (handler != null) {
            try {
                handler.accept(player);
            } catch (Exception e) {
                logger.severe("Error in NPC click handler: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * Gibt die Citizens-NPC-ID für eine UUID zurück.
     *
     * @param npcId UUID
     * @return Citizens NPC-ID oder null
     */
    public Integer getCitizensId(UUID npcId) {
        return npcMapping.get(npcId);
    }

    /**
     * Gibt die UUID für eine Citizens-NPC-ID zurück.
     *
     * @param citizensId Citizens NPC-ID
     * @return UUID oder null
     */
    public UUID getUUID(int citizensId) {
        return npcMapping.entrySet().stream()
                .filter(e -> e.getValue() == citizensId)
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);
    }

    /**
     * Lädt vorhandene NPCs aus Citizens-Registry.
     *
     * Sollte beim Modul-Start aufgerufen werden.
     */
    public void loadExistingNPCs() {
        int loaded = 0;

        for (NPC npc : npcRegistry) {
            // Prüfe ob NPC bereits gemapped ist
            UUID existingUUID = getUUID(npc.getId());
            if (existingUUID == null) {
                // TODO: Lade UUID aus Citizens-Data oder generiere neue
                // Für jetzt: Generiere neue UUID
                UUID newUUID = UUID.randomUUID();
                npcMapping.put(newUUID, npc.getId());
                loaded++;
            }
        }

        if (loaded > 0) {
            logger.info("Loaded " + loaded + " existing Citizens NPCs");
        }
    }

    /**
     * Cleanup - entfernt alle Mappings.
     */
    public void shutdown() {
        clickHandlers.clear();
        npcMapping.clear();
        logger.info("CitizensNPCProvider shutdown");
    }
}
