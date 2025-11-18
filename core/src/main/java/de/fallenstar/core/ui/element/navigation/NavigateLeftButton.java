package de.fallenstar.core.ui.element.navigation;

import de.fallenstar.core.ui.element.ClickableUiElement;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

/**
 * Navigate-Left Button - Vorherige Seite.
 *
 * **Type-Safety:**
 * - Extends ClickableUiElement&lt;PageNavigationAction&gt;
 * - Compiler erzwingt: Action MUSS vom Typ PageNavigationAction sein
 *
 * Standard-Slot-Position: 18 (untere linke Ecke, SmallChestUI)
 *
 * Verwendung:
 * ```java
 * var action = new PageNavigationAction(
 *     PageNavigationAction.Direction.PREVIOUS,
 *     pageableUi
 * );
 * var button = new NavigateLeftButton(action);
 * ui.setRow(2, 0, button); // Row 2, Slot 0 (= Slot 18)
 * ```
 *
 * @author FallenStar
 * @version 2.0
 */
public final class NavigateLeftButton extends ClickableUiElement<PageNavigationAction> {

    /**
     * Konstruktor mit benutzerdefinierter Action.
     *
     * @param action PageNavigationAction (PREVIOUS oder FIRST)
     */
    public NavigateLeftButton(PageNavigationAction action) {
        super(createDefaultItem(), action);
    }

    /**
     * Erstellt das Standard-ItemStack für den Button.
     *
     * @return ItemStack (Pfeil nach links)
     */
    private static ItemStack createDefaultItem() {
        ItemStack item = new ItemStack(Material.ARROW);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§6§l◀ Vorherige Seite");
            meta.setLore(List.of(
                    "§7Klicke um zur vorherigen Seite zu gehen"
            ));
            item.setItemMeta(meta);
        }
        return item;
    }

    /**
     * Factory-Methode für Standard-PREVIOUS Action.
     *
     * @param navigable Das navigierbare UI
     * @return NavigateLeftButton mit PREVIOUS Action
     */
    public static NavigateLeftButton previous(PageNavigationAction.PageNavigable navigable) {
        return new NavigateLeftButton(
                new PageNavigationAction(PageNavigationAction.Direction.PREVIOUS, navigable)
        );
    }

    /**
     * Factory-Methode für FIRST Action.
     *
     * @param navigable Das navigierbare UI
     * @return NavigateLeftButton mit FIRST Action
     */
    public static NavigateLeftButton first(PageNavigationAction.PageNavigable navigable) {
        var item = new ItemStack(Material.ARROW);
        var meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§6§l⏮ Erste Seite");
            meta.setLore(List.of("§7Klicke um zur ersten Seite zu gehen"));
            item.setItemMeta(meta);
        }

        return new NavigateLeftButton(
                new PageNavigationAction(PageNavigationAction.Direction.FIRST, navigable)
        );
    }
}
