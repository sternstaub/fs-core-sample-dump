package de.fallenstar.core.ui.element;

import org.bukkit.inventory.ItemStack;

import java.util.Objects;

/**
 * Abstrakte Basis-Klasse für klickbare UI-Elemente.
 *
 * **Type-Safety Pattern:**
 * - Generic &lt;T extends UiAction&gt; erzwingt, dass jedes klickbare Element eine Action hat
 * - Sealed class erzwingt, dass nur definierte Subtypen existieren können
 * - final field `action` verhindert nachträgliche Änderungen
 *
 * **Compiler-Garantien:**
 * - Kein ClickableUiElement ohne Action (Constructor erzwingt non-null)
 * - Action-Typ ist zur Compile-Time bekannt
 * - Alle Subtypen sind bekannt (sealed permits)
 *
 * Implementierungen:
 * - NavigateLeftButton extends ClickableUiElement&lt;PageNavigationAction&gt;
 * - NavigateRightButton extends ClickableUiElement&lt;PageNavigationAction&gt;
 * - CloseButton extends ClickableUiElement&lt;CloseUiAction&gt;
 * - CustomButton&lt;T&gt; extends ClickableUiElement&lt;T&gt; (für benutzerdefinierte Actions)
 *
 * @param <T> Der Action-Typ (muss UiAction implementieren)
 *
 * @author FallenStar
 * @version 2.0
 */
public abstract sealed class ClickableUiElement<T extends UiAction>
        implements UiElement
        permits ClickableUiElement.CustomButton,
                de.fallenstar.core.ui.element.navigation.NavigateLeftButton,
                de.fallenstar.core.ui.element.navigation.NavigateRightButton,
                de.fallenstar.core.ui.element.navigation.CloseButton {

    /**
     * Die Action die beim Click ausgeführt wird.
     * FINAL = Unveränderlich nach Konstruktion!
     */
    private final T action;

    /**
     * Das ItemStack das im Inventory angezeigt wird.
     * FINAL = Unveränderlich nach Konstruktion!
     */
    private final ItemStack itemStack;

    /**
     * Konstruktor für klickbare UI-Elemente.
     *
     * **Type-Safety:**
     * - action MUSS übergeben werden (nicht-null)
     * - itemStack MUSS übergeben werden (nicht-null)
     *
     * @param itemStack ItemStack für den Button
     * @param action Action die beim Click ausgeführt wird
     * @throws NullPointerException wenn action oder itemStack null ist
     */
    protected ClickableUiElement(ItemStack itemStack, T action) {
        this.itemStack = Objects.requireNonNull(itemStack, "ItemStack darf nicht null sein");
        this.action = Objects.requireNonNull(action, "Action darf nicht null sein");
    }

    /**
     * Gibt die Action zurück.
     *
     * @return Die Action (niemals null!)
     */
    public final T getAction() {
        return action;
    }

    @Override
    public final ItemStack getItemStack() {
        return itemStack;
    }

    @Override
    public final boolean isClickable() {
        return true;  // Immer klickbar
    }

    /**
     * CustomButton - Generisches klickbares UI-Element für beliebige Actions.
     *
     * Verwendung:
     * ```java
     * var button = new CustomButton&lt;&gt;(itemStack, new MyCustomAction());
     * ```
     *
     * @param <A> Der Action-Typ
     */
    public static final class CustomButton<A extends UiAction> extends ClickableUiElement<A> {
        public CustomButton(ItemStack itemStack, A action) {
            super(itemStack, action);
        }
    }
}
