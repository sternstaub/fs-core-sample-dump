package de.fallenstar.core.ui.element;

import org.bukkit.entity.Player;

/**
 * Interface für UI-Aktionen.
 *
 * Jedes UI-Element MUSS eine Action haben, um type-safe zu sein.
 * Actions definieren was passiert, wenn ein Spieler mit dem UI interagiert.
 *
 * Implementierungen:
 * - PageNavigationAction (Navigation zwischen Seiten)
 * - CloseUiAction (UI schließen)
 * - TeleportAction (Spieler teleportieren)
 * - ExecuteCommandAction (Command ausführen)
 * - CustomAction (Beliebige Lambda-Funktionen)
 *
 * Type-Safety Pattern:
 * - ClickableUiElement&lt;T extends UiAction&gt; erzwingt Action-Typ
 * - UiActionRegistry bindet Action ↔ UI-Element bidirektional
 *
 * @author FallenStar
 * @version 2.0
 */
public interface UiAction {

    /**
     * Führt die Action für einen Spieler aus.
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
