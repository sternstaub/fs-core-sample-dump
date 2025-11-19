package de.fallenstar.core.interaction.action;

import org.bukkit.entity.Player;

import java.util.Optional;

/**
 * Interface für UI-Aktionen die validiert werden müssen.
 *
 * Ermöglicht Validierung BEVOR Aktion ausgeführt wird.
 *
 * **Verwendung:**
 * <pre>
 * public class SetStoragePriceAction implements ValidatedUiAction {
 *     {@literal @}Override
 *     public Optional&lt;String&gt; validate(Player player) {
 *         if (!isPlotOwner(player)) {
 *             return Optional.of("§cDu musst der Plot-Besitzer sein!");
 *         }
 *         return Optional.empty();
 *     }
 *
 *     {@literal @}Override
 *     public void execute(Player player) {
 *         // Aktion ausführen (Validierung bereits erfolgt)
 *         openStoragePriceUi(player);
 *     }
 * }
 * </pre>
 *
 * @author FallenStar
 * @version 1.0
 */
public interface ValidatedUiAction {

    /**
     * Validiert ob die Aktion ausgeführt werden kann.
     *
     * @param player Spieler der die Aktion ausführen möchte
     * @return Optional mit Fehler-Nachricht, oder empty wenn valide
     */
    Optional<String> validate(Player player);

    /**
     * Führt die Aktion aus.
     *
     * Wird nur aufgerufen wenn validate() erfolgreich war.
     *
     * @param player Spieler
     */
    void execute(Player player);

    /**
     * Führt die Aktion mit Validierung aus.
     *
     * Prüft zuerst validate(), dann execute().
     *
     * @param player Spieler
     * @return true wenn Aktion erfolgreich ausgeführt wurde
     */
    default boolean executeValidated(Player player) {
        Optional<String> error = validate(player);
        if (error.isPresent()) {
            player.sendMessage(error.get());
            return false;
        }

        execute(player);
        return true;
    }
}
