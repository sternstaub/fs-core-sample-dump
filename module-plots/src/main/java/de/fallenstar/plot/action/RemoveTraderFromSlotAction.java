package de.fallenstar.plot.action;

import de.fallenstar.core.ui.element.UiAction;
import de.fallenstar.plot.slot.PlotSlot;
import org.bukkit.entity.Player;

import java.util.Objects;

/**
 * Action zum Entfernen eines Händlers von einem Slot.
 *
 * Entfernt den zugewiesenen NPC von einem Slot und gibt
 * den Slot wieder frei.
 *
 * **Features:**
 * - Entfernt NPC von Slot
 * - Gibt Slot frei
 * - Zeigt Info-Nachricht
 *
 * **Verwendung:**
 * ```java
 * if (plotSlot.isOccupied()) {
 *     var button = new ClickableUiElement.CustomButton<>(
 *         slotItem,
 *         new RemoveTraderFromSlotAction(plotSlot)
 *     );
 * }
 * ```
 *
 * @author FallenStar
 * @version 2.0
 */
public final class RemoveTraderFromSlotAction implements UiAction {

    private final PlotSlot slot;

    /**
     * Konstruktor für RemoveTraderFromSlotAction.
     *
     * @param slot Der PlotSlot
     */
    public RemoveTraderFromSlotAction(PlotSlot slot) {
        this.slot = Objects.requireNonNull(slot, "PlotSlot darf nicht null sein");
    }

    @Override
    public void execute(Player player) {
        if (!slot.isOccupied()) {
            player.sendMessage("§c§lSlot ist bereits leer!");
            return;
        }

        // Zeige Info
        String npcId = slot.getAssignedNPC().map(uuid -> uuid.toString().substring(0, 8)).orElse("Unbekannt");
        player.sendMessage("§7Händler: §e" + npcId);
        player.sendMessage("§7Rechtsklick um zu entfernen (noch nicht implementiert)");

        // TODO: Implementiere NPC-Entfernung
        // slot.removeNPC();
        // player.sendMessage("§a✓ Händler entfernt!");
    }

    @Override
    public String getActionName() {
        return "RemoveTraderFromSlot[" + slot.getSlotId() + "]";
    }

    /**
     * Gibt den PlotSlot zurück.
     *
     * @return Der PlotSlot
     */
    public PlotSlot getSlot() {
        return slot;
    }
}
