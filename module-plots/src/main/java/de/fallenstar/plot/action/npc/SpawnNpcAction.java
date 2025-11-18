package de.fallenstar.plot.action.npc;

import de.fallenstar.core.provider.Plot;
import de.fallenstar.core.ui.element.UiAction;
import de.fallenstar.plot.PlotModule;
import de.fallenstar.plot.ui.NpcSpawnMenuUi;
import org.bukkit.entity.Player;

import java.util.Objects;

/**
 * Action zum Öffnen des NPC-Spawn-Menüs.
 *
 * **Funktionalität:**
 * - Öffnet Spawn-Menü mit verfügbaren NPC-Typen
 * - Zeigt Gildenhändler, Spielerhändler, Bankier, Botschafter
 * - Ermöglicht Auswahl und Spawn an Spieler-Position
 *
 * **Type-Safety:**
 * - Compiler erzwingt Plot-Referenz
 * - PlotModule für Manager-Zugriff
 *
 * **Verwendung:**
 * ```java
 * addFunctionButton(
 *     Material.VILLAGER_SPAWN_EGG,
 *     "§a§lNPC spawnen",
 *     List.of("§7Öffnet das Spawn-Menü"),
 *     new SpawnNpcAction(plot, plotModule)
 * );
 * ```
 *
 * @author FallenStar
 * @version 2.0
 */
public final class SpawnNpcAction implements UiAction {

    private final Plot plot;
    private final PlotModule plotModule;

    /**
     * Konstruktor für SpawnNpcAction.
     *
     * @param plot Der Plot auf dem der NPC gespawnt werden soll
     * @param plotModule Das PlotModule für Manager-Zugriff
     */
    public SpawnNpcAction(Plot plot, PlotModule plotModule) {
        this.plot = Objects.requireNonNull(plot, "Plot darf nicht null sein");
        this.plotModule = Objects.requireNonNull(plotModule, "PlotModule darf nicht null sein");
    }

    @Override
    public void execute(Player player) {
        player.closeInventory();

        // Öffne NPC-Spawn-Menü
        NpcSpawnMenuUi spawnMenu = new NpcSpawnMenuUi(plot, plotModule, player);
        spawnMenu.open(player);
    }

    @Override
    public String getActionName() {
        return "SpawnNpc[" + plot.getIdentifier() + "]";
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
