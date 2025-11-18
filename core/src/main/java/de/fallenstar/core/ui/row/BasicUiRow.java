package de.fallenstar.core.ui.row;

import de.fallenstar.core.ui.element.UiElement;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Abstrakte Basis-Klasse für UI-Rows.
 *
 * Eine Row repräsentiert eine Zeile in einem Chest-UI.
 * Standard-Chest-Row hat 9 Slots (0-8).
 *
 * **Type-Safety:**
 * - size ist final und fix definiert
 * - Slots sind indiziert 0-basiert
 * - Out-of-bounds Zugriffe werfen IndexOutOfBoundsException
 *
 * Implementierungen:
 * - BasicUiRowForControl (Navigation-Zeile)
 * - BasicUiRowForContent (Content-Zeile)
 *
 * @author FallenStar
 * @version 2.0
 */
public abstract class BasicUiRow implements UiElementContainer {

    /**
     * Standard-Größe einer Chest-Row (9 Slots).
     */
    public static final int DEFAULT_ROW_SIZE = 9;

    private final int size;
    private final Map<Integer, UiElement> elements;

    /**
     * Konstruktor mit Standard-Größe (9 Slots).
     */
    protected BasicUiRow() {
        this(DEFAULT_ROW_SIZE);
    }

    /**
     * Konstruktor mit benutzerdefinierter Größe.
     *
     * @param size Anzahl der Slots in dieser Row
     */
    protected BasicUiRow(int size) {
        if (size <= 0) {
            throw new IllegalArgumentException("Row size muss > 0 sein");
        }
        this.size = size;
        this.elements = new HashMap<>(size);
    }

    @Override
    public int getSize() {
        return size;
    }

    @Override
    public void setElement(int position, UiElement element) {
        validatePosition(position);
        elements.put(position, element);
    }

    @Override
    public Optional<UiElement> getElement(int position) {
        validatePosition(position);
        return Optional.ofNullable(elements.get(position));
    }

    @Override
    public void removeElement(int position) {
        validatePosition(position);
        elements.remove(position);
    }

    @Override
    public void clear() {
        elements.clear();
    }

    /**
     * Validiert eine Slot-Position.
     *
     * @param position Position (0-basiert)
     * @throws IndexOutOfBoundsException wenn position außerhalb des gültigen Bereichs
     */
    private void validatePosition(int position) {
        if (position < 0 || position >= size) {
            throw new IndexOutOfBoundsException(
                    "Position " + position + " außerhalb des gültigen Bereichs [0, " + (size - 1) + "]"
            );
        }
    }

    /**
     * Gibt alle Elemente zurück.
     *
     * @return Map von Position zu UiElement
     */
    public Map<Integer, UiElement> getElements() {
        return new HashMap<>(elements);
    }

    /**
     * Gibt die Anzahl der gesetzten Elemente zurück.
     *
     * @return Anzahl der Elemente
     */
    public int getElementCount() {
        return elements.size();
    }

    /**
     * Prüft ob die Row leer ist.
     *
     * @return true wenn keine Elemente vorhanden
     */
    public boolean isEmpty() {
        return elements.isEmpty();
    }
}
