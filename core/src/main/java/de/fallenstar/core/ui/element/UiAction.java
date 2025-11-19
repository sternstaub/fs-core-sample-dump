package de.fallenstar.core.ui.element;

import org.bukkit.entity.Player;

/**
 * Interface für UI-Aktionen.
 *
 * Jedes UI-Element MUSS eine Action haben, um type-safe zu sein.
 * Actions definieren was passiert, wenn ein Spieler mit dem UI interagiert.
 *
 * **Command Pattern:**
 * Actions kapseln Logik + Berechtigungsprüfung + Objekt-Referenz.
 * Beispiel: SetNameAction hat Plot-Referenz und prüft isOwner().
 *
 * Implementierungen:
 * - PageNavigationAction (Navigation zwischen Seiten)
 * - CloseUiAction (UI schließen)
 * - TeleportAction (Spieler teleportieren)
 * - ExecuteCommandAction (Command ausführen)
 * - CustomAction (Beliebige Lambda-Funktionen)
 * - SetNameAction, ManageNpcsAction, etc. (Plot-gebundene Actions)
 *
 * Type-Safety Pattern:
 * - ClickableUiElement&lt;T extends UiAction&gt; erzwingt Action-Typ
 * - UiActionRegistry bindet Action ↔ UI-Element bidirektional
 *
 * @author FallenStar
 * @version 3.0
 */
public interface UiAction {

    /**
     * Prüft ob ein Spieler diese Action ausführen darf.
     *
     * Diese Methode wird VOR execute() aufgerufen und erlaubt
     * Actions ihre Berechtigungslogik selbst zu kapseln.
     *
     * Beispiele:
     * - Plot-Owner-Check: return plot.isOwner(player);
     * - Permission-Check: return player.hasPermission("...");
     * - Custom-Logik: return player.getLevel() >= 10;
     *
     * **Default:** Immer erlaubt (true)
     *
     * @param player Der Spieler der die Action ausführen möchte
     * @return true wenn Spieler berechtigt ist, sonst false
     */
    default boolean canExecute(Player player) {
        return true; // Default: Keine Einschränkungen
    }

    /**
     * Führt die Action für einen Spieler aus.
     *
     * Diese Methode wird nur aufgerufen wenn canExecute() true zurückgibt!
     * Die UI-Systeme prüfen automatisch vor Ausführung.
     *
     * @param player Der Spieler der die Action ausgelöst hat
     */
    void execute(Player player);

    /**
     * Gibt einen beschreibenden Namen der Action zurück.
     *
     * Nützlich für Debugging und Logging.
     *
     * @return Action-Name
     */
    default String getActionName() {
        return this.getClass().getSimpleName();
    }
}
