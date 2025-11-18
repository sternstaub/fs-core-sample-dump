package de.fallenstar.plot.action.npc;

import de.fallenstar.core.provider.Plot;
import de.fallenstar.core.ui.element.UiAction;
import org.bukkit.entity.Player;

import java.util.Objects;
import java.util.UUID;

/**
 * Action zum Kaufen eines NPC-Slots auf einem Plot.
 *
 * **Status:** Basis-Implementierung (Sprint 11-12)
 *
 * **Aktuelle Funktionalität:**
 * - Placeholder für NPC-Slot-Kauf
 * - Zeigt Kosten und Features an
 *
 * **Geplante Features (Sprint 13-14):**
 * - Economy-Integration (500 Sterne Kosten)
 * - Slot-Zuweisung auf Plot
 * - Automatisches NPC-Spawning
 * - Virtuelles Händler-Inventar erstellen
 *
 * **Type-Safety:**
 * - Compiler erzwingt Plot-Referenz
 * - Player-ID muss übergeben werden
 *
 * **Verwendung:**
 * ```java
 * addFunctionButton(
 *     Material.EMERALD,
 *     "§a§lNPC-Slot kaufen",
 *     List.of("§7Kosten: §e500 Sterne"),
 *     new BuyNpcSlotAction(plot, playerId)
 * );
 * ```
 *
 * @author FallenStar
 * @version 1.0
 */
public final class BuyNpcSlotAction implements UiAction {

    private final Plot plot;
    private final UUID playerId;

    /**
     * Konstruktor für BuyNpcSlotAction.
     *
     * @param plot Der Plot auf dem der Slot gekauft werden soll
     * @param playerId Die UUID des Spielers
     */
    public BuyNpcSlotAction(Plot plot, UUID playerId) {
        this.plot = Objects.requireNonNull(plot, "Plot darf nicht null sein");
        this.playerId = Objects.requireNonNull(playerId, "PlayerId darf nicht null sein");
    }

    @Override
    public void execute(Player player) {
        // TODO Sprint 13-14: Implementiere NPC-Slot-Kauf
        // - Prüfe Economy-Balance (500 Sterne)
        // - Erstelle Slot auf Plot
        // - Spawne Spielerhändler-NPC
        // - Erstelle virtuelles Händler-Inventar

        player.closeInventory();

        player.sendMessage("§6§m----------§r §e§lNPC-Slot kaufen §6§m----------");
        player.sendMessage("§7Plot: §e" + plot.getIdentifier());
        player.sendMessage("");
        player.sendMessage("§7Kosten: §e500 Sterne");
        player.sendMessage("§7NPC-Typ: §eSpielhändler");
        player.sendMessage("");
        player.sendMessage("§c§lNoch nicht implementiert!");
        player.sendMessage("§7Wird in Sprint 13-14 verfügbar sein");
        player.sendMessage("");
        player.sendMessage("§7Geplante Features:");
        player.sendMessage("§7  • §eSlot auf Grundstück zuweisen");
        player.sendMessage("§7  • §eEigenes Händler-Inventar (54 Slots)");
        player.sendMessage("§7  • §ePreise selbst festlegen");
        player.sendMessage("§7  • §eNPC-Position wählbar");
        player.sendMessage("§7  • §eHandel mit anderen Spielern");
        player.sendMessage("§6§m--------------------------------");
    }

    @Override
    public String getActionName() {
        return "BuyNpcSlot[" + plot.getIdentifier() + ", Player:" + playerId + "]";
    }

    /**
     * Gibt den Plot zurück.
     *
     * @return Der Plot
     */
    public Plot getPlot() {
        return plot;
    }

    /**
     * Gibt die Spieler-ID zurück.
     *
     * @return Die Spieler-ID
     */
    public UUID getPlayerId() {
        return playerId;
    }
}
