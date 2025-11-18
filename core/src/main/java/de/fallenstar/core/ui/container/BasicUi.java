package de.fallenstar.core.ui.container;

import de.fallenstar.core.ui.row.BasicUiRow;
import de.fallenstar.core.ui.row.BasicUiRowForControl;
import de.fallenstar.core.ui.row.BasicUiRowForContent;

/**
 * Basis-UI mit 3 Rows (Content + Navigation).
 *
 * **Standard-Layout:**
 * - Row 0 (Slots 0-8): Content-Row
 * - Row 1 (Slots 9-17): Content-Row
 * - Row 2 (Slots 18-26): Control-Row (Navigation)
 *
 * **Type-Safety:**
 * - Rows sind vorinitialisiert und type-safe
 * - UI-Elemente MÜSSEN Actions haben (wenn klickbar)
 * - Compiler erzwingt korrekte Action-Typen
 *
 * **Verwendung:**
 * ```java
 * public class MyUi extends BasicUi {
 *     public MyUi() {
 *         super("Mein UI");
 *
 *         // Content hinzufügen
 *         getContentRow(0).append(myElement1);
 *         getContentRow(1).append(myElement2);
 *
 *         // Navigation setzen
 *         getControlRow().setCloseButton(CloseButton.create(this));
 *     }
 * }
 * ```
 *
 * @author FallenStar
 * @version 2.0
 */
public class BasicUi extends GenericUiSmallChest {

    private static final int CONTENT_ROW_1 = 0;
    private static final int CONTENT_ROW_2 = 1;
    private static final int CONTROL_ROW = 2;

    /**
     * Konstruktor für BasicUi.
     *
     * Initialisiert:
     * - 2 Content-Rows (Row 0 und Row 1)
     * - 1 Control-Row (Row 2)
     *
     * @param title Titel des UI
     */
    public BasicUi(String title) {
        super(title);

        // Initialisiere Content-Rows
        setRow(CONTENT_ROW_1, new BasicUiRowForContent());
        setRow(CONTENT_ROW_2, new BasicUiRowForContent());

        // Initialisiere Control-Row
        setRow(CONTROL_ROW, new BasicUiRowForControl());
    }

    /**
     * Gibt die erste Content-Row zurück (Row 0, Slots 0-8).
     *
     * @return Content-Row 1
     */
    public BasicUiRowForContent getContentRow1() {
        return (BasicUiRowForContent) getRow(CONTENT_ROW_1);
    }

    /**
     * Gibt die zweite Content-Row zurück (Row 1, Slots 9-17).
     *
     * @return Content-Row 2
     */
    public BasicUiRowForContent getContentRow2() {
        return (BasicUiRowForContent) getRow(CONTENT_ROW_2);
    }

    /**
     * Gibt eine Content-Row anhand des Index zurück.
     *
     * @param index Content-Row-Index (0 oder 1)
     * @return Content-Row
     * @throws IllegalArgumentException wenn index nicht 0 oder 1
     */
    public BasicUiRowForContent getContentRow(int index) {
        return switch (index) {
            case 0 -> getContentRow1();
            case 1 -> getContentRow2();
            default -> throw new IllegalArgumentException("Content-Row-Index muss 0 oder 1 sein");
        };
    }

    /**
     * Gibt die Control-Row zurück (Row 2, Slots 18-26).
     *
     * @return Control-Row
     */
    public BasicUiRowForControl getControlRow() {
        return (BasicUiRowForControl) getRow(CONTROL_ROW);
    }
}
