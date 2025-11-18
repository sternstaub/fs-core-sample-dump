package de.fallenstar.core.ui.event;

/**
 * Result eines UI-Clicks.
 *
 * Definiert was nach einem Click auf ein UI-Element passiert.
 *
 * **Verwendung:**
 * - KEEP_OPEN: UI bleibt geöffnet (Standard)
 * - CLOSE_UI: UI wird geschlossen
 * - REFRESH_UI: UI wird neu aufgebaut (für dynamische Inhalte)
 * - OPEN_NEW_UI: Ein neues UI wird geöffnet (altes schließt)
 *
 * Beispiel:
 * ```java
 * var button = new ClickableUiElement<>(item, new MyAction() {
 *     @Override
 *     public void execute(Player player) {
 *         // Action ausführen
 *         // Result wird in UiClickEvent gesetzt
 *     }
 * });
 * ```
 *
 * @author FallenStar
 * @version 2.0
 */
public enum UiClickResult {

    /**
     * UI bleibt geöffnet (Standard).
     */
    KEEP_OPEN,

    /**
     * UI wird geschlossen.
     *
     * Verwendung: Close-Button, Zurück-Button
     */
    CLOSE_UI,

    /**
     * UI wird neu aufgebaut (refresh).
     *
     * Verwendung: Nach Änderungen an Items/Preisen/etc.
     */
    REFRESH_UI,

    /**
     * Ein neues UI wird geöffnet (altes schließt).
     *
     * Verwendung: Navigation zu anderem UI (z.B. Sub-Menü)
     */
    OPEN_NEW_UI
}
