package de.fallenstar.core.ui.row;

import de.fallenstar.core.ui.element.navigation.CloseButton;
import de.fallenstar.core.ui.element.navigation.NavigateLeftButton;
import de.fallenstar.core.ui.element.navigation.NavigateRightButton;

/**
 * Control-Row für Navigation und UI-Steuerung.
 *
 * Verwendet für:
 * - Navigation-Buttons (Links/Rechts)
 * - Close-Button (Schließen)
 * - Filter/Sort-Buttons
 *
 * Standard-Layout für SmallChestUI (Row 2, Slots 18-26):
 * - Slot 0 (18): Navigate Left / First Page
 * - Slot 4 (22): Close Button
 * - Slot 8 (26): Navigate Right / Last Page
 *
 * Verwendung:
 * ```java
 * var controlRow = new BasicUiRowForControl();
 * controlRow.setNavigateLeft(NavigateLeftButton.previous(pageableUi));
 * controlRow.setNavigateRight(NavigateRightButton.next(pageableUi));
 * controlRow.setCloseButton(CloseButton.create(ui));
 * ```
 *
 * @author FallenStar
 * @version 2.0
 */
public final class BasicUiRowForControl extends BasicUiRow {

    // Standard-Positionen für Navigation
    private static final int POSITION_LEFT = 0;   // Linker Rand
    private static final int POSITION_CENTER = 4; // Mitte
    private static final int POSITION_RIGHT = 8;  // Rechter Rand

    /**
     * Konstruktor für Control-Row mit Standard-Größe (9 Slots).
     */
    public BasicUiRowForControl() {
        super(DEFAULT_ROW_SIZE);
    }

    /**
     * Setzt den Navigate-Left Button.
     *
     * Standard-Position: Slot 0 (linker Rand)
     *
     * @param button NavigateLeftButton
     */
    public void setNavigateLeft(NavigateLeftButton button) {
        setElement(POSITION_LEFT, button);
    }

    /**
     * Setzt den Navigate-Right Button.
     *
     * Standard-Position: Slot 8 (rechter Rand)
     *
     * @param button NavigateRightButton
     */
    public void setNavigateRight(NavigateRightButton button) {
        setElement(POSITION_RIGHT, button);
    }

    /**
     * Setzt den Close Button.
     *
     * Standard-Position: Slot 4 (Mitte)
     *
     * @param button CloseButton
     */
    public void setCloseButton(CloseButton button) {
        setElement(POSITION_CENTER, button);
    }
}
