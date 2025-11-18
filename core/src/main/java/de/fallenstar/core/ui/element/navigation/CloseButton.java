package de.fallenstar.core.ui.element.navigation;

import de.fallenstar.core.ui.UiParent;
import de.fallenstar.core.ui.element.ClickableUiElement;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

/**
 * Close Button - Schließt das UI.
 *
 * **Type-Safety:**
 * - Extends ClickableUiElement&lt;CloseUiAction&gt;
 * - Compiler erzwingt: Action MUSS vom Typ CloseUiAction sein
 *
 * Standard-Slot-Position: 22 (Mitte unten, SmallChestUi)
 *
 * Verwendung:
 * ```java
 * var button = CloseButton.create(myUi);
 * ui.setRow(2, 4, button); // Row 2, Slot 4 (= Slot 22)
 * ```
 *
 * @author FallenStar
 * @version 2.0
 */
public final class CloseButton extends ClickableUiElement<CloseUiAction> {

    /**
     * Konstruktor mit benutzerdefinierter Action.
     *
     * @param action CloseUiAction
     */
    public CloseButton(CloseUiAction action) {
        super(createDefaultItem(), action);
    }

    /**
     * Erstellt das Standard-ItemStack für den Button.
     *
     * @return ItemStack (Barrier/Barriere)
     */
    private static ItemStack createDefaultItem() {
        ItemStack item = new ItemStack(Material.BARRIER);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§c§lSchließen");
            meta.setLore(List.of(
                    "§7Klicke um das Menü zu schließen"
            ));
            item.setItemMeta(meta);
        }
        return item;
    }

    /**
     * Factory-Methode für Standard-CloseButton.
     *
     * @param ui Das UI das geschlossen werden soll
     * @return CloseButton
     */
    public static CloseButton create(UiParent ui) {
        return new CloseButton(new CloseUiAction(ui));
    }
}
