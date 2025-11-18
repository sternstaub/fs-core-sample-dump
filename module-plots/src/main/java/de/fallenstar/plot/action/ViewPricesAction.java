package de.fallenstar.plot.action;

import de.fallenstar.core.provider.Plot;
import de.fallenstar.core.ui.element.UiAction;
import org.bukkit.entity.Player;

import java.util.Objects;

/**
 * Action zum Anzeigen der Preisliste eines Grundstücks.
 *
 * Führt den Befehl "/plot price list" aus, der alle
 * definierten Preise auf dem Grundstück anzeigt.
 *
 * **Verwendung:**
 * ```java
 * addFunctionButton(
 *     Material.BOOK,
 *     "§e§lPreise anzeigen",
 *     List.of("§7Zeigt alle Preise"),
 *     new ViewPricesAction(plot)  // Type-Safe!
 * );
 * ```
 *
 * @author FallenStar
 * @version 2.0
 */
public final class ViewPricesAction implements UiAction {

    private final Plot plot;

    /**
     * Konstruktor für ViewPricesAction.
     *
     * @param plot Der Plot dessen Preise angezeigt werden sollen
     */
    public ViewPricesAction(Plot plot) {
        this.plot = Objects.requireNonNull(plot, "Plot darf nicht null sein");
    }

    @Override
    public void execute(Player player) {
        player.closeInventory();
        player.performCommand("plot price list");
    }

    @Override
    public String getActionName() {
        return "ViewPrices[" + plot.getIdentifier() + "]";
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
