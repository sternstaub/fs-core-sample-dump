package de.fallenstar.plot.action;

import de.fallenstar.core.ui.element.UiAction;
import de.fallenstar.plot.slot.SlottedPlot;
import org.bukkit.entity.Player;

import java.util.Objects;

/**
 * Action zum Verwalten von Händler-Slots auf einem Grundstück.
 *
 * Öffnet die SlotManagementUI, die alle verfügbaren Slots zeigt.
 *
 * **Features:**
 * - Slot-Liste mit Status (belegt/frei)
 * - Händler auf Slots platzieren
 * - Händler von Slots entfernen
 * - Neue Slots kaufen
 *
 * **Verwendung:**
 * ```java
 * addFunctionButton(
 *     Material.ARMOR_STAND,
 *     "§6§lHändler-Slots verwalten",
 *     List.of("§7Verwalte Händler-Slot-Positionen"),
 *     new ManageTraderSlotsAction(marketPlot)
 * );
 * ```
 *
 * @author FallenStar
 * @version 2.0
 */
public final class ManageTraderSlotsAction implements UiAction {

    private final SlottedPlot slottedPlot;

    /**
     * Konstruktor für ManageTraderSlotsAction.
     *
     * @param slottedPlot Der SlottedPlot (z.B. MarketPlot)
     */
    public ManageTraderSlotsAction(SlottedPlot slottedPlot) {
        this.slottedPlot = Objects.requireNonNull(slottedPlot, "SlottedPlot darf nicht null sein");
    }

    @Override
    public void execute(Player player) {
        player.closeInventory();
        // Nutze Command-basierte Lösung (SlotManagementUI wird via Command geöffnet)
        player.performCommand("plot slots list");
    }

    @Override
    public String getActionName() {
        return "ManageTraderSlots[" + slottedPlot.getIdentifier() + "]";
    }

    /**
     * Gibt den SlottedPlot zurück.
     *
     * @return Der SlottedPlot
     */
    public SlottedPlot getSlottedPlot() {
        return slottedPlot;
    }
}
