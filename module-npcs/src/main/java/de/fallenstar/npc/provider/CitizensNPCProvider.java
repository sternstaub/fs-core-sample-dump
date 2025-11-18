package de.fallenstar.npc.provider;

import de.fallenstar.core.exception.ProviderFunctionalityNotFoundException;
import de.fallenstar.core.provider.NPCProvider;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCRegistry;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import net.citizensnpcs.api.event.NPCRightClickEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Citizens-Implementation des NPCProviders.
 *
 * Verwendet Citizens 2.x API für NPC-Management.
 * Alle NPCs werden im Standard-Registry erstellt.
 *
 * Features:
 * - NPC-Erstellung und -Verwaltung
 * - Click-Handler-System
 * - Skin-Support
 * - Teleportation
 *
 * @author FallenStar
 * @version 1.0
 */
public class CitizensNPCProvider implements NPCProvider, Listener {

    private final NPCRegistry registry;
    private final Map<UUID, Consumer<Player>> clickHandlers;

    /**
     * Konstruktor - initialisiert Citizens-Registry.
     */
    public CitizensNPCProvider() {
        this.registry = CitizensAPI.getNPCRegistry();
        this.clickHandlers = new ConcurrentHashMap<>();
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public UUID createNPC(Location location, String name, String skin)
            throws ProviderFunctionalityNotFoundException {
        try {
            // Erstelle NPC als PLAYER-Entity
            NPC npc = registry.createNPC(EntityType.PLAYER, name);

            // Setze Skin wenn angegeben (über getName setzen)
            // Citizens verwendet automatisch den Skin des Spielernamens
            if (skin != null && !skin.isEmpty()) {
                npc.setName(skin);
            }

            // Spawne NPC an Location
            if (!npc.spawn(location)) {
                throw new ProviderFunctionalityNotFoundException(
                    "NPCProvider",
                    "createNPC",
                    "Failed to spawn NPC at location: " + location
                );
            }

            return npc.getUniqueId();

        } catch (Exception e) {
            throw new ProviderFunctionalityNotFoundException(
                "NPCProvider",
                "createNPC",
                "Error creating NPC: " + e.getMessage()
            );
        }
    }

    @Override
    public boolean removeNPC(UUID npcId) throws ProviderFunctionalityNotFoundException {
        NPC npc = registry.getByUniqueId(npcId);

        if (npc == null) {
            return false;
        }

        // Entferne Click-Handler
        clickHandlers.remove(npcId);

        // Despawne und lösche NPC
        npc.destroy();

        return true;
    }

    @Override
    public boolean teleportNPC(UUID npcId, Location location)
            throws ProviderFunctionalityNotFoundException {
        NPC npc = registry.getByUniqueId(npcId);

        if (npc == null) {
            throw new ProviderFunctionalityNotFoundException(
                "NPCProvider",
                "teleportNPC",
                "NPC not found: " + npcId
            );
        }

        // Teleportiere NPC (gibt void zurück, daher true wenn keine Exception)
        try {
            if (npc.isSpawned()) {
                npc.teleport(location, org.bukkit.event.player.PlayerTeleportEvent.TeleportCause.PLUGIN);
            } else {
                npc.spawn(location);
            }
            return true;
        } catch (Exception e) {
            throw new ProviderFunctionalityNotFoundException(
                "NPCProvider",
                "teleportNPC",
                "Failed to teleport NPC: " + e.getMessage()
            );
        }
    }

    @Override
    public void setClickHandler(UUID npcId, Consumer<Player> handler)
            throws ProviderFunctionalityNotFoundException {
        NPC npc = registry.getByUniqueId(npcId);

        if (npc == null) {
            throw new ProviderFunctionalityNotFoundException(
                "NPCProvider",
                "setClickHandler",
                "NPC not found: " + npcId
            );
        }

        // Registriere Handler in Map
        clickHandlers.put(npcId, handler);
    }

    @Override
    public boolean setSkin(UUID npcId, String skin)
            throws ProviderFunctionalityNotFoundException {
        NPC npc = registry.getByUniqueId(npcId);

        if (npc == null) {
            throw new ProviderFunctionalityNotFoundException(
                "NPCProvider",
                "setSkin",
                "NPC not found: " + npcId
            );
        }

        // Setze Skin über Namen
        // Citizens verwendet automatisch den Skin des Spielernamens
        npc.setName(skin);

        return true;
    }

    @Override
    public boolean npcExists(UUID npcId) throws ProviderFunctionalityNotFoundException {
        return registry.getByUniqueId(npcId) != null;
    }

    /**
     * Event-Handler für NPC-Rechtsklicks.
     * Ruft registrierte Click-Handler auf.
     *
     * @param event NPCRightClickEvent
     */
    @EventHandler
    public void onNPCClick(NPCRightClickEvent event) {
        UUID npcId = event.getNPC().getUniqueId();
        Consumer<Player> handler = clickHandlers.get(npcId);

        if (handler != null) {
            handler.accept(event.getClicker());
        }
    }
}
