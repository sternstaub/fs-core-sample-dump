package de.fallenstar.core.distributor;

import java.util.List;

/**
 * Interface für Objekte die Inhalte distribuieren können.
 *
 * **Konzept:**
 * - Automatische Verteilung von Objekten
 * - Kapazitäts-Management
 * - Type-Safe via Generics
 *
 * **Implementierungen:**
 * - NpcDistributor - Verteilt NPCs auf Slots
 * - QuestDistributor - Verteilt Quests an NPCs
 * - ItemDistributor - Verteilt Items in Lager
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
 *         // Platziere NPC
 *         placeNpcInSlot(slot, npc.getId());
 *
 *         // Callback
 *         npc.onDistributed(this);
 *
 *         return true;
 *     }
 * }
 * </pre>
 *
 * @param <T> Typ des Distributable
 * @author FallenStar
 * @version 1.0
 */
public interface Distributor<T extends Distributable> {

    /**
     * Distribuiert ein Objekt.
     *
     * Algorithmus (Beispiel):
     * 1. Prüfe hasCapacity()
     * 2. Finde freien Platz
     * 3. Platziere Objekt
     * 4. Rufe distributable.onDistributed() auf
     *
     * @param distributable Das zu distribuierende Objekt
     * @return true wenn erfolgreich, false wenn voll/fehlgeschlagen
     */
    boolean distribute(T distributable);

    /**
     * Entfernt ein distribuiertes Objekt.
     *
     * Algorithmus (Beispiel):
     * 1. Finde Objekt
     * 2. Entferne Objekt
     * 3. Rufe distributable.onUndistributed() auf
     *
     * @param distributable Das Objekt
     * @return true wenn erfolgreich entfernt
     */
    boolean undistribute(T distributable);

    /**
     * Gibt die maximale Kapazität zurück.
     *
     * @return Maximale Anzahl an Distributables
     */
    int getCapacity();

    /**
     * Gibt die aktuelle Belegung zurück.
     *
     * @return Anzahl distribuierter Objekte
     */
    int getCurrentCount();

    /**
     * Prüft ob noch Kapazität verfügbar ist.
     *
     * @return true wenn Platz frei
     */
    default boolean hasCapacity() {
        return getCurrentCount() < getCapacity();
    }

    /**
     * Gibt alle distribuierten Objekte zurück.
     *
     * @return Liste von Distributables
     */
    List<T> getDistributed();

    /**
     * Gibt die freie Kapazität zurück.
     *
     * @return Anzahl freier Plätze
     */
    default int getFreeCapacity() {
        return getCapacity() - getCurrentCount();
    }

    /**
     * Prüft ob Distributor leer ist.
     *
     * @return true wenn keine Objekte distribuiert sind
     */
    default boolean isEmpty() {
        return getCurrentCount() == 0;
    }

    /**
     * Prüft ob Distributor voll ist.
     *
     * @return true wenn Kapazität erreicht
     */
    default boolean isFull() {
        return getCurrentCount() >= getCapacity();
    }
}
