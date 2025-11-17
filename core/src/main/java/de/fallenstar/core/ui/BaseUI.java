package de.fallenstar.core.ui;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Abstrakte Basis-Klasse für alle UI-Typen.
 *
 * Bietet grundlegende Funktionalität für:
 * - Click-Handler mit ProviderFunction-Integration
 * - Item-Button-Verwaltung
 * - Spieler-spezifische UI-Instanzen
 * - Event-Registrierung (für Listener-Subklassen)
 *
 * Implementierungen:
 * - SmallChestUI (27 Slots, 3 Zeilen)
 * - LargeChestUI (54 Slots, 6 Zeilen)
 * - SignUI (4 Zeilen Text-Input)
 * - AnvilUI (Text-Input mit Rename)
 * - BookUI (Multi-Page Output)
 *
 * @author FallenStar
 * @version 1.0
 */
public abstract class BaseUI {

    protected String title;
    protected final Map<Integer, ItemStack> items;
    protected final Map<Integer, Consumer<Player>> clickHandlers;

    /**
     * Statisches Plugin-Feld für Event-Registrierung.
     * Wird von FallenStarCore beim Start gesetzt.
     */
    private static Plugin pluginInstance;

    /**
     * Konstruktor für BaseUI.
     *
     * @param title Titel des UI
     */
    protected BaseUI(String title) {
        this.title = title;
        this.items = new HashMap<>();
        this.clickHandlers = new HashMap<>();
    }

    /**
     * Setzt die Plugin-Instanz für alle UIs.
     *
     * MUSS von FallenStarCore beim Start aufgerufen werden!
     *
     * @param plugin Die Plugin-Instanz
     */
    public static void setPlugin(Plugin plugin) {
        pluginInstance = plugin;
    }

    /**
     * Gibt die Plugin-Instanz zurück.
     *
     * @return Die Plugin-Instanz oder null wenn nicht gesetzt
     */
    protected static Plugin getPlugin() {
        return pluginInstance;
    }

    /**
     * Setzt ein Item an einer bestimmten Slot-Position.
     *
     * @param slot Slot-Position (0-basiert)
     * @param item ItemStack für den Button
     */
    public void setItem(int slot, ItemStack item) {
        items.put(slot, item);
    }

    /**
     * Setzt ein Item mit Click-Handler an einer Slot-Position.
     *
     * @param slot Slot-Position (0-basiert)
     * @param item ItemStack für den Button
     * @param clickHandler Handler-Funktion die beim Click ausgeführt wird
     */
    public void setItem(int slot, ItemStack item, Consumer<Player> clickHandler) {
        items.put(slot, item);
        clickHandlers.put(slot, clickHandler);
    }

    /**
     * Entfernt ein Item von einer Slot-Position.
     *
     * @param slot Slot-Position (0-basiert)
     */
    public void removeItem(int slot) {
        items.remove(slot);
        clickHandlers.remove(slot);
    }

    /**
     * Entfernt alle Items und Click-Handler.
     */
    public void clearItems() {
        items.clear();
        clickHandlers.clear();
    }

    /**
     * Setzt den Titel des UI (für dynamische UIs).
     *
     * @param title Neuer Titel
     */
    protected void setTitle(String title) {
        this.title = title;
    }

    /**
     * Öffnet das UI für einen Spieler.
     *
     * @param player Spieler für den das UI geöffnet wird
     */
    public abstract void open(Player player);

    /**
     * Schließt das UI für einen Spieler.
     *
     * @param player Spieler für den das UI geschlossen wird
     */
    public abstract void close(Player player);

    /**
     * Behandelt einen Click auf einen Slot.
     *
     * Wenn ein Click-Handler für den Slot existiert, wird dieser ausgeführt.
     * Wenn kein Handler existiert, aber ein Item vorhanden ist,
     * wird ein "Funktion nicht verfügbar" Sound gespielt.
     *
     * @param player Spieler der geclickt hat
     * @param slot Geclickter Slot
     */
    protected void handleClick(Player player, int slot) {
        Consumer<Player> handler = clickHandlers.get(slot);
        if (handler != null) {
            // Handler vorhanden - ausführen
            handler.accept(player);
        } else if (items.containsKey(slot)) {
            // Item vorhanden, aber kein Handler - Sound abspielen
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
        }
    }

    /**
     * Gibt den Titel des UI zurück.
     *
     * @return UI-Titel
     */
    public String getTitle() {
        return title;
    }

    /**
     * Gibt alle Items zurück.
     *
     * @return Map von Slot zu ItemStack
     */
    public Map<Integer, ItemStack> getItems() {
        return new HashMap<>(items);
    }
}
