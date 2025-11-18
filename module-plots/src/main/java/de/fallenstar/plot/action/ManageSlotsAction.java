package de.fallenstar.plot.action;

import de.fallenstar.core.provider.Plot;
import de.fallenstar.core.ui.element.UiAction;
import org.bukkit.entity.Player;

import java.util.Objects;

/**
 * Action zum Verwalten von Händler-Slots auf einem Grundstück.
 *
 * **Status:** Noch nicht implementiert (Sprint 11-12)
 *
 * Wird zukünftig folgende Funktionen bieten:
 * - Slots erstellen (WorldAnchors)
 * - Händler auf Slots platzieren
 * - Slots aktivieren/deaktivieren
 * - Slot-Positionen setzen
 * - Fahrende Händler-Slots verwalten
 *
 * **Aktuell:** Zeigt Placeholder-Nachricht an.
 *
 * **Verwendung:**
 * ```java
 * addFunctionButton(
 *     Material.ARMOR_STAND,
 *     "§6§lHändler-Slots",
 *     List.of("§c§lRoadmap: Sprint 11-12"),
 *     new ManageSlotsAction(plot)  // Type-Safe!
 * );
 * ```
 *
 * @author FallenStar
 * @version 2.0
 */
public final class ManageSlotsAction implements UiAction {

    private final Plot plot;

    /**
     * Konstruktor für ManageSlotsAction.
     *
     * @param plot Der Plot dessen Slots verwaltet werden sollen
     */
    public ManageSlotsAction(Plot plot) {
        this.plot = Objects.requireNonNull(plot, "Plot darf nicht null sein");
    }

    @Override
    public void execute(Player player) {
        // Placeholder für Sprint 11-12
        player.sendMessage("§c§lHändler-Slots noch nicht implementiert!");
        player.sendMessage("§7Geplant für Sprint 11-12 (WorldAnchors)");
        player.sendMessage("§7");
        player.sendMessage("§7Geplante Features:");
        player.sendMessage("§7  • Slots erstellen (WorldAnchors)");
        player.sendMessage("§7  • Händler auf Slots platzieren");
        player.sendMessage("§7  • Slots aktivieren/deaktivieren");
        player.sendMessage("§7  • Fahrende Händler verwalten");
    }

    @Override
    public String getActionName() {
        return "ManageSlots[" + plot.getIdentifier() + "][PLACEHOLDER]";
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
