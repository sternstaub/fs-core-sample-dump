package de.fallenstar.plot.action;

import de.fallenstar.core.provider.Plot;
import de.fallenstar.core.ui.element.UiAction;
import org.bukkit.entity.Player;

import java.util.Objects;

/**
 * Action zum Finden von Händlern auf einem Marktplatz.
 *
 * **Status:** Noch nicht implementiert (Placeholder)
 *
 * Wird zukünftig folgende Funktionen bieten:
 * - Liste aller aktiven Händler
 * - Händler-Positionen auf Karte
 * - Navigation zu Händlern
 * - Händler-Spezialisierungen
 *
 * **Aktuell:** Zeigt Placeholder-Nachricht an.
 *
 * **Verwendung:**
 * ```java
 * addFunctionButton(
 *     Material.COMPASS,
 *     "§e§lHändler finden",
 *     List.of("§c§lNoch nicht implementiert"),
 *     new FindTradersAction(plot)
 * );
 * ```
 *
 * @author FallenStar
 * @version 2.0
 */
public final class FindTradersAction implements UiAction {

    private final Plot plot;

    /**
     * Konstruktor für FindTradersAction.
     *
     * @param plot Der Plot
     */
    public FindTradersAction(Plot plot) {
        this.plot = Objects.requireNonNull(plot, "Plot darf nicht null sein");
    }

    @Override
    public void execute(Player player) {
        player.sendMessage("§c§lHändler-Suche noch nicht implementiert!");
        player.sendMessage("§7Suche manuell nach Händler-NPCs auf dem Plot");
    }

    @Override
    public String getActionName() {
        return "FindTraders[" + plot.getIdentifier() + "][PLACEHOLDER]";
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
