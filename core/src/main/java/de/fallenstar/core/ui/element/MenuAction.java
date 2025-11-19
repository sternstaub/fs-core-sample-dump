package de.fallenstar.core.ui.element;

import org.bukkit.entity.Player;

import java.util.List;

/**
 * Interface für Actions mit Untermenüs.
 *
 * **Konzept:**
 * Actions können entweder direkt ausgeführt werden ODER ein Untermenü öffnen.
 * Das System entscheidet automatisch basierend auf getSubActions().
 *
 * **Hierarchische Menüs:**
 * ```
 * Main Menu
 *   ├─ Manage NPCs (MenuAction)
 *   │    ├─ NPC #1 (Action)
 *   │    ├─ NPC #2 (Action)
 *   │    └─ NPC #3 (Action)
 *   └─ Manage Storage (Action)
 * ```
 *
 * **Verwendung:**
 * <pre>
 * public class ManageNpcsAction extends PlotAction {
 *     {@literal @}Override
 *     public List&lt;PlotAction&gt; getSubActions(Player player) {
 *         // Dynamisches Untermenü mit allen NPCs
 *         return plot.getNpcIds().stream()
 *             .map(npcId -> new ConfigureNpcAction(plot, npcId))
 *             .toList();
 *     }
 *
 *     {@literal @}Override
 *     public String getSubMenuTitle() {
 *         return "§bNPCs auf " + plot.getDisplayName();
 *     }
 * }
 * </pre>
 *
 * **Automatisches Verhalten:**
 * - Wenn getSubActions() nicht leer → Öffne Untermenü
 * - Wenn getSubActions() leer → Führe executeAction() aus
 *
 * @author FallenStar
 * @version 1.0
 */
public interface MenuAction {

    /**
     * Gibt den Titel für das Untermenü zurück.
     *
     * Wird nur verwendet wenn hasSubMenu() true ist.
     *
     * @return Untermenü-Titel
     */
    default String getSubMenuTitle() {
        return "§6Untermenü";
    }

    /**
     * Gibt Sub-Actions für das Untermenü zurück.
     *
     * **Default:** Leere Liste = Kein Untermenü
     *
     * **Implementierungen:**
     * - Statisch: return List.of(action1, action2, action3);
     * - Dynamisch: return items.stream().map(i -> new ItemAction(i)).toList();
     *
     * @param player Der betrachtende Spieler (für Owner-Filterung)
     * @return Liste von Sub-Actions (GuiRenderable)
     */
    default List<? extends GuiRenderable> getSubActions(Player player) {
        return List.of(); // Default: Kein Untermenü
    }

    /**
     * Prüft ob diese Action ein Untermenü hat.
     *
     * Wird automatisch von execute() geprüft um zu entscheiden:
     * - true → Öffne Untermenü
     * - false → Führe executeAction() aus
     *
     * @param player Der Spieler (für dynamische Untermenüs)
     * @return true wenn Sub-Actions vorhanden
     */
    default boolean hasSubMenu(Player player) {
        List<? extends GuiRenderable> subActions = getSubActions(player);
        return subActions != null && !subActions.isEmpty();
    }
}
