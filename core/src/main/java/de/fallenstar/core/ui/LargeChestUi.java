package de.fallenstar.core.ui;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Large Chest UI - 54 Slots (6 Zeilen).
 *
 * Verwendet für:
 * - Große Menüs mit vielen Optionen
 * - Item-Übersichten
 * - Händler-Interfaces (MarketTraderUI)
 *
 * Items können als anklickbare Buttons mit Handler-Funktionen
 * konfiguriert werden. Die Handler können auf Provider-Funktionen
 * zugreifen.
 *
 * @author FallenStar
 * @version 1.0
 */
public abstract class LargeChestUi extends BaseUi implements Listener {

    public static final int SIZE = 54; // 6 Zeilen
    protected static final Map<UUID, LargeChestUi> activeUIs = new HashMap<>();

    /**
     * Das aktuell geöffnete Inventory dieser UI-Instanz.
     * Wird verwendet um zu prüfen ob Clicks zum richtigen Inventory gehören.
     */
    private Inventory currentInventory;

    /**
     * Konstruktor für LargeChestUi.
     *
     * @param title Titel des Chest-UI
     */
    public LargeChestUi(String title) {
        super(title);
    }

    @Override
    public void open(Player player) {
        // Prüfe ob Spieler bereits ein Inventory offen hat (Rebuild-Fall)
        Inventory currentInventory = player.getOpenInventory().getTopInventory();
        boolean isRebuild = activeUIs.get(player.getUniqueId()) == this;

        // Deregistriere alte Listener NUR wenn es KEIN Rebuild ist
        // (beim Rebuild sind die Listener bereits registriert!)
        if (!isRebuild) {
            org.bukkit.event.HandlerList.unregisterAll(this);
        }

        // Event-Listener registrieren (benötigt BaseUi.setPlugin() beim Server-Start!)
        if (!isRebuild && getPlugin() != null) {
            Bukkit.getPluginManager().registerEvents(this, getPlugin());
        }

        Inventory inventory;
        if (isRebuild && currentInventory.getSize() == SIZE) {
            // Rebuild: Update existierendes Inventory statt neues zu erstellen
            inventory = currentInventory;
            inventory.clear(); // Lösche alte Items
        } else {
            // Erstes Öffnen: Erstelle neues Inventory
            inventory = Bukkit.createInventory(null, SIZE, title);
        }

        // Items in Inventory laden
        for (Map.Entry<Integer, ItemStack> entry : items.entrySet()) {
            if (entry.getKey() >= 0 && entry.getKey() < SIZE) {
                inventory.setItem(entry.getKey(), entry.getValue());
            }
        }

        // Speichere das Inventory für diese UI-Instanz
        this.currentInventory = inventory;

        // UI für Spieler speichern
        activeUIs.put(player.getUniqueId(), this);

        // Inventory öffnen (nur wenn nicht bereits offen)
        if (!isRebuild) {
            player.openInventory(inventory);
        }
        // Sonst ist das Inventory bereits offen und wurde nur updated
    }

    @Override
    public void close(Player player) {
        player.closeInventory();
        activeUIs.remove(player.getUniqueId());
    }

    /**
     * Event-Handler für Inventory-Clicks.
     *
     * WICHTIG: Cancelt ALLE Clicks wenn ein UI aktiv ist!
     * Dies verhindert das Bewegen von Items auch bei nicht-klickbaren Slots.
     */
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        // WICHTIG: Prüfe ob der Click zum Inventory DIESER UI-Instanz gehört!
        // Problem: Mehrere UI-Instanzen haben Event-Handler registriert.
        // Lösung: Nur reagieren wenn event.getInventory() == this.currentInventory
        if (event.getInventory() != this.currentInventory) {
            return;  // Nicht mein Inventory - ignorieren!
        }

        // IMMER canceln - verhindert Item-Bewegung
        event.setCancelled(true);

        // Click-Handler ausführen
        int slot = event.getRawSlot();
        if (slot >= 0 && slot < SIZE) {
            handleClick(player, slot);
        }
        // Clicks außerhalb des UI-Inventars (z.B. Spieler-Inventar) werden auch gecancelt!
    }

    /**
     * Event-Handler für Inventory-Close.
     */
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        // Nur reagieren wenn es MEIN Inventory ist
        if (event.getInventory() != this.currentInventory) {
            return;
        }

        if (event.getPlayer() instanceof Player player) {
            activeUIs.remove(player.getUniqueId());

            // Inventory-Referenz löschen (Memory-Leak vermeiden)
            this.currentInventory = null;

            // Event-Listener unregistrieren, um Memory-Leaks zu vermeiden
            org.bukkit.event.HandlerList.unregisterAll(this);
        }
    }

    /**
     * Gibt die aktive UI für einen Spieler zurück.
     *
     * @param player Spieler
     * @return Aktive LargeChestUi oder null
     */
    public static LargeChestUi getActiveUI(Player player) {
        return activeUIs.get(player.getUniqueId());
    }
}
