package de.fallenstar.core.ui;

import org.bukkit.Material;

import java.util.function.Consumer;

/**
 * Repräsentiert einen klickbaren Button in einer UI.
 *
 * Ein UIButton kann gerendert werden als:
 * - Inventory-Slot mit Item (UI-Modul)
 * - Anklickbarer Chat-Link (Native Fallback)
 *
 * @param id Eindeutige ID des Buttons
 * @param label Anzeigename
 * @param icon Material-Icon (für Inventory-UI)
 * @param action Command der ausgeführt wird beim Klick
 *
 * @author FallenStar
 * @version 1.0
 */
public record UIButton(
        String id,
        String label,
        Material icon,
        String action
) {
    /**
     * Erstellt einen einfachen Button.
     *
     * @param id ID
     * @param label Anzeigename
     * @param action Command
     * @return UIButton
     */
    public static UIButton of(String id, String label, String action) {
        return new UIButton(id, label, Material.STONE, action);
    }

    /**
     * Erstellt einen Button mit Icon.
     *
     * @param id ID
     * @param label Anzeigename
     * @param icon Material-Icon
     * @param action Command
     * @return UIButton
     */
    public static UIButton withIcon(String id, String label, Material icon, String action) {
        return new UIButton(id, label, icon, action);
    }
}
