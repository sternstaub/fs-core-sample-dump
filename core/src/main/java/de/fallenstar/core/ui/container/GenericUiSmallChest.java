package de.fallenstar.core.ui.container;

import de.fallenstar.core.ui.SmallChestUi;
import de.fallenstar.core.ui.UiParent;
import de.fallenstar.core.ui.element.ClickableUiElement;
import de.fallenstar.core.ui.element.UiElement;
import de.fallenstar.core.ui.row.BasicUiRow;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Generisches SmallChest-UI mit Row-basiertem Layout.
 *
 * **Architektur:**
 * - 3 Rows mit je 9 Slots (insgesamt 27 Slots)
 * - Row 0: Slots 0-8 (erste Zeile)
 * - Row 1: Slots 9-17 (zweite Zeile)
 * - Row 2: Slots 18-26 (dritte Zeile, oft für Navigation)
 *
 * **Type-Safety:**
 * - Rows enthalten UiElements (nicht rohe ItemStacks)
 * - ClickableUiElement&lt;T&gt; erzwingt Action-Typen
 * - Compiler verhindert Actions ohne UI-Elemente
 *
 * **Erweiterungen:**
 * - BasicUi (einfaches 3-Row UI)
 * - PageableBasicUi (UI mit Pagination)
 * - BasicGsUi (Grundstücks-UI)
 *
 * @author FallenStar
 * @version 2.0
 */
public abstract class GenericUiSmallChest extends SmallChestUi implements UiParent {

    /**
     * Anzahl der Rows in einem SmallChest (3 Zeilen).
     */
    public static final int ROW_COUNT = 3;

    /**
     * Die 3 Rows des UI.
     */
    protected final List<BasicUiRow> rows;

    /**
     * Konstruktor für GenericUiSmallChest.
     *
     * @param title Titel des UI
     */
    protected GenericUiSmallChest(String title) {
        super(title);
        this.rows = new ArrayList<>(ROW_COUNT);

        // Initialisiere 3 leere Rows (werden von Subklassen befüllt)
        for (int i = 0; i < ROW_COUNT; i++) {
            rows.add(null); // Platzhalter, Subklassen setzen konkrete Rows
        }
    }

    /**
     * Setzt eine Row an einer Position.
     *
     * @param rowIndex Row-Index (0-2)
     * @param row BasicUiRow
     * @throws IndexOutOfBoundsException wenn rowIndex außerhalb [0, 2]
     */
    protected void setRow(int rowIndex, BasicUiRow row) {
        validateRowIndex(rowIndex);
        rows.set(rowIndex, row);
    }

    /**
     * Gibt eine Row zurück.
     *
     * @param rowIndex Row-Index (0-2)
     * @return BasicUiRow oder null wenn nicht gesetzt
     */
    protected BasicUiRow getRow(int rowIndex) {
        validateRowIndex(rowIndex);
        return rows.get(rowIndex);
    }

    /**
     * Setzt ein UI-Element an einer absoluten Slot-Position.
     *
     * **Konvertierung:**
     * - Slot 0-8 → Row 0, Position 0-8
     * - Slot 9-17 → Row 1, Position 0-8
     * - Slot 18-26 → Row 2, Position 0-8
     *
     * @param slot Absolute Slot-Position (0-26)
     * @param element UI-Element
     */
    public void setElement(int slot, UiElement element) {
        int rowIndex = slot / BasicUiRow.DEFAULT_ROW_SIZE;
        int position = slot % BasicUiRow.DEFAULT_ROW_SIZE;

        BasicUiRow row = getRow(rowIndex);
        if (row == null) {
            throw new IllegalStateException("Row " + rowIndex + " ist nicht initialisiert");
        }

        row.setElement(position, element);
    }

    /**
     * Baut das UI auf - konvertiert Rows → BaseUi Items.
     *
     * MUSS vor open() aufgerufen werden!
     */
    protected void build() {
        clearItems(); // Lösche alte Items

        // Konvertiere alle Rows zu BaseUi Items
        for (int rowIndex = 0; rowIndex < ROW_COUNT; rowIndex++) {
            BasicUiRow row = rows.get(rowIndex);
            if (row == null) {
                continue; // Skip nicht initialisierte Rows
            }

            // Konvertiere Row-Elemente zu Inventory-Items
            final int currentRow = rowIndex; // Final für Lambda
            for (int position = 0; position < row.getSize(); position++) {
                final int currentPos = position; // Final für Lambda
                row.getElement(currentPos).ifPresent(uiElement -> {
                    int slot = currentRow * BasicUiRow.DEFAULT_ROW_SIZE + currentPos;

                    // Setze Item in BaseUi
                    setItem(slot, uiElement.getItemStack());

                    // Wenn klickbar: Setze Click-Handler
                    if (uiElement instanceof ClickableUiElement<?> clickable) {
                        setItem(slot, uiElement.getItemStack(), player -> {
                            clickable.getAction().execute(player);
                        });
                    }
                });
            }
        }
    }

    /**
     * Öffnet das UI für einen Spieler.
     *
     * WICHTIG: Ruft automatisch build() auf!
     */
    @Override
    public void open(Player player) {
        build(); // Baue UI vor dem Öffnen
        super.open(player);
    }

    /**
     * Validiert einen Row-Index.
     *
     * @param rowIndex Row-Index (0-2)
     * @throws IndexOutOfBoundsException wenn rowIndex außerhalb [0, 2]
     */
    private void validateRowIndex(int rowIndex) {
        if (rowIndex < 0 || rowIndex >= ROW_COUNT) {
            throw new IndexOutOfBoundsException(
                    "Row-Index " + rowIndex + " außerhalb des gültigen Bereichs [0, " + (ROW_COUNT - 1) + "]"
            );
        }
    }
}
