package de.fallenstar.core.ui;

import java.util.ArrayList;
import java.util.List;

/**
 * Repräsentiert ein UI-Menü mit Titel und Buttons.
 *
 * Ein UIMenu kann gerendert werden als:
 * - Chest-Inventory mit Items (UI-Modul)
 * - Chat-Nachricht mit anklickbaren Links (Native Fallback)
 *
 * @author FallenStar
 * @version 1.0
 */
public class UIMenu {

    private final String title;
    private final List<UIButton> buttons;
    private final String context; // Optionaler Kontext (z.B. Plot-ID)

    /**
     * Erstellt ein neues UIMenu.
     *
     * @param title Titel des Menüs
     */
    public UIMenu(String title) {
        this(title, null);
    }

    /**
     * Erstellt ein neues UIMenu mit Kontext.
     *
     * @param title Titel
     * @param context Kontext (z.B. Plot-ID, Spieler-UUID)
     */
    public UIMenu(String title, String context) {
        this.title = title;
        this.context = context;
        this.buttons = new ArrayList<>();
    }

    /**
     * Fügt einen Button hinzu.
     *
     * @param button UIButton
     * @return this (für Chaining)
     */
    public UIMenu addButton(UIButton button) {
        buttons.add(button);
        return this;
    }

    /**
     * Fügt mehrere Buttons hinzu.
     *
     * @param buttons Buttons
     * @return this (für Chaining)
     */
    public UIMenu addButtons(UIButton... buttons) {
        this.buttons.addAll(List.of(buttons));
        return this;
    }

    /**
     * Gibt den Titel zurück.
     *
     * @return Titel
     */
    public String getTitle() {
        return title;
    }

    /**
     * Gibt alle Buttons zurück.
     *
     * @return Liste von UIButtons
     */
    public List<UIButton> getButtons() {
        return new ArrayList<>(buttons);
    }

    /**
     * Gibt den Kontext zurück.
     *
     * @return Kontext oder null
     */
    public String getContext() {
        return context;
    }

    /**
     * Prüft ob das Menü Buttons hat.
     *
     * @return true wenn Buttons vorhanden
     */
    public boolean hasButtons() {
        return !buttons.isEmpty();
    }

    /**
     * Gibt die Anzahl der Buttons zurück.
     *
     * @return Anzahl
     */
    public int getButtonCount() {
        return buttons.size();
    }
}
