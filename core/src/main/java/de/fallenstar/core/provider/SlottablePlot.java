package de.fallenstar.core.provider;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Trait-Interface für Plots mit Trader-Slots.
 *
 * **Features:**
 * - Mehrere NPC-Slots pro Plot
 * - Slot-Verwaltung (belegt/frei)
 * - NPCs können auf Slots platziert werden
 *
 * **Konzept:**
 * - Plots haben feste Anzahl an Slots
 * - Slots können mit PlotBoundNPCs belegt werden
 * - NPCs können von anderen Plots kommen (Marktplatz-Konzept)
 *
 * **Verwendung:**
 * <pre>
 * class MarketPlot extends BasePlot implements SlottablePlot {
 *     private Map&lt;Integer, UUID&gt; slots = new HashMap&lt;&gt;();
 *     private int maxSlots = 10;
 *
 *     {@literal @}Override
 *     public int getMaxSlots() {
 *         return maxSlots;
 *     }
 *
 *     {@literal @}Override
 *     public boolean placeNpcInSlot(int slot, UUID npcId) {
 *         if (slot &lt; 0 || slot &gt;= maxSlots) return false;
 *         if (isSlotOccupied(slot)) return false;
 *         slots.put(slot, npcId);
 *         return true;
 *     }
 * }
 * </pre>
 *
 * **Integration:**
 * - SlotManagementUi: Slots verwalten
 * - MarketPlot: Zentrale Marktplätze mit vielen Slots
 * - TradeguildPlot: Wenige eigene Slots + Delegation an Marktplatz
 *
 * @author FallenStar
 * @version 1.0
 */
public interface SlottablePlot extends Plot {

    /**
     * Gibt die maximale Anzahl an Slots zurück.
     *
     * @return Maximale Slots
     */
    int getMaxSlots();

    /**
     * Setzt die maximale Anzahl an Slots.
     *
     * @param maxSlots Neue maximale Anzahl
     */
    void setMaxSlots(int maxSlots);

    /**
     * Prüft ob ein Slot belegt ist.
     *
     * @param slot Slot-Nummer (0-basiert)
     * @return true wenn belegt
     */
    boolean isSlotOccupied(int slot);

    /**
     * Gibt den NPC in einem Slot zurück.
     *
     * @param slot Slot-Nummer (0-basiert)
     * @return Optional mit NPC UUID, oder empty wenn Slot frei
     */
    Optional<UUID> getNpcInSlot(int slot);

    /**
     * Platziert einen NPC in einem Slot.
     *
     * @param slot Slot-Nummer (0-basiert)
     * @param npcId NPC UUID
     * @return true wenn erfolgreich platziert
     */
    boolean placeNpcInSlot(int slot, UUID npcId);

    /**
     * Entfernt einen NPC aus einem Slot.
     *
     * @param slot Slot-Nummer (0-basiert)
     * @return Optional mit entfernter NPC UUID, oder empty wenn Slot leer war
     */
    Optional<UUID> removeNpcFromSlot(int slot);

    /**
     * Gibt alle belegten Slots zurück.
     *
     * @return Liste von Slot-Nummern
     */
    List<Integer> getOccupiedSlots();

    /**
     * Gibt alle freien Slots zurück.
     *
     * @return Liste von Slot-Nummern
     */
    default List<Integer> getFreeSlots() {
        List<Integer> freeSlots = new java.util.ArrayList<>();
        for (int i = 0; i < getMaxSlots(); i++) {
            if (!isSlotOccupied(i)) {
                freeSlots.add(i);
            }
        }
        return freeSlots;
    }

    /**
     * Gibt die Anzahl belegter Slots zurück.
     *
     * @return Anzahl
     */
    default int getOccupiedSlotCount() {
        return getOccupiedSlots().size();
    }

    /**
     * Gibt die Anzahl freier Slots zurück.
     *
     * @return Anzahl
     */
    default int getFreeSlotCount() {
        return getMaxSlots() - getOccupiedSlotCount();
    }

    /**
     * Prüft ob noch freie Slots verfügbar sind.
     *
     * @return true wenn mindestens ein freier Slot existiert
     */
    default boolean hasFreeSlotsAvailable() {
        return getFreeSlotCount() > 0;
    }

    /**
     * Leert alle Slots.
     */
    void clearAllSlots();
}
