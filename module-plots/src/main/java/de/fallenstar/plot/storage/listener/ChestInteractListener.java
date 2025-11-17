package de.fallenstar.plot.storage.listener;

import de.fallenstar.core.provider.Plot;
import de.fallenstar.core.provider.PlotProvider;
import de.fallenstar.core.exception.ProviderFunctionalityNotFoundException;
import de.fallenstar.plot.storage.manager.StorageManager;
import de.fallenstar.plot.storage.model.PlotStorage;
import de.fallenstar.plot.storage.provider.PlotStorageProvider;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.InventoryHolder;

import java.util.logging.Logger;

/**
 * Listener für Truhen-Interaktionen.
 *
 * Aktualisiert den Storage bei:
 * - Truhen-Schließen (Inventar-Änderungen)
 * - Truhen-Platzieren (neue Truhe)
 * - Truhen-Zerstören (Truhe entfernen)
 *
 * Performance-Optimierung: Nur bei Interaktion, nicht kontinuierlich.
 *
 * @author FallenStar
 * @version 1.0
 */
public class ChestInteractListener implements Listener {

    private final Logger logger;
    private final PlotProvider plotProvider;
    private final PlotStorageProvider storageProvider;
    private final StorageManager storageManager;

    /**
     * Erstellt einen neuen ChestInteractListener.
     *
     * @param logger Der Logger
     * @param plotProvider Der PlotProvider
     * @param storageProvider Der PlotStorageProvider
     * @param storageManager Der StorageManager
     */
    public ChestInteractListener(Logger logger, PlotProvider plotProvider,
                                  PlotStorageProvider storageProvider,
                                  StorageManager storageManager) {
        this.logger = logger;
        this.plotProvider = plotProvider;
        this.storageProvider = storageProvider;
        this.storageManager = storageManager;
    }

    /**
     * Wird gefeuert wenn eine Truhe geschlossen wird.
     * Aktualisiert den Storage für dieses Plot.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryClose(InventoryCloseEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();

        // Prüfe ob es eine Truhe ist
        if (!(holder instanceof org.bukkit.block.Chest)) {
            return;
        }

        org.bukkit.block.Chest chest = (org.bukkit.block.Chest) holder;
        Location chestLocation = chest.getLocation();

        try {
            // Prüfe ob Truhe auf einem Plot ist
            Plot plot = plotProvider.getPlot(chestLocation);
            if (plot == null) {
                return; // Nicht auf einem Plot
            }

            // Rescanne nur diese eine Truhe (Performance)
            PlotStorage storage = storageProvider.getPlotStorage(plot);

            // Lösung: Kompletten Plot neu scannen (einfacher)
            // TODO: Optimierung - nur diese Truhe scannen
            storageManager.rescanPlot(plot);

            logger.fine("Storage aktualisiert für Plot " + plot.getIdentifier() +
                       " nach Truhen-Interaktion");

        } catch (ProviderFunctionalityNotFoundException e) {
            // PlotProvider nicht verfügbar - ignorieren
        } catch (Exception e) {
            logger.warning("Fehler beim Aktualisieren des Storage: " + e.getMessage());
        }
    }

    /**
     * Wird gefeuert wenn eine Truhe platziert wird.
     * Scannt das Plot neu um die neue Truhe zu registrieren.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onChestPlace(BlockPlaceEvent event) {
        Block block = event.getBlockPlaced();

        // Prüfe ob es eine Truhe ist
        if (block.getType() != Material.CHEST &&
            block.getType() != Material.TRAPPED_CHEST &&
            block.getType() != Material.BARREL) {
            return;
        }

        Location chestLocation = block.getLocation();

        try {
            // Prüfe ob auf Plot
            Plot plot = plotProvider.getPlot(chestLocation);
            if (plot == null) {
                return;
            }

            // Rescanne Plot
            storageManager.rescanPlot(plot);

            logger.info("Neue Truhe auf Plot " + plot.getIdentifier() + " registriert");

        } catch (ProviderFunctionalityNotFoundException e) {
            // PlotProvider nicht verfügbar - ignorieren
        } catch (Exception e) {
            logger.warning("Fehler beim Registrieren neuer Truhe: " + e.getMessage());
        }
    }

    /**
     * Wird gefeuert wenn eine Truhe zerstört wird.
     * Unregistriert die Truhe aus dem PlotStorage.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onChestBreak(BlockBreakEvent event) {
        Block block = event.getBlock();

        // Prüfe ob es eine Truhe ist
        if (block.getType() != Material.CHEST &&
            block.getType() != Material.TRAPPED_CHEST &&
            block.getType() != Material.BARREL) {
            return;
        }

        Location chestLocation = block.getLocation();

        try {
            // Prüfe ob auf Plot
            Plot plot = plotProvider.getPlot(chestLocation);
            if (plot == null) {
                return;
            }

            // Hole PlotStorage
            PlotStorage storage = storageProvider.getPlotStorage(plot);

            // Finde und unregistriere die Truhe
            boolean unregistered = false;
            for (de.fallenstar.plot.storage.model.ChestData chestData : storage.getAllChests()) {
                if (chestData.getLocation().equals(chestLocation)) {
                    storage.unregisterChest(chestData.getChestId());
                    unregistered = true;
                    logger.info("Truhe auf Plot " + plot.getIdentifier() + " unregistriert (Type: " +
                               chestData.getChestType().getDisplayName() + ")");
                    break;
                }
            }

            // Falls Truhe nicht registriert war, ist das kein Fehler (kann normale Truhe sein)
            if (!unregistered) {
                logger.fine("Nicht-registrierte Truhe auf Plot " + plot.getIdentifier() + " zerstört");
            }

        } catch (ProviderFunctionalityNotFoundException e) {
            // PlotProvider nicht verfügbar - ignorieren
        } catch (Exception e) {
            logger.warning("Fehler beim Entfernen von Truhe: " + e.getMessage());
        }
    }
}
