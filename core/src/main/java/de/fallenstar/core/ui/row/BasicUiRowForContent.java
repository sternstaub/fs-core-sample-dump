package de.fallenstar.core.ui.row;

import de.fallenstar.core.ui.element.UiElement;

import java.util.List;

/**
 * Content-Row für Inhalts-Elemente.
 *
 * Verwendet für:
 * - Item-Listen (z.B. Shop-Items)
 * - Player-Listen (z.B. Online-Spieler)
 * - Plot-Listen (z.B. Grundstücke)
 * - Any dynamic content
 *
 * Unterstützt:
 * - Append (fügt Element hinzu)
 * - Fill (füllt Row mit Elementen)
 * - Pagination (verteilt Elemente über mehrere Rows/Pages)
 *
 * Verwendung:
 * ```java
 * var contentRow = new BasicUiRowForContent();
 * contentRow.append(shopItem1);
 * contentRow.append(shopItem2);
 * // ...bis Row voll ist
 * ```
 *
 * @author FallenStar
 * @version 2.0
 */
public final class BasicUiRowForContent extends BasicUiRow {

    /**
     * Konstruktor für Content-Row mit Standard-Größe (9 Slots).
     */
    public BasicUiRowForContent() {
        super(DEFAULT_ROW_SIZE);
    }

    /**
     * Fügt ein Element hinzu (append).
     *
     * Sucht automatisch den nächsten freien Slot und fügt das Element ein.
     * Wenn die Row voll ist, wird das Element NICHT hinzugefügt.
     *
     * @param element UI-Element
     * @return true wenn Element hinzugefügt wurde, false wenn Row voll
     */
    public boolean append(UiElement element) {
        for (int i = 0; i < getSize(); i++) {
            if (!hasElement(i)) {
                setElement(i, element);
                return true;
            }
        }
        return false; // Row ist voll
    }

    /**
     * Füllt die Row mit Elementen.
     *
     * Fügt so viele Elemente wie möglich hinzu.
     * Wenn die Row voll ist, werden restliche Elemente ignoriert.
     *
     * @param elements Liste von UI-Elementen
     * @return Anzahl der hinzugefügten Elemente
     */
    public int fill(List<UiElement> elements) {
        int added = 0;
        for (UiElement element : elements) {
            if (!append(element)) {
                break; // Row ist voll
            }
            added++;
        }
        return added;
    }

    /**
     * Prüft ob die Row voll ist.
     *
     * @return true wenn alle Slots belegt sind
     */
    public boolean isFull() {
        return getElementCount() >= getSize();
    }

    /**
     * Gibt die Anzahl der freien Slots zurück.
     *
     * @return Anzahl der freien Slots
     */
    public int getFreeSlots() {
        return getSize() - getElementCount();
    }
}
