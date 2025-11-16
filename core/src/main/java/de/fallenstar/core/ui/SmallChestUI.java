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

    @Override
    public void close(Player player) {
        player.closeInventory();
        activeUIs.remove(player.getUniqueId());
    }

    /**
     * Event-Handler für Inventory-Clicks.
     */
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        SmallChestUI ui = activeUIs.get(player.getUniqueId());
        if (ui == null || ui != this) {
            return;
        }

        event.setCancelled(true); // Verhindere Item-Bewegung

        int slot = event.getRawSlot();
        if (slot >= 0 && slot < SIZE) {
            handleClick(player, slot);
        }
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
