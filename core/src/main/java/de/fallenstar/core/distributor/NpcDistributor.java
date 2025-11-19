package de.fallenstar.core.distributor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Distributor für NPCs auf Slots.
 *
 * **HINWEIS:** Erweitert NICHT Distributor<T> um Kompatibilität mit QuestDistributor zu ermöglichen.
 * Java erlaubt nicht, dass eine Klasse Distributor<DistributableNpc> UND Distributor<DistributableQuest>
 * gleichzeitig implementiert (Generics Erasure Problem).
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
 *         if (!hasNpcCapacity()) return false;
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
 * @version 2.0 - Refactored: Entfernt Distributor<T> Vererbung
 */
public interface NpcDistributor {

    /**
     * Distribuiert einen NPC auf einen freien Slot.
     *
     * Algorithmus:
     * 1. Prüfe hasNpcCapacity()
     * 2. Finde freien Slot
     * 3. Berechne Spawn-Location
     * 4. Spawne NPC
     * 5. Platziere NPC in Slot
     * 6. Rufe npc.onDistributed() auf
     *
     * @param npc Der NPC
     * @return true wenn erfolgreich
     */
    boolean distribute(DistributableNpc npc);

    /**
     * Entfernt einen distribuierten NPC.
     *
     * @param npc Der NPC
     * @return true wenn erfolgreich entfernt
     */
    boolean undistribute(DistributableNpc npc);

    /**
     * Gibt die maximale NPC-Kapazität zurück.
     *
     * @return Maximale Anzahl an NPCs
     */
    int getCapacity();

    /**
     * Gibt die aktuelle Anzahl distribuierter NPCs zurück.
     *
     * @return Anzahl NPCs
     */
    int getCurrentCount();

    /**
     * Prüft ob noch NPC-Kapazität verfügbar ist.
     *
     * @return true wenn Platz frei
     */
    default boolean hasCapacity() {
        return getCurrentCount() < getCapacity();
    }

    /**
     * Gibt alle distribuierten NPCs zurück.
     *
     * HINWEIS: Umbenennung von getDistributed() um Konflikt mit QuestDistributor zu vermeiden.
     * QuestDistributor.getDistributedQuests() gibt List<DistributableQuest> zurück.
     *
     * @return Liste von DistributableNpcs
     */
    List<DistributableNpc> getDistributedNpcs();

    /**
     * Gibt den Slot für einen NPC zurück.
     *
     * @param npcId NPC Entity UUID
     * @return Optional mit Slot-Nummer (0-basiert)
     */
    Optional<Integer> getSlotForNpc(UUID npcId);

    /**
     * Gibt den DistributableNpc in einem Slot zurück.
     *
     * HINWEIS: Umbenennung von getNpcInSlot() um Konflikt mit SlottablePlot zu vermeiden.
     * SlottablePlot.getNpcInSlot() gibt UUID zurück, diese Methode gibt DistributableNpc zurück.
     *
     * @param slot Slot-Nummer (0-basiert)
     * @return Optional mit DistributableNpc
     */
    Optional<DistributableNpc> getDistributableNpcInSlot(int slot);

    /**
     * Entfernt einen NPC aus einem Slot (Distributor-Methode).
     *
     * HINWEIS: Umbenennung von removeNpcFromSlot() um Konflikt mit SlottablePlot zu vermeiden.
     * SlottablePlot.removeNpcFromSlot() gibt Optional<UUID> zurück, diese Methode gibt boolean zurück.
     *
     * @param slot Slot-Nummer
     * @return true wenn erfolgreich
     */
    boolean undistributeNpcFromSlot(int slot);
}
