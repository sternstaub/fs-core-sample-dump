package de.fallenstar.core.interaction;

import org.bukkit.entity.Player;

/**
 * Interface für alle interagierbaren Objekte im System.
 *
 * Ermöglicht einheitliche Behandlung von Klicks auf:
 * - Plots
 * - NPCs
 * - Items
 * - Custom Entities
 *
 * Features:
 * - Einheitlicher Interaktions-Handler
 * - Permission-Checks
 * - Type-Safety durch InteractionType
 *
 * **Verwendung:**
 * <pre>
 * public class TradeguildPlot implements Interactable {
 *     {@literal @}Override
 *     public boolean onInteract(Player player, InteractionContext context) {
 *         if (context.isAdminInteraction()) {
 *             return openAdminUi(player);
 *         }
 *         return openMainUi(player);
 *     }
 *
 *     {@literal @}Override
 *     public InteractionType getInteractionType() {
 *         return InteractionType.PLOT;
 *     }
 * }
 * </pre>
 *
 * @author FallenStar
 * @version 1.0
 */
public interface Interactable {

    /**
     * Behandelt eine Interaktion mit diesem Objekt.
     *
     * @param player Spieler der interagiert
     * @param context Interaktions-Kontext (Shift, Links/Rechts, etc.)
     * @return true wenn Interaktion erfolgreich behandelt wurde
     */
    boolean onInteract(Player player, InteractionContext context);

    /**
     * Prüft ob Spieler mit diesem Objekt interagieren darf.
     *
     * Standard-Implementierung erlaubt alle Interaktionen.
     * Überschreiben für Permission-Checks.
     *
     * @param player Spieler
     * @return true wenn Interaktion erlaubt
     */
    default boolean canInteract(Player player) {
        return true;
    }

    /**
     * Gibt den Interaktions-Typ zurück.
     *
     * Wird für Routing und Registrierung verwendet.
     *
     * @return InteractionType
     */
    InteractionType getInteractionType();
}
