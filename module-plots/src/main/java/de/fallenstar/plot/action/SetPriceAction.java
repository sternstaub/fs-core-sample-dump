package de.fallenstar.plot.action;

import de.fallenstar.core.provider.Plot;
import de.fallenstar.core.ui.element.UiAction;
import org.bukkit.entity.Player;

import java.util.Objects;

/**
 * Action zum Setzen von Preisen auf einem Grundstück.
 *
 * Aktiviert den Preis-Setzungs-Modus für den Spieler:
 * 1. Nimm ein Item in die Hand
 * 2. Rechtsklicke
 * 3. Setze den Preis im UI
 *
 * Führt den Befehl "/plot price set" aus.
 *
 * **Verwendung:**
 * ```java
 * addFunctionButton(
 *     Material.WRITABLE_BOOK,
 *     "§e§lPreise setzen",
 *     List.of("§7Aktiviert Preis-Modus"),
 *     new SetPriceAction(plot)  // Type-Safe!
 * );
 * ```
 *
 * @author FallenStar
 * @version 2.0
 */
public final class SetPriceAction implements UiAction {

    private final Plot plot;

    /**
     * Konstruktor für SetPriceAction.
     *
     * @param plot Der Plot auf dem Preise gesetzt werden
     */
    public SetPriceAction(Plot plot) {
        this.plot = Objects.requireNonNull(plot, "Plot darf nicht null sein");
    }

    @Override
    public void execute(Player player) {
        player.closeInventory();
        player.performCommand("plot price set");
    }

    @Override
    public String getActionName() {
        return "SetPrice[" + plot.getIdentifier() + "]";
    }

    /**
     * Gibt den Plot zurück.
     *
     * @return Der Plot
     */
    public Plot getPlot() {
        return plot;
    }
}
