package de.fallenstar.core.distributor;

import org.bukkit.Location;

import java.util.Optional;
import java.util.UUID;

/**
 * NPC der auf Slots distribuiert werden kann.
 *
 * **Features:**
 * - Spawn/Despawn an Location
 * - Entity-ID-Verwaltung
 * - NPC-Typ-Information
 *
 * **Verwendung:**
 * <pre>
 * class GuildTraderDistributable implements DistributableNpc {
 *     private UUID entityId;
 *
 *     {@literal @}Override
 *     public UUID spawn(Location location) {
 *         // Spawne NPC mit Citizens/Custom-System
 *         entityId = npcManager.spawn(location, "guild_trader");
 *         return entityId;
 *     }
 *
 *     {@literal @}Override
 *     public void despawn() {
 *         if (entityId != null) {
 *             npcManager.despawn(entityId);
 *         }
 *     }
 * }
 * </pre>
 *
 * @author FallenStar
 * @version 1.0
 */
public interface DistributableNpc extends Distributable {

    /**
     * Gibt die Entity-UUID zurück.
     *
     * @return Entity UUID oder empty wenn noch nicht gespawned
     */
    Optional<UUID> getEntityId();

    /**
     * Spawnt den NPC an einer Location.
     *
     * @param location Location
     * @return Entity UUID
     */
    UUID spawn(Location location);

    /**
     * Despawnt den NPC.
     *
     * Entfernt die Entity aus der Welt.
     */
    void despawn();

    /**
     * Prüft ob NPC gespawned ist.
     *
     * @return true wenn Entity existiert
     */
    default boolean isSpawned() {
        return getEntityId().isPresent();
    }

    /**
     * Gibt den NPC-Typ zurück.
     *
     * @return NPC-Typ String (z.B. "guild_trader", "quest_giver", "guard")
     */
    String getNpcType();

    /**
     * Gibt den Display-Namen zurück.
     *
     * @return Anzeige-Name (mit Farbcodes)
     */
    String getDisplayName();
}
