package de.fallenstar.core.ui.row;

import de.fallenstar.core.ui.element.UiElement;

import java.util.Optional;

/**
 * Interface für Container die UI-Elemente halten können.
 *
 * Container können sein:
 * - Rows (Zeilen in einem Chest-UI, 9 Slots)
 * - Pages (Seiten in einem Pageable UI)
 * - Sections (Bereiche in einem UI)
 *
 * Implementierungen:
 * - BasicUiRow (abstract)
 * - BasicUiRowForControl (Navigation/Control-Zeile)
 * - BasicUiRowForContent (Content-Zeile mit Items)
 *
 * @author FallenStar
 * @version 2.0
 */
public interface UiElementContainer {

    /**
     * Gibt die Anzahl der Slots in diesem Container zurück.
     *
     * @return Anzahl der Slots (z.B. 9 für Chest-Row)
     */
    int getSize();

    /**
     * Setzt ein UI-Element an einer Position.
     *
     * @param position Position (0-basiert)
     * @param element UI-Element
     * @throws IndexOutOfBoundsException wenn position außerhalb des gültigen Bereichs
     */
    void setElement(int position, UiElement element);

    /**
     * Gibt ein UI-Element an einer Position zurück.
     *
     * @param position Position (0-basiert)
     * @return Optional mit UI-Element, oder empty wenn kein Element vorhanden
     */
    Optional<UiElement> getElement(int position);

    /**
     * Entfernt ein UI-Element an einer Position.
     *
     * @param position Position (0-basiert)
     */
    void removeElement(int position);

    /**
     * Entfernt alle UI-Elemente aus diesem Container.
     */
    void clear();

    /**
     * Prüft ob an einer Position ein Element vorhanden ist.
     *
     * @param position Position (0-basiert)
     * @return true wenn Element vorhanden, false sonst
     */
    default boolean hasElement(int position) {
        return getElement(position).isPresent();
    }
}
