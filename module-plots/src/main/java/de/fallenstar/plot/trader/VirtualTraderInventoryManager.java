package de.fallenstar.plot.trader;

import de.fallenstar.core.provider.Plot;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * Verwaltet alle virtuellen Händler-Inventare.
 *
 * Features:
 * - Zentrale Verwaltung aller Inventare
 * - Auto-Save bei Inventar-Schließung
 * - Cache für Performance
 * - Persistierung in Config
 *
 * **Verwendung:**
 * <pre>
 * VirtualTraderInventory inv = manager.getOrCreateInventory(player.getUniqueId(), plot);
 * inv.open(player);
 * </pre>
 *
 * **Integration:**
 * - HandelsgildeUi: Button "Händler-Inventar"
 * - SlotManagementUi: Händler-Auswahl
 *
 * @author FallenStar
 * @version 1.0
 */
public class VirtualTraderInventoryManager implements Listener {

    private final Logger logger;
    private final Map<String, VirtualTraderInventory> inventoryCache;  // Key: "plotId:playerId"

    /**
     * Konstruktor für VirtualTraderInventoryManager.
     *
     * @param logger Logger
     */
    public VirtualTraderInventoryManager(Logger logger) {
        this.logger = logger;
        this.inventoryCache = new ConcurrentHashMap<>();

        logger.info("VirtualTraderInventoryManager initialisiert");
    }

    /**
     * Gibt das Inventar für einen Spieler auf einem Plot zurück.
     *
     * Erstellt ein neues Inventar wenn noch nicht vorhanden.
     *
     * @param playerId Player-UUID
     * @param plot Der Plot
     * @return VirtualTraderInventory
     */
    public VirtualTraderInventory getOrCreateInventory(UUID playerId, Plot plot) {
        String key = getCacheKey(playerId, plot);

        return inventoryCache.computeIfAbsent(key, k ->
                new VirtualTraderInventory(playerId, plot, logger)
        );
    }

    /**
     * Gibt das Inventar für einen Spieler zurück (falls vorhanden).
     *
     * @param playerId Player-UUID
     * @param plot Der Plot
     * @return Optional mit VirtualTraderInventory
     */
    public Optional<VirtualTraderInventory> getInventory(UUID playerId, Plot plot) {
        String key = getCacheKey(playerId, plot);
        return Optional.ofNullable(inventoryCache.get(key));
    }

    /**
     * Entfernt ein Inventar aus dem Cache.
     *
     * @param playerId Player-UUID
     * @param plot Der Plot
     * @return true wenn entfernt
     */
    public boolean removeInventory(UUID playerId, Plot plot) {
        String key = getCacheKey(playerId, plot);
        boolean removed = inventoryCache.remove(key) != null;

        if (removed) {
            logger.info("Händler-Inventar entfernt: " + key);
        }

        return removed;
    }

    /**
     * Gibt die Anzahl gecachter Inventare zurück.
     *
     * @return Anzahl
     */
    public int getCachedInventoryCount() {
        return inventoryCache.size();
    }

    /**
     * Lädt alle Inventare aus Config.
     *
     * WICHTIG: Lädt nur Struktur, nicht die vollständigen Inventare!
     * Inventare werden lazy via getOrCreateInventory() geladen.
     *
     * @param config FileConfiguration
     */
    public void loadFromConfig(FileConfiguration config) {
        // Inventare werden lazy geladen via getOrCreateInventory()
        // Hier nur Info-Log
        logger.info("VirtualTraderInventoryManager bereit zum Laden");
    }

    /**
     * Speichert alle Inventare in Config.
     *
     * @param config FileConfiguration
     */
    public void saveToConfig(FileConfiguration config) {
        // Lösche alte Daten
        config.set("trader-inventories", null);

        // Speichere alle Inventare
        for (VirtualTraderInventory inventory : inventoryCache.values()) {
            inventory.saveToConfig(config);
        }

        logger.info("Händler-Inventare gespeichert: " + inventoryCache.size() + " Inventare");
    }

    /**
     * Behandelt Inventar-Schließungen (Auto-Save).
     *
     * @param event InventoryCloseEvent
     */
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) {
            return;
        }

        Inventory inventory = event.getInventory();
        String title = event.getView().getTitle();

        // Prüfe ob es ein Händler-Inventar ist
        if (!title.contains("Händler-Inventar")) {
            return;
        }

        // Suche passendes VirtualTraderInventory
        for (VirtualTraderInventory virtualInv : inventoryCache.values()) {
            if (virtualInv.getPlayerId().equals(player.getUniqueId())) {
                // Aktualisiere Inhalte
                virtualInv.updateContents(inventory);

                logger.fine("Händler-Inventar aktualisiert für " + player.getName());
                player.sendMessage("§a✓ Händler-Inventar gespeichert");

                // Hinweis: Config wird beim Plugin-Disable oder via /plot gui gespeichert
                break;
            }
        }
    }

    /**
     * Gibt den Cache-Key zurück.
     *
     * Format: "plotId:playerId"
     *
     * @param playerId Player-UUID
     * @param plot Der Plot
     * @return Cache-Key
     */
    private String getCacheKey(UUID playerId, Plot plot) {
        return plot.getIdentifier() + ":" + playerId.toString();
    }

    /**
     * Gibt Debug-Informationen zurück.
     *
     * @return Debug-String
     */
    public String getDebugInfo() {
        return "VirtualTraderInventoryManager{" +
                "cachedInventories=" + inventoryCache.size() +
                '}';
    }
}
