package de.fallenstar.core.ui.element;

import org.bukkit.inventory.ItemStack;

/**
 * Interface für UI-Elemente.
 *
 * UI-Elemente repräsentieren Items in einem Inventory, die
 * entweder statisch (nur Anzeige) oder klickbar (mit Action) sind.
 *
 * Type-Safety:
 * - ClickableUiElement&lt;T extends UiAction&gt; - MUSS Action haben
 * - StaticUiElement - Keine Action, nur Anzeige
 *
 * Implementierungen:
 * - ClickableUiElement&lt;T&gt; (abstract sealed)
 * - StaticUiElement (final)
 *
 * @author FallenStar
 * @version 2.0
 */
public interface UiElement {

    /**
     * Gibt das ItemStack zurück, das im Inventory angezeigt wird.
     *
     * @return ItemStack für dieses UI-Element
     */
    ItemStack getItemStack();

    /**
     * Gibt an ob dieses Element klickbar ist.
     *
     * @return true wenn klickbar, false sonst
     */
    boolean isClickable();

    /**
     * Gibt an ob dieses Element gesperrt ist (nicht interagierbar).
     *
     * @return true wenn gesperrt, false sonst
     */
    default boolean isLocked() {
        return false;
    }
}
