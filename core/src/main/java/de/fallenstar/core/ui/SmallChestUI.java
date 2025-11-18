package de.fallenstar.core.ui;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Small Chest UI - 27 Slots (3 Zeilen).
 *
 * Verwendet für:
 * - Einfache Menüs
 * - Auswahl-Dialoge
 * - Ambassador UI (Botschafts-Grundstücke)
 *
 * Items können als anklickbare Buttons mit Handler-Funktionen
 * konfiguriert werden. Die Handler können auf Provider-Funktionen
 * zugreifen.
 *
 * @author FallenStar
 * @version 1.0
 */
public abstract class SmallChestUI extends BaseUI implements Listener {

    public static final int SIZE = 27; // 3 Zeilen
    protected static final Map<UUID, SmallChestUI> activeUIs = new HashMap<>();

    /**
     * Konstruktor für SmallChestUI.
     *
     * @param title Titel des Chest-UI
     */
    public SmallChestUI(String title) {
        super(title);
    }

    @Override
    public void open(Player player) {
        // Deregistriere alte Listener BEVOR wir einen neuen registrieren
        // (verhindert mehrfache Listener bei rebuild())
        org.bukkit.event.HandlerList.unregisterAll(this);

        // Event-Listener registrieren (benötigt BaseUI.setPlugin() beim Server-Start!)
        if (getPlugin() != null) {
            Bukkit.getPluginManager().registerEvents(this, getPlugin());
        }

        Inventory inventory = Bukkit.createInventory(null, SIZE, title);

        // Items in Inventory laden
        for (Map.Entry<Integer, ItemStack> entry : items.entrySet()) {
            if (entry.getKey() >= 0 && entry.getKey() < SIZE) {
                inventory.setItem(entry.getKey(), entry.getValue());
            }
        }

        // UI für Spieler speichern
        activeUIs.put(player.getUniqueId(), this);

        // Inventory öffnen
        player.openInventory(inventory);
    }

    /**
     * Aktualisiert das Inventory ohne es zu schließen.
     * Verwendet wenn sich Items ändern aber das UI geöffnet bleiben soll.
     *
     * @param player Der Spieler
     */
    public void refresh(Player player) {
        Inventory inventory = player.getOpenInventory().getTopInventory();
        if (inventory.getSize() != SIZE) {
            return; // Falsches Inventory
        }

        // Items aktualisieren
        for (Map.Entry<Integer, ItemStack> entry : items.entrySet()) {
            if (entry.getKey() >= 0 && entry.getKey() < SIZE) {
                inventory.setItem(entry.getKey(), entry.getValue());
            }
        }

        // Spieler-Inventory updaten (für visuelle Aktualisierung)
        player.updateInventory();
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

        SmallChestUI ui = activeUIs.get(player.getUniqueId());
        if (ui == null) {
            return;  // Kein aktives UI für diesen Spieler
        }

        // IMMER canceln - verhindert Item-Bewegung in ALLEN Fällen
        // Auch wenn ui != this, canceln wir trotzdem (verhindert Race Conditions)
        event.setCancelled(true);

        // Nur Click-Handler für DIESES UI ausführen (wenn es das aktive ist)
        if (ui == this) {
            int slot = event.getRawSlot();
            if (slot >= 0 && slot < SIZE) {
                handleClick(player, slot);
            }
        }
        // Clicks außerhalb des UI-Inventars (z.B. Spieler-Inventar) werden auch gecancelt!
    }

    /**
     * Event-Handler für Inventory-Close.
     */
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getPlayer() instanceof Player player) {
            SmallChestUI ui = activeUIs.get(player.getUniqueId());
            if (ui == this) {
                activeUIs.remove(player.getUniqueId());

                // Event-Listener unregistrieren, um Memory-Leaks zu vermeiden
                org.bukkit.event.HandlerList.unregisterAll(this);
            }
        }
    }

    /**
     * Gibt die aktive UI für einen Spieler zurück.
     *
     * @param player Spieler
     * @return Aktive SmallChestUI oder null
     */
    public static SmallChestUI getActiveUI(Player player) {
        return activeUIs.get(player.getUniqueId());
    }
}
