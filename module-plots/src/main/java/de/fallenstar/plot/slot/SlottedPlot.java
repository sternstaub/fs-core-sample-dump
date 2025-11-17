package de.fallenstar.plot.slot;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Interface für Grundstücke die NPC-Slots unterstützen.
 *
 * Slottable Plots können eine definierte Anzahl von NPC-Slots haben,
 * an denen NPCs platziert werden können.
 *
 * **Konzept:**
 * Slots sind Positionen auf Grundstücken, an denen NPCs platziert werden können.
 * Dies ermöglicht:
 * - Feste NPC-Platzierung durch Grundstücksbesitzer
 * - Dynamische NPC-Platzierung (fahrende Händler, Handwerker)
 * - Slot-Verwaltung über UI
 * - Verschiedene Slot-Typen für verschiedene NPC-Arten
 *
 * Features:
 * - Slot-Verwaltung (hinzufügen, entfernen, abrufen)
 * - Aktive Slots abrufen
 * - Slot-Limits pro Typ
 *
 * Implementierungen:
 * - SlottedPlotForMerchants (Handelsgilde)
 * - SlottedPlotForDiplomacy (Botschaft)
 *
 * @author FallenStar
 * @version 1.0
 */
public interface SlottedPlot {

    /**
     * Gibt alle aktiven Slots auf diesem Grundstück zurück.
     *
     * @return Liste der aktiven Slots
     */
    List<PlotSlot> getActiveSlots();

    /**
     * Gibt alle Slots auf diesem Grundstück zurück (aktiv + inaktiv).
     *
     * @return Liste aller Slots
     */
    List<PlotSlot> getAllSlots();

    /**
     * Gibt einen Slot anhand seiner ID zurück.
     *
     * @param slotId UUID des Slots
     * @return Optional mit Slot, oder empty wenn nicht gefunden
     */
    Optional<PlotSlot> getSlot(UUID slotId);

    /**
     * Gibt alle Slots eines bestimmten Typs zurück.
     *
     * @param slotType Slot-Typ
     * @return Liste der Slots dieses Typs
     */
    List<PlotSlot> getSlotsByType(PlotSlot.SlotType slotType);

    /**
     * Fügt einen neuen Slot hinzu.
     *
     * @param slot Der Slot
     * @return true wenn erfolgreich, false wenn Limit erreicht
     */
    boolean addSlot(PlotSlot slot);

    /**
     * Entfernt einen Slot.
     *
     * @param slotId UUID des Slots
     * @return true wenn erfolgreich, false wenn nicht gefunden
     */
    boolean removeSlot(UUID slotId);

    /**
     * Gibt die maximale Anzahl Slots für dieses Grundstück zurück.
     *
     * @return Maximale Slot-Anzahl
     */
    int getMaxSlots();

    /**
     * Gibt die Anzahl verwendeter Slots zurück.
     *
     * @return Anzahl aktiver Slots
     */
    default int getUsedSlots() {
        return getActiveSlots().size();
    }

    /**
     * Gibt die Anzahl freier Slots zurück.
     *
     * @return Anzahl freier Slots
     */
    default int getFreeSlots() {
        return getMaxSlots() - getUsedSlots();
    }

    /**
     * Prüft ob noch Slots verfügbar sind.
     *
     * @return true wenn freie Slots verfügbar
     */
    default boolean hasFreeSlots() {
        return getFreeSlots() > 0;
    }
}
