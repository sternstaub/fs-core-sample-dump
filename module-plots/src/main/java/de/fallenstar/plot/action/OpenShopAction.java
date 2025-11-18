package de.fallenstar.plot.action;

import de.fallenstar.core.provider.Plot;
import de.fallenstar.core.ui.element.UiAction;
import org.bukkit.entity.Player;

import java.util.Objects;

/**
 * Action zum Öffnen des Shop-UI auf einem Grundstück.
 *
 * **Status:** Noch nicht implementiert (zukünftiger Sprint)
 *
 * Wird zukünftig ein Shop-UI öffnen, in dem Spieler:
 * - Items kaufen können
 * - Preise sehen können
 * - Mit Gildenhändlern handeln können
 *
 * **Aktuell:** Zeigt Placeholder-Nachricht an.
 *
 * **Verwendung:**
 * ```java
 * addFunctionButton(
 *     Material.EMERALD,
 *     "§a§lItems kaufen",
 *     List.of("§c§lNoch nicht implementiert"),
 *     new OpenShopAction(plot)  // Type-Safe!
 * );
 * ```
 *
 * @author FallenStar
 * @version 2.0
 */
public final class OpenShopAction implements UiAction {

    private final Plot plot;

    /**
     * Konstruktor für OpenShopAction.
     *
     * @param plot Der Plot dessen Shop geöffnet werden soll
     */
    public OpenShopAction(Plot plot) {
        this.plot = Objects.requireNonNull(plot, "Plot darf nicht null sein");
    }

    @Override
    public void execute(Player player) {
        // Placeholder für zukünftiges Shop-System
        player.sendMessage("§c§lShop-System noch nicht implementiert!");
        player.sendMessage("§7Nutze vorerst die Gildenhändler-NPCs");
        player.sendMessage("§7");
        player.sendMessage("§7Geplante Features:");
        player.sendMessage("§7  • Items kaufen mit Vault-Guthaben");
        player.sendMessage("§7  • Items kaufen mit Münzen");
        player.sendMessage("§7  • Händler-Inventar-Verwaltung");
        player.sendMessage("§7  • Trading-UI Integration");
    }

    @Override
    public String getActionName() {
        return "OpenShop[" + plot.getIdentifier() + "][PLACEHOLDER]";
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
