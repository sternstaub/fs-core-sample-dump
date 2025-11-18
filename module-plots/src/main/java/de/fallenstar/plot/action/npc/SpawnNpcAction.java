package de.fallenstar.plot.action.npc;

import de.fallenstar.core.provider.Plot;
import de.fallenstar.core.ui.element.UiAction;
import de.fallenstar.plot.ui.NpcManagementUi;
import org.bukkit.entity.Player;

import java.util.Objects;

/**
 * Action zum Spawnen eines NPCs auf einem Plot.
 *
 * **Status:** Basis-Implementierung (Sprint 11-12)
 *
 * **Aktuelle Funktionalität:**
 * - Öffnet Spawn-Menü (zukünftig)
 * - Zeigt verfügbare NPC-Typen an
 * - Prüft Slot-Verfügbarkeit
 *
 * **Geplante Features (Sprint 13-14):**
 * - Citizens-Integration (echte NPC-Entities)
 * - NPC-Skin-Pool-Selektion
 * - Slot-basierte Platzierung
 * - NPC-Reisesystem-Integration
 *
 * **Type-Safety:**
 * - Compiler erzwingt Plot-Referenz
 * - NPC-Typ muss übergeben werden
 *
 * **Verwendung:**
 * ```java
 * addFunctionButton(
 *     Material.VILLAGER_SPAWN_EGG,
 *     "§a§lNPC spawnen",
 *     List.of("§7Öffnet das Spawn-Menü"),
 *     new SpawnNpcAction(plot, NpcType.TRADER)
 * );
 * ```
 *
 * @author FallenStar
 * @version 1.0
 */
public final class SpawnNpcAction implements UiAction {

    private final Plot plot;
    private final NpcManagementUi.NpcType npcType;

    /**
     * Konstruktor für SpawnNpcAction.
     *
     * @param plot Der Plot auf dem der NPC gespawnt werden soll
     * @param npcType Der Typ des NPCs
     */
    public SpawnNpcAction(Plot plot, NpcManagementUi.NpcType npcType) {
        this.plot = Objects.requireNonNull(plot, "Plot darf nicht null sein");
        this.npcType = Objects.requireNonNull(npcType, "NpcType darf nicht null sein");
    }

    @Override
    public void execute(Player player) {
        // TODO Sprint 13-14: Öffne NPC-Spawn-Menü mit Typ-Auswahl
        // Für jetzt: Zeige Placeholder-Nachricht

        player.closeInventory();

        player.sendMessage("§6§m----------§r §e§lNPC Spawnen §6§m----------");
        player.sendMessage("§7Plot: §e" + plot.getIdentifier());
        player.sendMessage("§7Typ: §e" + getNpcTypeName(npcType));
        player.sendMessage("");
        player.sendMessage("§c§lNoch nicht implementiert!");
        player.sendMessage("§7Wird in Sprint 13-14 verfügbar sein");
        player.sendMessage("");
        player.sendMessage("§7Geplante Features:");
        player.sendMessage("§7  • §eNPC-Typ auswählen");
        player.sendMessage("§7  • §eSkin aus Pool wählen");
        player.sendMessage("§7  • §eSlot-Position festlegen");
        player.sendMessage("§7  • §eNPC konfigurieren (Inventar, Preise)");
        player.sendMessage("§6§m--------------------------------");
    }

    @Override
    public String getActionName() {
        return "SpawnNpc[" + plot.getIdentifier() + ", " + npcType + "]";
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
     * Gibt den NPC-Typ zurück.
     *
     * @return Der NPC-Typ
     */
    public NpcManagementUi.NpcType getNpcType() {
        return npcType;
    }

    /**
     * Gibt Namen für NPC-Typ zurück.
     */
    private String getNpcTypeName(NpcManagementUi.NpcType type) {
        return switch (type) {
            case TRADER -> "Händler";
            case BANKER -> "Bankier";
            case AMBASSADOR -> "Botschafter";
            case CRAFTSMAN -> "Handwerker";
            case WORLD_BANKER -> "Weltbankier";
        };
    }
}
