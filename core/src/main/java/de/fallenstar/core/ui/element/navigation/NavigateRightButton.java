package de.fallenstar.core.ui.element.navigation;

import de.fallenstar.core.ui.element.ClickableUiElement;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

/**
 * Navigate-Right Button - Nächste Seite.
 *
 * **Type-Safety:**
 * - Extends ClickableUiElement&lt;PageNavigationAction&gt;
 * - Compiler erzwingt: Action MUSS vom Typ PageNavigationAction sein
 *
 * Standard-Slot-Position: 26 (untere rechte Ecke, SmallChestUI)
 *
 * Verwendung:
 * ```java
 * var action = new PageNavigationAction(
 *     PageNavigationAction.Direction.NEXT,
 *     pageableUi
 * );
 * var button = new NavigateRightButton(action);
 * ui.setRow(2, 8, button); // Row 2, Slot 8 (= Slot 26)
 * ```
 *
 * @author FallenStar
 * @version 2.0
 */
public final class NavigateRightButton extends ClickableUiElement<PageNavigationAction> {

    /**
     * Konstruktor mit benutzerdefinierter Action.
     *
     * @param action PageNavigationAction (NEXT oder LAST)
     */
    public NavigateRightButton(PageNavigationAction action) {
        super(createDefaultItem(), action);
    }

    /**
     * Erstellt das Standard-ItemStack für den Button.
     *
     * @return ItemStack (Pfeil nach rechts)
     */
    private static ItemStack createDefaultItem() {
        ItemStack item = new ItemStack(Material.ARROW);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§6§lNächste Seite ▶");
            meta.setLore(List.of(
                    "§7Klicke um zur nächsten Seite zu gehen"
            ));
            item.setItemMeta(meta);
        }
        return item;
    }

    /**
     * Factory-Methode für Standard-NEXT Action.
     *
     * @param navigable Das navigierbare UI
     * @return NavigateRightButton mit NEXT Action
     */
    public static NavigateRightButton next(PageNavigationAction.PageNavigable navigable) {
        return new NavigateRightButton(
                new PageNavigationAction(PageNavigationAction.Direction.NEXT, navigable)
        );
    }

    /**
     * Factory-Methode für LAST Action.
     *
     * @param navigable Das navigierbare UI
     * @return NavigateRightButton mit LAST Action
     */
    public static NavigateRightButton last(PageNavigationAction.PageNavigable navigable) {
        var item = new ItemStack(Material.ARROW);
        var meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§6§lLetzte Seite ⏭");
            meta.setLore(List.of("§7Klicke um zur letzten Seite zu gehen"));
            item.setItemMeta(meta);
        }

        return new NavigateRightButton(
                new PageNavigationAction(PageNavigationAction.Direction.LAST, navigable)
        );
    }
}
