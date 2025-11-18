package de.fallenstar.plot.action;

import de.fallenstar.core.provider.Plot;
import de.fallenstar.core.ui.element.UiAction;
import org.bukkit.entity.Player;

import java.util.Objects;

/**
 * Action zum Anzeigen der Grundstücks-Informationen.
 *
 * Führt den Befehl "/plot info" aus, der detaillierte
 * Informationen über das Grundstück anzeigt:
 * - Besitzer
 * - Typ (Handelsgilde, Botschaft, etc.)
 * - Größe
 * - Permissions
 * - etc.
 *
 * **Verwendung:**
 * ```java
 * addFunctionButton(
 *     Material.MAP,
 *     "§e§lGrundstücks-Info",
 *     List.of("§7Zeigt Details"),
 *     new ViewPlotInfoAction(plot)  // Type-Safe!
 * );
 * ```
 *
 * @author FallenStar
 * @version 2.0
 */
public final class ViewPlotInfoAction implements UiAction {

    private final Plot plot;

    /**
     * Konstruktor für ViewPlotInfoAction.
     *
     * @param plot Der Plot dessen Infos angezeigt werden sollen
     */
    public ViewPlotInfoAction(Plot plot) {
        this.plot = Objects.requireNonNull(plot, "Plot darf nicht null sein");
    }

    @Override
    public void execute(Player player) {
        player.closeInventory();
        player.performCommand("plot info");
    }

    @Override
    public String getActionName() {
        return "ViewPlotInfo[" + plot.getIdentifier() + "]";
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
