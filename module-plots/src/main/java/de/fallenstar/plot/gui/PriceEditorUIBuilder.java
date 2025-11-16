package de.fallenstar.plot.gui;

import de.fallenstar.core.ui.UIButton;
import de.fallenstar.core.ui.UIMenu;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Builder für Preis-Editor-UIs.
 *
 * Erstellt UIMenus für die Handelspreis-Festlegung auf Handelsgilde-Grundstücken.
 *
 * Layout (Text-basiert via NativeTextUIProvider):
 * - Titel: "Preis festlegen - [Item]"
 * - Aktueller Preis: [X Sterne]
 * - Buttons: +100 / +10 / +1 / -1 / -10 / -100 (Gold/Silber/Bronze)
 * - Bestätigen / Abbrechen
 *
 * @author FallenStar
 * @version 1.0
 */
public class PriceEditorUIBuilder {

    /**
     * Erstellt ein Preis-Editor-UI für einen Kontext.
     *
     * @param context PriceEditorContext
     * @param sessionId Eindeutige Session-ID (für Command-Routing)
     * @return UIMenu
     */
    public static UIMenu buildPriceEditorUI(PriceEditorContext context, UUID sessionId) {
        String itemName = context.getItemDisplayName();
        BigDecimal currentPrice = context.getCurrentPrice();

        UIMenu menu = new UIMenu(
            "Preis festlegen - " + itemName,
            "Aktueller Preis: " + formatPrice(currentPrice) + " Sterne"
        );

        // Preis erhöhen
        menu.addButton(UIButton.withIcon(
            "increase_gold",
            "+100 Sterne (Gold)",
            Material.GOLD_INGOT,
            "/plot price adjust " + sessionId + " +100"
        ));

        menu.addButton(UIButton.withIcon(
            "increase_silver",
            "+10 Sterne (Silber)",
            Material.IRON_INGOT,
            "/plot price adjust " + sessionId + " +10"
        ));

        menu.addButton(UIButton.withIcon(
            "increase_bronze",
            "+1 Stern (Bronze)",
            Material.COPPER_INGOT,
            "/plot price adjust " + sessionId + " +1"
        ));

        // Preis verringern
        menu.addButton(UIButton.withIcon(
            "decrease_bronze",
            "-1 Stern (Bronze)",
            Material.COPPER_INGOT,
            "/plot price adjust " + sessionId + " -1"
        ));

        menu.addButton(UIButton.withIcon(
            "decrease_silver",
            "-10 Sterne (Silber)",
            Material.IRON_INGOT,
            "/plot price adjust " + sessionId + " -10"
        ));

        menu.addButton(UIButton.withIcon(
            "decrease_gold",
            "-100 Sterne (Gold)",
            Material.GOLD_INGOT,
            "/plot price adjust " + sessionId + " -100"
        ));

        // Bestätigen/Abbrechen
        menu.addButton(UIButton.withIcon(
            "confirm",
            "Bestätigen",
            Material.LIME_WOOL,
            "/plot price confirm " + sessionId
        ));

        menu.addButton(UIButton.withIcon(
            "cancel",
            "Abbrechen",
            Material.RED_WOOL,
            "/plot price cancel " + sessionId
        ));

        return menu;
    }

    /**
     * Formatiert einen Preis für die Anzeige.
     *
     * @param price Preis
     * @return Formatierter String
     */
    private static String formatPrice(BigDecimal price) {
        // Entferne unnötige Nachkommastellen
        return price.stripTrailingZeros().toPlainString();
    }
}
