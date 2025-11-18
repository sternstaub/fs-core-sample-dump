package de.fallenstar.core.ui.element;

import org.bukkit.inventory.ItemStack;

import java.util.Objects;

/**
 * Statisches UI-Element - nur zur Anzeige, nicht klickbar.
 *
 * Verwendung:
 * - Trennlinien (Glass Panes)
 * - Info-Anzeigen (z.B. "Seite 1 / 5")
 * - Dekorative Elemente
 * - Locked Items (z.B. "Funktion bald verfügbar")
 *
 * Type-Safety:
 * - Keine Action erforderlich (da nicht klickbar)
 * - Immutable (final fields)
 *
 * Beispiel:
 * ```java
 * // Trennlinie
 * var separator = new StaticUiElement(
 *     new ItemStack(Material.GRAY_STAINED_GLASS_PANE)
 * );
 *
 * // Info-Element
 * var info = new StaticUiElement(
 *     createInfoItem("Seite 1 / 5")
 * );
 * ```
 *
 * @author FallenStar
 * @version 2.0
 */
public final class StaticUiElement implements UiElement {

    private final ItemStack itemStack;
    private final boolean locked;

    /**
     * Konstruktor für statisches UI-Element.
     *
     * @param itemStack ItemStack für die Anzeige
     */
    public StaticUiElement(ItemStack itemStack) {
        this(itemStack, false);
    }

    /**
     * Konstruktor für statisches UI-Element mit Lock-Status.
     *
     * @param itemStack ItemStack für die Anzeige
     * @param locked Ob das Element gesperrt ist (z.B. "Bald verfügbar")
     */
    public StaticUiElement(ItemStack itemStack, boolean locked) {
        this.itemStack = Objects.requireNonNull(itemStack, "ItemStack darf nicht null sein");
        this.locked = locked;
    }

    @Override
    public ItemStack getItemStack() {
        return itemStack;
    }

    @Override
    public boolean isClickable() {
        return false;
    }

    @Override
    public boolean isLocked() {
        return locked;
    }
}
