package de.fallenstar.core.distributor;

import java.util.Optional;
import java.util.UUID;

/**
 * Distributor für NPCs auf Slots.
 *
 * **Features:**
 * - Automatische Slot-Zuweisung
 * - Kapazitäts-Management
 * - Slot-Tracking
 *
 * **Verwendung:**
 * <pre>
 * class TradeguildPlot implements NpcDistributor {
 *     {@literal @}Override
 *     public boolean distribute(DistributableNpc npc) {
 *         if (!hasCapacity()) return false;
 *
 *         // Finde freien Slot
 *         int slot = getFreeSlots().get(0);
 *
 *         // Berechne Spawn-Location
 *         Location spawnLoc = calculateSlotLocation(slot);
 *
 *         // Spawne NPC
 *         UUID entityId = npc.spawn(spawnLoc);
 *
 *         // Platziere in Slot
 *         placeNpcInSlot(slot, entityId);
 *
 *         // Callback
 *         npc.onDistributed(this);
 *
 *         return true;
 *     }
 * }
 * </pre>
 *
 * @author FallenStar
 * @version 1.0
 */
public interface NpcDistributor extends Distributor<DistributableNpc> {

    /**
     * Distribuiert einen NPC auf einen freien Slot.
     *
     * Algorithmus:
     * 1. Prüfe hasCapacity()
     * 2. Finde freien Slot
     * 3. Berechne Spawn-Location
     * 4. Spawne NPC
     * 5. Platziere NPC in Slot
     * 6. Rufe npc.onDistributed() auf
     *
     * @param npc Der NPC
     * @return true wenn erfolgreich
     */
    @Override
    boolean distribute(DistributableNpc npc);

    /**
     * Gibt den Slot für einen NPC zurück.
     *
     * @param npcId NPC Entity UUID
     * @return Optional mit Slot-Nummer (0-basiert)
     */
    Optional<Integer> getSlotForNpc(UUID npcId);

    /**
     * Gibt den NPC in einem Slot zurück.
     *
     * @param slot Slot-Nummer (0-basiert)
     * @return Optional mit DistributableNpc
     */
    Optional<DistributableNpc> getNpcInSlot(int slot);

    /**
     * Entfernt einen NPC aus einem Slot.
     *
     * @param slot Slot-Nummer
     * @return true wenn erfolgreich
     */
    boolean removeNpcFromSlot(int slot);
}
