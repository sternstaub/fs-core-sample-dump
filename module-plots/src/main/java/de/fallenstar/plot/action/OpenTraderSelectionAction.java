package de.fallenstar.plot.action;

import de.fallenstar.core.ui.element.UiAction;
import de.fallenstar.plot.slot.PlotSlot;
import org.bukkit.entity.Player;

import java.util.Objects;

/**
 * Action zum Öffnen der Händler-Auswahl für einen Slot.
 *
 * Öffnet die TraderSelectionUI, die alle verfügbaren Händler
 * aus PlotRegistry-Handelsgilden zeigt.
 *
 * **Workflow:**
 * 1. Spieler klickt auf freien Slot in SlotManagementUi
 * 2. TraderSelectionUI öffnet sich
 * 3. Spieler wählt Händler aus Handelsgilde
 * 4. Händler reist zum Slot (NPC-Reisesystem)
 *
 * **Verwendung:**
 * ```java
 * if (!plotSlot.isOccupied()) {
 *     var button = new ClickableUiElement.CustomButton<>(
 *         slotItem,
 *         new OpenTraderSelectionAction(plotSlot)
 *     );
 * }
 * ```
 *
 * @author FallenStar
 * @version 2.0
 */
public final class OpenTraderSelectionAction implements UiAction {

    private final PlotSlot slot;

    /**
     * Konstruktor für OpenTraderSelectionAction.
     *
     * @param slot Der PlotSlot
     */
    public OpenTraderSelectionAction(PlotSlot slot) {
        this.slot = Objects.requireNonNull(slot, "PlotSlot darf nicht null sein");
    }

    @Override
    public void execute(Player player) {
        // TODO: Öffne TraderSelectionUI (wird in nächster Phase implementiert)
        player.sendMessage("§a§lHändler-Auswahl:");
        player.sendMessage("§7Slot: §e" + slot.getSlotId().toString().substring(0, 8));
        player.sendMessage("§c§lTraderSelectionUI noch nicht implementiert!");
        player.sendMessage("§7Geplant für nächste Migrationsphasen");
    }

    @Override
    public String getActionName() {
        return "OpenTraderSelection[" + slot.getSlotId() + "]";
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
