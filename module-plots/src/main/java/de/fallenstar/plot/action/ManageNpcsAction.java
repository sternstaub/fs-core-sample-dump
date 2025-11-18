package de.fallenstar.plot.action;

import de.fallenstar.core.provider.Plot;
import de.fallenstar.core.ui.element.UiAction;
import org.bukkit.entity.Player;

import java.util.Objects;

/**
 * Action zum Verwalten von NPCs auf einem Grundstück.
 *
 * **Status:** Noch nicht implementiert (Sprint 13-14)
 *
 * Wird zukünftig folgende Funktionen bieten:
 * - Gildenhändler spawnen
 * - Spielerhändler verwalten
 * - NPC-Positionen setzen
 * - NPC-Skins ändern
 * - Händler-Inventare verwalten
 *
 * **Aktuell:** Zeigt Placeholder-Nachricht an.
 *
 * **Verwendung:**
 * ```java
 * addFunctionButton(
 *     Material.VILLAGER_SPAWN_EGG,
 *     "§6§lNPCs verwalten",
 *     List.of("§c§lNoch nicht implementiert"),
 *     new ManageNpcsAction(plot)  // Type-Safe!
 * );
 * ```
 *
 * @author FallenStar
 * @version 2.0
 */
public final class ManageNpcsAction implements UiAction {

    private final Plot plot;

    /**
     * Konstruktor für ManageNpcsAction.
     *
     * @param plot Der Plot dessen NPCs verwaltet werden sollen
     */
    public ManageNpcsAction(Plot plot) {
        this.plot = Objects.requireNonNull(plot, "Plot darf nicht null sein");
    }

    @Override
    public void execute(Player player) {
        // Placeholder für Sprint 13-14
        player.sendMessage("§c§lNPC-Verwaltung noch nicht implementiert!");
        player.sendMessage("§7Wird in Sprint 13-14 verfügbar sein");
        player.sendMessage("§7");
        player.sendMessage("§7Geplante Features:");
        player.sendMessage("§7  • Gildenhändler spawnen");
        player.sendMessage("§7  • Spielerhändler verwalten");
        player.sendMessage("§7  • NPC-Positionen setzen");
        player.sendMessage("§7  • Händler-Inventare verwalten");
    }

    @Override
    public String getActionName() {
        return "ManageNpcs[" + plot.getIdentifier() + "][PLACEHOLDER]";
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
