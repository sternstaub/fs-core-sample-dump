package de.fallenstar.plot.slot;

import de.fallenstar.core.provider.Plot;
import org.bukkit.Location;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Repräsentiert ein Market-Grundstück mit Händler-Slots.
 *
 * Market-Grundstücke ermöglichen es Besitzern, Händler-NPCs
 * auf definierten Slots zu platzieren.
 *
 * **Features:**
 * - Start mit 1 Händler-Slot
 * - Bis zu 5 Händler-Slots kaufbar
 * - Slots können vom Besitzer positioniert werden
 * - Persistente Speicherung aller Slots
 *
 * **Slot-Verwaltung:**
 * - Slots werden über PlotSlotManager verwaltet
 * - Positionen können per UI oder Command gesetzt werden
 * - NPCs können Slots zugewiesen werden
 *
 * Verwendung:
 * <pre>
 * MarketPlot marketPlot = new MarketPlot(basePlot, maxSlots);
 * int available = marketPlot.getCurrentlyAvailableSlots();
 * if (marketPlot.canPurchaseMoreSlots()) {
 *     // Slot kaufen...
 * }
 * </pre>
 *
 * @author FallenStar
 * @version 1.0
 */
public class MarketPlot implements SlottedPlotForMerchants {

    private final Plot basePlot;
    private final Map<UUID, PlotSlot> slots;
    private final int maximumAvailableSlots;

    /**
     * Erstellt ein neues Market-Grundstück.
     *
     * @param basePlot Basis-Plot
     * @param maximumAvailableSlots Maximale Anzahl kaufbarer Slots (Default: 5)
     */
    public MarketPlot(Plot basePlot, int maximumAvailableSlots) {
        this.basePlot = basePlot;
        this.slots = new HashMap<>();
        this.maximumAvailableSlots = maximumAvailableSlots;
    }

    /**
     * Erstellt ein neues Market-Grundstück mit Default-Werten.
     *
     * @param basePlot Basis-Plot
     */
    public MarketPlot(Plot basePlot) {
        this(basePlot, 5); // Default: Max 5 Slots
    }

    /**
     * Gibt das zugrundeliegende Plot-Objekt zurück.
     *
     * @return Basis-Plot
     */
    public Plot getBasePlot() {
        return basePlot;
    }

    @Override
    public List<PlotSlot> getActiveSlots() {
        return slots.values().stream()
                .filter(PlotSlot::isActive)
                .collect(Collectors.toList());
    }

    @Override
    public List<PlotSlot> getAllSlots() {
        return new ArrayList<>(slots.values());
    }

    @Override
    public Optional<PlotSlot> getSlot(UUID slotId) {
        return Optional.ofNullable(slots.get(slotId));
    }

    @Override
    public List<PlotSlot> getSlotsByType(PlotSlot.SlotType slotType) {
        return slots.values().stream()
                .filter(slot -> slot.getSlotType() == slotType)
                .collect(Collectors.toList());
    }

    @Override
    public boolean addSlot(PlotSlot slot) {
        // Prüfe ob Limit erreicht
        if (slot.getSlotType() == PlotSlot.SlotType.TRADER) {
            if (getTraderSlotAmount() >= maximumAvailableSlots) {
                return false; // Limit erreicht
            }
        }

        slots.put(slot.getSlotId(), slot);
        return true;
    }

    @Override
    public boolean removeSlot(UUID slotId) {
        PlotSlot slot = slots.get(slotId);
        if (slot == null) {
            return false;
        }

        // Nur leere Slots entfernen
        if (slot.isOccupied()) {
            return false;
        }

        slots.remove(slotId);
        return true;
    }

    @Override
    public int getMaxSlots() {
        return maximumAvailableSlots;
    }

    @Override
    public int getMaximumAvailableSlots() {
        return maximumAvailableSlots;
    }

    /**
     * Erstellt einen neuen Händler-Slot an der angegebenen Position.
     *
     * @param location Position des Slots
     * @return true wenn erfolgreich, false wenn Limit erreicht
     */
    public boolean createTraderSlot(Location location) {
        PlotSlot slot = new PlotSlot(location, PlotSlot.SlotType.TRADER);
        return addSlot(slot);
    }

    /**
     * Aktualisiert die Position eines existierenden Slots.
     *
     * @param slotId UUID des Slots
     * @param newLocation Neue Position
     * @return true wenn erfolgreich, false wenn Slot nicht gefunden
     */
    public boolean updateSlotLocation(UUID slotId, Location newLocation) {
        PlotSlot oldSlot = slots.get(slotId);
        if (oldSlot == null) {
            return false;
        }

        // Erstelle neuen Slot mit gleicher ID aber neuer Position
        PlotSlot newSlot = new PlotSlot(
                slotId,
                newLocation,
                oldSlot.getSlotType(),
                oldSlot.getAssignedNPC().orElse(null),
                oldSlot.isActive()
        );

        slots.put(slotId, newSlot);
        return true;
    }

    @Override
    public String toString() {
        return "MarketPlot{" +
                "basePlot=" + basePlot.getIdentifier() +
                ", slots=" + getCurrentlyAvailableSlots() + "/" + maximumAvailableSlots +
                ", used=" + getTraderSlots().stream().filter(PlotSlot::isOccupied).count() +
                '}';
    }
}
