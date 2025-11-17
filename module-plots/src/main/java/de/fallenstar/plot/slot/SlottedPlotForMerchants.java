package de.fallenstar.plot.slot;

import java.util.List;

/**
 * Interface für Handelsgilde-Grundstücke mit Händler-Slots.
 *
 * Erweitert SlottedPlot um spezifische Methoden für Handelsgrundstücke.
 *
 * **Slot-Typen für Handelsgrundstücke:**
 * - **Händler-Slots:** Gildenhändler, Spielerhändler
 * - **Bankier-Slots:** Lokale Bankiers (eigene Münzbestände)
 * - **Handwerker-Slots:** Handwerks-NPCs (Rüstungsschmied, Waffenschmied, etc.)
 *
 * Features:
 * - Händler-Slots (für Gildenhändler, Spielerhändler)
 * - Bankier-Slots (für Lokale Bankiers)
 * - Handwerker-Slots (für Handwerks-NPCs)
 * - Slot-Limits pro Typ
 *
 * Verwendung:
 * <pre>
 * SlottedPlotForMerchants merchantPlot = ...;
 * int traderSlots = merchantPlot.getTraderSlotAmount();
 * List<PlotSlot> traderSlots = merchantPlot.getTraderSlots();
 *
 * // Händler-Slot hinzufügen
 * PlotSlot slot = new PlotSlot(location, PlotSlot.SlotType.TRADER);
 * merchantPlot.addSlot(slot);
 * </pre>
 *
 * @author FallenStar
 * @version 1.0
 */
public interface SlottedPlotForMerchants extends SlottedPlot {

    /**
     * Gibt die Anzahl der Händler-Slots zurück.
     *
     * @return Anzahl Händler-Slots
     */
    default int getTraderSlotAmount() {
        return (int) getSlotsByType(PlotSlot.SlotType.TRADER).stream()
                .filter(PlotSlot::isActive)
                .count();
    }

    /**
     * Gibt alle Händler-Slots zurück.
     *
     * @return Liste der Händler-Slots
     */
    default List<PlotSlot> getTraderSlots() {
        return getSlotsByType(PlotSlot.SlotType.TRADER);
    }

    /**
     * Gibt die Anzahl der Bankier-Slots zurück.
     *
     * @return Anzahl Bankier-Slots
     */
    default int getBankerSlotAmount() {
        return (int) getSlotsByType(PlotSlot.SlotType.BANKER).stream()
                .filter(PlotSlot::isActive)
                .count();
    }

    /**
     * Gibt alle Bankier-Slots zurück.
     *
     * @return Liste der Bankier-Slots
     */
    default List<PlotSlot> getBankerSlots() {
        return getSlotsByType(PlotSlot.SlotType.BANKER);
    }

    /**
     * Gibt die Anzahl der Handwerker-Slots zurück.
     *
     * @return Anzahl Handwerker-Slots
     */
    default int getCraftsmanSlotAmount() {
        return (int) getSlotsByType(PlotSlot.SlotType.CRAFTSMAN).stream()
                .filter(PlotSlot::isActive)
                .count();
    }

    /**
     * Gibt alle Handwerker-Slots zurück.
     *
     * @return Liste der Handwerker-Slots
     */
    default List<PlotSlot> getCraftsmanSlots() {
        return getSlotsByType(PlotSlot.SlotType.CRAFTSMAN);
    }

    /**
     * Gibt die maximale Anzahl Händler-Slots zurück.
     *
     * Diese Methode kann überschrieben werden um grundstücksspezifische Limits zu setzen.
     *
     * @return Maximale Anzahl Händler-Slots (Default: 5)
     */
    default int getMaxTraderSlots() {
        return 5;
    }

    /**
     * Gibt die maximale Anzahl Bankier-Slots zurück.
     *
     * @return Maximale Anzahl Bankier-Slots (Default: 2)
     */
    default int getMaxBankerSlots() {
        return 2;
    }

    /**
     * Gibt die maximale Anzahl Handwerker-Slots zurück.
     *
     * @return Maximale Anzahl Handwerker-Slots (Default: 3)
     */
    default int getMaxCraftsmanSlots() {
        return 3;
    }

    // ========== Erweiterte Slot-Verwaltung (Market-Plots) ==========

    /**
     * Gibt die Anzahl aktuell verfügbarer Händler-Slots zurück.
     *
     * Dies sind Slots die bereits gekauft/freigeschaltet wurden,
     * aber noch nicht mit einem NPC belegt sind.
     *
     * @return Anzahl verfügbarer (nicht belegter) Händler-Slots
     */
    default int getUnusedTraderSlots() {
        return (int) getTraderSlots().stream()
                .filter(PlotSlot::isActive)
                .filter(slot -> !slot.isOccupied())
                .count();
    }

    /**
     * Gibt die Anzahl aktuell verfügbarer Slots zurück.
     *
     * Dies entspricht der Gesamtanzahl gekaufter Slots,
     * unabhängig davon ob sie belegt sind oder nicht.
     *
     * @return Anzahl aktuell verfügbarer Slots
     */
    default int getCurrentlyAvailableSlots() {
        return getTraderSlotAmount();
    }

    /**
     * Gibt die maximale Anzahl kaufbarer Händler-Slots zurück.
     *
     * Dies ist das absolute Maximum an Slots die auf diesem
     * Grundstück gekauft werden können.
     *
     * Wird verwendet für:
     * - Prüfung ob weitere Slots gekauft werden können
     * - Anzeige im UI ("3/5 Slots")
     *
     * @return Maximale Anzahl kaufbarer Slots
     */
    int getMaximumAvailableSlots();

    /**
     * Gibt die Anzahl noch kaufbarer Slots zurück.
     *
     * @return Anzahl Slots die noch gekauft werden können
     */
    default int getRemainingPurchasableSlots() {
        return getMaximumAvailableSlots() - getCurrentlyAvailableSlots();
    }

    /**
     * Prüft ob weitere Slots gekauft werden können.
     *
     * @return true wenn weitere Slots kaufbar sind
     */
    default boolean canPurchaseMoreSlots() {
        return getRemainingPurchasableSlots() > 0;
    }
}
