package de.fallenstar.core.interaction;

import de.fallenstar.core.ui.BaseUi;
import org.bukkit.entity.Player;

import java.util.Optional;

/**
 * Interface für klickbare Objekte die ein UI öffnen.
 *
 * Kombiniert Interactable + UiActionTarget für vollständiges
 * Click-to-UI-System.
 *
 * **Features:**
 * - Automatisches UI-Öffnen beim Klick
 * - Admin-UI-Support (Shift + Admin-Permission)
 * - Self-Constructing UIs aus verfügbaren Aktionen
 * - Type-Safe durch Interactable-Vertrag
 *
 * **Workflow:**
 * 1. Spieler klickt auf Objekt (Block/Entity)
 * 2. InteractionHandler findet UiTarget
 * 3. UiTarget.onInteract() wird aufgerufen
 * 4. Default-Implementierung ruft createUi() auf
 * 5. UI wird geöffnet
 *
 * **Verwendung:**
 * <pre>
 * public class TradeguildPlot implements NamedPlot, StorageContainerPlot,
 *                                         NpcContainerPlot, UiTarget {
 *
 *     {@literal @}Override
 *     public Optional&lt;BaseUi&gt; createUi(Player player, InteractionContext context) {
 *         // UI erstellt sich selbst aus verfügbaren Aktionen
 *         PlotMainMenuUi ui = new PlotMainMenuUi(this, player);
 *         return Optional.of(ui);
 *     }
 *
 *     {@literal @}Override
 *     public InteractionType getInteractionType() {
 *         return InteractionType.PLOT;
 *     }
 *
 *     {@literal @}Override
 *     public List&lt;UiActionInfo&gt; getAvailableActions(Player player, UiContext context) {
 *         return switch (context) {
 *             case MAIN_MENU -&gt; List.of(
 *                 UiActionInfo.builder()
 *                     .id("manage_storage")
 *                     .displayName("§aLager verwalten")
 *                     .icon(Material.CHEST)
 *                     .build()
 *             );
 *             default -&gt; List.of();
 *         };
 *     }
 *
 *     {@literal @}Override
 *     public boolean executeAction(Player player, String actionId) {
 *         return switch (actionId) {
 *             case "manage_storage" -&gt; {
 *                 storageManager.openStorageUi(player, this);
 *                 yield true;
 *             }
 *             default -&gt; false;
 *         };
 *     }
 * }
 * </pre>
 *
 * @author FallenStar
 * @version 1.0
 */
public interface UiTarget extends Interactable, UiActionTarget {

    /**
     * Erstellt das UI für dieses Objekt.
     *
     * Implementierungen sollten basierend auf dem Kontext
     * das passende UI erstellen.
     *
     * @param player Spieler für den das UI erstellt wird
     * @param context Interaktions-Kontext
     * @return Optional mit UI, oder empty wenn kein UI verfügbar
     */
    Optional<BaseUi> createUi(Player player, InteractionContext context);

    /**
     * Erstellt das Admin-UI für dieses Objekt.
     *
     * Wird aufgerufen wenn Spieler Shift drückt und Admin-Permission hat.
     * Standard-Implementierung gibt leeres Optional zurück.
     *
     * @param player Admin-Spieler
     * @param context Interaktions-Kontext
     * @return Optional mit Admin-UI, oder empty wenn nicht verfügbar
     */
    default Optional<BaseUi> createAdminUi(Player player, InteractionContext context) {
        return Optional.empty();
    }

    /**
     * Öffnet das UI für den Spieler.
     *
     * Default-Implementierung erstellt UI und öffnet es.
     *
     * @param player Spieler
     * @param context Interaktions-Kontext
     * @return true wenn UI geöffnet wurde
     */
    default boolean openUi(Player player, InteractionContext context) {
        Optional<BaseUi> ui = createUi(player, context);
        if (ui.isEmpty()) {
            return false;
        }

        ui.get().open(player);
        return true;
    }

    /**
     * Öffnet das Admin-UI für den Spieler.
     *
     * @param player Admin-Spieler
     * @param context Interaktions-Kontext
     * @return true wenn Admin-UI geöffnet wurde
     */
    default boolean openAdminUi(Player player, InteractionContext context) {
        Optional<BaseUi> adminUi = createAdminUi(player, context);
        if (adminUi.isEmpty()) {
            // Fallback: Normales UI öffnen
            return openUi(player, context);
        }

        adminUi.get().open(player);
        return true;
    }

    /**
     * Default-Implementierung von onInteract().
     *
     * Behandelt Admin-Interaktionen (Shift + Permission) und
     * normale Interaktionen.
     *
     * Kann überschrieben werden für custom Logik.
     *
     * @param player Spieler
     * @param context Interaktions-Kontext
     * @return true wenn UI geöffnet wurde
     */
    @Override
    default boolean onInteract(Player player, InteractionContext context) {
        // Admin-Interaktion (Shift + Permission)
        if (context.isAdminInteraction() && player.hasPermission("fallenstar.admin")) {
            return openAdminUi(player, context);
        }

        // Normale Interaktion
        return openUi(player, context);
    }
}
