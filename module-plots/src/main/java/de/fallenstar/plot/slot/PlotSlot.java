package de.fallenstar.plot.slot;

import org.bukkit.Location;

import java.util.Optional;
import java.util.UUID;

/**
 * Repräsentiert einen NPC-Slot auf einem Grundstück.
 *
 * Slots sind Positionen, an denen NPCs platziert werden können.
 * Verschiedene Grundstückstypen unterstützen verschiedene Slot-Typen.
 *
 * Features:
 * - Position-basierte NPC-Platzierung
 * - Slot-Typen (Händler, Bankier, Botschafter, etc.)
 * - Aktiv/Inaktiv Status
 * - Optional: Referenz auf platzierten NPC
 *
 * Verwendung:
 * <pre>
 * PlotSlot slot = new PlotSlot(location, SlotType.TRADER);
 * slot.assignNPC(npcUuid);
 * if (slot.isOccupied()) {
 *     // Slot ist belegt
 * }
 * </pre>
 *
 * @author FallenStar
 * @version 1.0
 */
public class PlotSlot {

    private final UUID slotId;
    private final Location location;
    private final SlotType slotType;
    private UUID assignedNPC;
    private boolean active;

    /**
     * Slot-Typen für verschiedene NPC-Arten.
     */
    public enum SlotType {
        TRADER("Händler"),
        BANKER("Bankier"),
        AMBASSADOR("Botschafter"),
        CRAFTSMAN("Handwerker"),
        TRAVELING_MERCHANT("Fahrender Händler");

        private final String displayName;

        SlotType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * Konstruktor für einen neuen Slot.
     *
     * @param location Position des Slots
     * @param slotType Typ des Slots
     */
    public PlotSlot(Location location, SlotType slotType) {
        this.slotId = UUID.randomUUID();
        this.location = location.clone();
        this.slotType = slotType;
        this.assignedNPC = null;
        this.active = true;
    }

    /**
     * Konstruktor für Deserialisierung aus Config.
     *
     * @param slotId ID des Slots
     * @param location Position des Slots
     * @param slotType Typ des Slots
     * @param assignedNPC UUID des zugewiesenen NPCs (oder null)
     * @param active Ob der Slot aktiv ist
     */
    public PlotSlot(UUID slotId, Location location, SlotType slotType, UUID assignedNPC, boolean active) {
        this.slotId = slotId;
        this.location = location.clone();
        this.slotType = slotType;
        this.assignedNPC = assignedNPC;
        this.active = active;
    }

    /**
     * Weist diesem Slot einen NPC zu.
     *
     * @param npcUuid UUID des NPCs
     * @return true wenn erfolgreich, false wenn Slot bereits belegt
     */
    public boolean assignNPC(UUID npcUuid) {
        if (isOccupied()) {
            return false;
        }
        this.assignedNPC = npcUuid;
        return true;
    }

    /**
     * Entfernt den zugewiesenen NPC von diesem Slot.
     */
    public void removeNPC() {
        this.assignedNPC = null;
    }

    /**
     * Prüft ob dieser Slot belegt ist.
     *
     * @return true wenn ein NPC zugewiesen ist
     */
    public boolean isOccupied() {
        return assignedNPC != null;
    }

    /**
     * Gibt die UUID des zugewiesenen NPCs zurück.
     *
     * @return Optional mit NPC-UUID, oder empty wenn nicht belegt
     */
    public Optional<UUID> getAssignedNPC() {
        return Optional.ofNullable(assignedNPC);
    }

    /**
     * Gibt die ID dieses Slots zurück.
     *
     * @return Slot-UUID
     */
    public UUID getSlotId() {
        return slotId;
    }

    /**
     * Gibt die Position dieses Slots zurück.
     *
     * @return Location (geklont)
     */
    public Location getLocation() {
        return location.clone();
    }

    /**
     * Gibt den Typ dieses Slots zurück.
     *
     * @return SlotType
     */
    public SlotType getSlotType() {
        return slotType;
    }

    /**
     * Prüft ob dieser Slot aktiv ist.
     *
     * @return true wenn aktiv
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Setzt den Aktiv-Status dieses Slots.
     *
     * @param active Neuer Status
     */
    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public String toString() {
        return "PlotSlot{" +
                "id=" + slotId +
                ", type=" + slotType.getDisplayName() +
                ", location=" + location +
                ", occupied=" + isOccupied() +
                ", active=" + active +
                '}';
    }
}
