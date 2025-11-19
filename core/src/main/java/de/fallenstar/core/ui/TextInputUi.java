package de.fallenstar.core.ui;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Text-Input UI basierend auf Anvil-Rename-Mechanik.
 *
 * **Konzept:**
 * - Öffnet virtuelles Anvil-Inventory
 * - Spieler gibt Text ein via Item-Rename
 * - Text wird an Handler-Funktion übergeben
 *
 * **Verwendung:**
 * <pre>
 * TextInputUi ui = new TextInputUi(
 *     "Gib einen Namen ein",
 *     "Standard-Name",
 *     text -> {
 *         player.sendMessage("Eingabe: " + text);
 *     }
 * );
 * ui.open(player);
 * </pre>
 *
 * **Features:**
 * - Placeholder-Text im Input-Item
 * - Bestätigungs-Button (grünes Glas)
 * - Abbrechen-Button (rotes Glas)
 * - Automatisches Schließen nach Eingabe
 *
 * **Slots:**
 * - Slot 0: Input-Item (Papier mit Placeholder-Text)
 * - Slot 1: (leer - Anvil kombiniert hier)
 * - Slot 2: Bestätigung (grünes Glas) oder Abbruch (rotes Glas)
 *
 * @author FallenStar
 * @version 1.0
 */
public class TextInputUi extends BaseUi implements Listener {

    private static final Map<UUID, TextInputUi> activeUIs = new HashMap<>();

    private final String placeholder;
    private final Consumer<String> inputHandler;
    private final Consumer<Player> cancelHandler;
    private boolean confirmed = false;

    /**
     * Erstellt ein Text-Input UI.
     *
     * @param title Titel des Anvil-Fensters
     * @param placeholder Platzhalter-Text im Input-Item
     * @param inputHandler Handler für erfolgreiche Eingabe
     */
    public TextInputUi(String title, String placeholder, Consumer<String> inputHandler) {
        this(title, placeholder, inputHandler, null);
    }

    /**
     * Erstellt ein Text-Input UI mit Cancel-Handler.
     *
     * @param title Titel des Anvil-Fensters
     * @param placeholder Platzhalter-Text im Input-Item
     * @param inputHandler Handler für erfolgreiche Eingabe
     * @param cancelHandler Handler für Abbruch (kann null sein)
     */
    public TextInputUi(String title, String placeholder, Consumer<String> inputHandler, Consumer<Player> cancelHandler) {
        super(title);
        this.placeholder = placeholder;
        this.inputHandler = inputHandler;
        this.cancelHandler = cancelHandler;
    }

    @Override
    public void open(Player player) {
        // Deregistriere alte Listener
        org.bukkit.event.HandlerList.unregisterAll(this);

        // Event-Listener registrieren
        if (getPlugin() != null) {
            Bukkit.getPluginManager().registerEvents(this, getPlugin());
        }

        // Erstelle Anvil-Inventory
        Inventory inventory = Bukkit.createInventory(null, InventoryType.ANVIL, Component.text(title));

        // Slot 0: Input-Item (Papier mit Placeholder)
        ItemStack inputItem = new ItemStack(Material.PAPER);
        ItemMeta meta = inputItem.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(placeholder);
            inputItem.setItemMeta(meta);
        }
        inventory.setItem(0, inputItem);

        // Slot 2: Bestätigung (grünes Glas)
        ItemStack confirmButton = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
        ItemMeta confirmMeta = confirmButton.getItemMeta();
        if (confirmMeta != null) {
            confirmMeta.setDisplayName("§a§lBestätigen");
            confirmButton.setItemMeta(confirmMeta);
        }
        inventory.setItem(2, confirmButton);

        // Registriere aktives UI
        activeUIs.put(player.getUniqueId(), this);
        confirmed = false;

        // Öffne Inventory
        player.openInventory(inventory);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        TextInputUi activeUI = activeUIs.get(player.getUniqueId());
        if (activeUI != this) {
            return;
        }

        // Nur Anvil-Inventory verarbeiten
        if (event.getInventory().getType() != InventoryType.ANVIL) {
            return;
        }

        int slot = event.getRawSlot();

        // Slot 2: Bestätigung
        if (slot == 2) {
            event.setCancelled(true);

            // Hole eingegebenen Text aus Slot 0
            ItemStack inputItem = event.getInventory().getItem(0);
            String text = "";

            if (inputItem != null && inputItem.hasItemMeta()) {
                ItemMeta meta = inputItem.getItemMeta();
                if (meta != null && meta.hasDisplayName()) {
                    text = meta.getDisplayName();
                }
            }

            // Markiere als bestätigt (verhindert Cancel-Handler)
            confirmed = true;

            // Rufe Input-Handler auf
            if (inputHandler != null && !text.isEmpty()) {
                inputHandler.accept(text);
            }

            // Schließe UI
            player.closeInventory();
            cleanup(player);
        }
        // Slot 0: Verhindere Entfernung des Input-Items
        else if (slot == 0) {
            // Erlaube Umbenennung, aber verhindere Entfernung
            if (event.getClick().isShiftClick() || event.isShiftClick()) {
                event.setCancelled(true);
            }
        }
        // Alle anderen Slots: Verhindere Interaktion
        else if (slot < 3) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) {
            return;
        }

        TextInputUi activeUI = activeUIs.get(player.getUniqueId());
        if (activeUI != this) {
            return;
        }

        // Nur Anvil-Inventory verarbeiten
        if (event.getInventory().getType() != InventoryType.ANVIL) {
            return;
        }

        // Wenn nicht bestätigt → Abbruch
        if (!confirmed && cancelHandler != null) {
            cancelHandler.accept(player);
        }

        cleanup(player);
    }

    /**
     * Räumt Listener und aktive UI-Registrierung auf.
     *
     * @param player Spieler
     */
    private void cleanup(Player player) {
        activeUIs.remove(player.getUniqueId());
        org.bukkit.event.HandlerList.unregisterAll(this);
    }

    @Override
    public void close(Player player) {
        player.closeInventory();
        cleanup(player);
    }
}
