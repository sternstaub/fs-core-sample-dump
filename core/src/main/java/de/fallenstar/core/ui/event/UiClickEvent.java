package de.fallenstar.core.ui.event;

import de.fallenstar.core.ui.UiParent;
import de.fallenstar.core.ui.element.UiAction;
import de.fallenstar.core.ui.element.UiElement;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Event das gefeuert wird wenn ein Spieler auf ein UI-Element klickt.
 *
 * **Type-Safety:**
 * - Element ist typisiert (UiElement)
 * - Action ist typisiert (UiAction)
 * - Result definiert was nach dem Click passiert
 *
 * **Verwendung:**
 * - Externe Listener können auf UI-Clicks reagieren
 * - Results steuern ob UI geöffnet bleibt oder geschlossen wird
 * - Kann gecancelt werden (verhindert Action-Ausführung)
 *
 * Beispiel:
 * ```java
 * @EventHandler
 * public void onUiClick(UiClickEvent event) {
 *     if (event.getAction() instanceof MyCustomAction action) {
 *         // Custom Handling
 *         event.setResult(UiClickResult.CLOSE_UI);
 *     }
 * }
 * ```
 *
 * @author FallenStar
 * @version 2.0
 */
public class UiClickEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Player player;
    private final UiParent ui;
    private final UiElement element;
    private final UiAction action;
    private final int slot;

    private boolean cancelled;
    private UiClickResult result;

    /**
     * Konstruktor für UiClickEvent.
     *
     * @param player Spieler der geclickt hat
     * @param ui UI in dem geclickt wurde
     * @param element UI-Element das geclickt wurde
     * @param action Action die ausgeführt wird
     * @param slot Slot-Position (0-basiert)
     */
    public UiClickEvent(Player player, UiParent ui, UiElement element, UiAction action, int slot) {
        this.player = player;
        this.ui = ui;
        this.element = element;
        this.action = action;
        this.slot = slot;
        this.cancelled = false;
        this.result = UiClickResult.KEEP_OPEN;
    }

    /**
     * Gibt den Spieler zurück.
     *
     * @return Spieler
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Gibt das UI zurück.
     *
     * @return UI
     */
    public UiParent getUi() {
        return ui;
    }

    /**
     * Gibt das geklickte UI-Element zurück.
     *
     * @return UI-Element
     */
    public UiElement getElement() {
        return element;
    }

    /**
     * Gibt die Action zurück.
     *
     * @return Action
     */
    public UiAction getAction() {
        return action;
    }

    /**
     * Gibt die Slot-Position zurück.
     *
     * @return Slot (0-basiert)
     */
    public int getSlot() {
        return slot;
    }

    /**
     * Gibt an ob das Event gecancelt wurde.
     *
     * @return true wenn gecancelt
     */
    public boolean isCancelled() {
        return cancelled;
    }

    /**
     * Setzt den Cancel-Status.
     *
     * Wenn gecancelt, wird die Action NICHT ausgeführt.
     *
     * @param cancelled Cancel-Status
     */
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    /**
     * Gibt das Click-Result zurück.
     *
     * @return Click-Result
     */
    public UiClickResult getResult() {
        return result;
    }

    /**
     * Setzt das Click-Result.
     *
     * @param result Click-Result
     */
    public void setResult(UiClickResult result) {
        this.result = result;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
