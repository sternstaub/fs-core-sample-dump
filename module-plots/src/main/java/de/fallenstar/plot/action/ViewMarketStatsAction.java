package de.fallenstar.plot.action;

import de.fallenstar.core.provider.Plot;
import de.fallenstar.core.ui.element.UiAction;
import org.bukkit.entity.Player;

import java.util.Objects;

/**
 * Action zum Anzeigen von Markt-Statistiken.
 *
 * **Status:** Noch nicht implementiert (Placeholder)
 *
 * Wird zukünftig folgende Statistiken zeigen:
 * - Umsatz (Gesamt, Heute, Woche, Monat)
 * - Top-Verkäufer
 * - Meistverkaufte Items
 * - Händler-Performance
 *
 * **Aktuell:** Zeigt Placeholder-Nachricht an.
 *
 * **Verwendung:**
 * ```java
 * addFunctionButton(
 *     Material.BOOK,
 *     "§e§lMarkt-Statistiken",
 *     List.of("§c§lNoch nicht implementiert"),
 *     new ViewMarketStatsAction(plot)
 * );
 * ```
 *
 * @author FallenStar
 * @version 2.0
 */
public final class ViewMarketStatsAction implements UiAction {

    private final Plot plot;

    /**
     * Konstruktor für ViewMarketStatsAction.
     *
     * @param plot Der Plot
     */
    public ViewMarketStatsAction(Plot plot) {
        this.plot = Objects.requireNonNull(plot, "Plot darf nicht null sein");
    }

    @Override
    public void execute(Player player) {
        player.sendMessage("§c§lMarkt-Statistiken noch nicht implementiert!");
        player.sendMessage("§7Geplant für zukünftige Sprints");
    }

    @Override
    public String getActionName() {
        return "ViewMarketStats[" + plot.getIdentifier() + "][PLACEHOLDER]";
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
