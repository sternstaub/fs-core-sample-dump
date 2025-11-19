package de.fallenstar.core.interaction;

import de.fallenstar.core.interaction.action.UiActionInfo;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Interface für Objekte die UI-Aktionen bereitstellen.
 *
 * Objekte definieren ihre eigenen UI-Buttons kontextabhängig.
 *
 * **Konzept:**
 * - Objekte wissen, welche Aktionen sie anbieten
 * - UIs generieren sich automatisch aus verfügbaren Aktionen
 * - Kontext bestimmt welche Aktionen sichtbar sind
 *
 * **Verwendung:**
 * <pre>
 * public class TradeguildPlot implements UiActionTarget {
 *     {@literal @}Override
 *     public List&lt;UiActionInfo&gt; getAvailableActions(Player player, UiContext context) {
 *         return switch (context) {
 *             case MAIN_MENU -&gt; List.of(
 *                 UiActionInfo.builder()
 *                     .id("manage_storage")
 *                     .displayName("§aLager verwalten")
 *                     .icon(Material.CHEST)
 *                     .build(),
 *                 UiActionInfo.builder()
 *                     .id("manage_npcs")
 *                     .displayName("§bNPCs verwalten")
 *                     .icon(Material.VILLAGER_SPAWN_EGG)
 *                     .build()
 *             );
 *             case STORAGE_MENU -&gt; storageManager.getStorageActions(this);
 *             default -&gt; List.of();
 *         };
 *     }
 * }
 * </pre>
 *
 * @author FallenStar
 * @version 1.0
 */
public interface UiActionTarget {

    /**
     * Gibt alle verfügbaren UI-Aktionen für einen Kontext zurück.
     *
     * @param player Spieler der das UI öffnet
     * @param context UI-Kontext (MAIN_MENU, STORAGE_MENU, etc.)
     * @return Liste von UiActionInfo
     */
    List<UiActionInfo> getAvailableActions(Player player, UiContext context);

    /**
     * Führt eine UI-Aktion aus.
     *
     * Wird von UIs aufgerufen wenn Spieler einen Button klickt.
     *
     * @param player Spieler der die Aktion ausführt
     * @param actionId Aktion-ID
     * @return true wenn Aktion erfolgreich ausgeführt wurde
     */
    boolean executeAction(Player player, String actionId);
}
