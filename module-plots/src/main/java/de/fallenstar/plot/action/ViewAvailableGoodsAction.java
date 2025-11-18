package de.fallenstar.plot.action;

import de.fallenstar.core.provider.Plot;
import de.fallenstar.core.ui.element.UiAction;
import org.bukkit.entity.Player;

import java.util.Objects;

/**
 * Action zum Anzeigen verfügbarer Waren auf einem Marktplatz.
 *
 * **Status:** Noch nicht implementiert (Placeholder)
 *
 * Wird zukünftig folgende Informationen zeigen:
 * - Alle verfügbaren Items und Preise
 * - Händler-Übersicht pro Item
 * - Preis-Vergleich
 * - Verfügbarkeit
 *
 * **Aktuell:** Zeigt Placeholder-Nachricht an.
 *
 * **Verwendung:**
 * ```java
 * addFunctionButton(
 *     Material.EMERALD,
 *     "§a§lVerfügbare Waren",
 *     List.of("§c§lNoch nicht implementiert"),
 *     new ViewAvailableGoodsAction(plot)
 * );
 * ```
 *
 * @author FallenStar
 * @version 2.0
 */
public final class ViewAvailableGoodsAction implements UiAction {

    private final Plot plot;

    /**
     * Konstruktor für ViewAvailableGoodsAction.
     *
     * @param plot Der Plot
     */
    public ViewAvailableGoodsAction(Plot plot) {
        this.plot = Objects.requireNonNull(plot, "Plot darf nicht null sein");
    }

    @Override
    public void execute(Player player) {
        player.sendMessage("§c§lWaren-Übersicht noch nicht implementiert!");
        player.sendMessage("§7Sprich direkt mit den Händler-NPCs");
    }

    @Override
    public String getActionName() {
        return "ViewAvailableGoods[" + plot.getIdentifier() + "][PLACEHOLDER]";
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
