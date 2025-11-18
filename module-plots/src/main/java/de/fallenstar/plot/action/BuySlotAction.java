package de.fallenstar.plot.action;

import de.fallenstar.core.ui.element.UiAction;
import de.fallenstar.plot.slot.SlottedPlot;
import org.bukkit.entity.Player;

import java.util.Objects;

/**
 * Action zum Kaufen eines neuen Händler-Slots.
 *
 * Führt den Befehl "/plot slots buy" aus, der einen neuen Slot kauft.
 *
 * **Voraussetzungen:**
 * - Spieler ist Plot-Besitzer
 * - Plot hat noch nicht das Slot-Limit erreicht
 * - Spieler hat genug Währung (konfigurierbar)
 *
 * **Verwendung:**
 * ```java
 * if (marketPlot.canPurchaseMoreSlots()) {
 *     addFunctionButton(
 *         Material.GOLD_INGOT,
 *         "§a§lNeuen Slot kaufen",
 *         List.of("§7Preis: §e100 Sterne"),
 *         new BuySlotAction(marketPlot)
 *     );
 * }
 * ```
 *
 * @author FallenStar
 * @version 2.0
 */
public final class BuySlotAction implements UiAction {

    private final SlottedPlot slottedPlot;

    /**
     * Konstruktor für BuySlotAction.
     *
     * @param slottedPlot Der SlottedPlot (z.B. MarketPlot)
     */
    public BuySlotAction(SlottedPlot slottedPlot) {
        this.slottedPlot = Objects.requireNonNull(slottedPlot, "SlottedPlot darf nicht null sein");
    }

    @Override
    public void execute(Player player) {
        player.closeInventory();
        player.performCommand("plot slots buy");
    }

    @Override
    public String getActionName() {
        return "BuySlot[" + slottedPlot.getIdentifier() + "]";
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
