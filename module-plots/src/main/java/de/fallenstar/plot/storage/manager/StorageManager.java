package de.fallenstar.plot.storage.manager;

import de.fallenstar.core.provider.Plot;
import de.fallenstar.core.provider.PlotProvider;
import de.fallenstar.core.exception.ProviderFunctionalityNotFoundException;
import de.fallenstar.plot.storage.model.ChestData;
import de.fallenstar.plot.storage.model.PlotStorage;
import de.fallenstar.plot.storage.model.StoredMaterial;
import de.fallenstar.plot.storage.provider.PlotStorageProvider;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * Manager für Storage-Operationen.
 *
 * Koordiniert Material-Verwaltung zwischen PlotStorage und tatsächlichen Truhen.
 * Bietet High-Level API für add/remove Operationen.
 *
 * @author FallenStar
 * @version 1.0
 */
public class StorageManager {

    private final Logger logger;
    private final PlotProvider plotProvider;
    private final PlotStorageProvider storageProvider;
    private final ChestScanService scanService;

    /**
     * Erstellt einen neuen StorageManager.
     *
     * @param logger Der Logger für Ausgaben
     * @param plotProvider Der PlotProvider
     * @param storageProvider Der PlotStorageProvider
     */
    public StorageManager(Logger logger, PlotProvider plotProvider, PlotStorageProvider storageProvider) {
        this.logger = logger;
        this.plotProvider = plotProvider;
        this.storageProvider = storageProvider;
        this.scanService = new ChestScanService(logger, plotProvider);
    }

    /**
     * Gibt die Gesamtmenge eines Materials auf einem Plot zurück.
     *
     * @param plot Das Plot
     * @param material Der Material-Typ
     * @return Die Gesamtmenge (0 wenn nicht vorhanden)
     */
    public int getMaterialAmount(Plot plot, Material material) {
        return storageProvider.getMaterialAmount(plot, material);
    }

    /**
     * Entfernt Material von einem Plot (aus den Truhen).
     *
     * @param plot Das Plot
     * @param material Der Material-Typ
     * @param amount Die zu entfernende Menge
     * @return true wenn erfolgreich entfernt
     */
    public boolean removeMaterial(Plot plot, Material material, int amount) {
        PlotStorage storage = storageProvider.getPlotStorage(plot);

        // Prüfe verfügbare Menge
        int available = storage.getTotalAmount(material);
        if (available < amount) {
            logger.warning("Nicht genug " + material + " auf Plot " + plot.getIdentifier() +
                         " (verfügbar: " + available + ", benötigt: " + amount + ")");
            return false;
        }

        // Entferne aus Truhen
        List<StoredMaterial> materialLocations = storage.getMaterialLocations(material);
        int remaining = amount;

        for (StoredMaterial stored : materialLocations) {
            if (remaining <= 0) break;

            int toRemove = Math.min(remaining, stored.getAmount());

            // Entferne aus echter Truhe
            if (removeFromChest(stored.getChestLocation(), material, toRemove)) {
                // Aktualisiere StoredMaterial
                stored.removeAmount(toRemove);
                remaining -= toRemove;

                // Entferne Eintrag wenn leer
                if (stored.getAmount() == 0) {
                    storage.removeMaterial(stored);
                }
            } else {
                logger.warning("Fehler beim Entfernen von Material aus Truhe: " + stored.getChestLocation());
            }
        }

        boolean success = remaining == 0;
        if (success) {
            logger.info("Entfernt: " + amount + "x " + material + " von Plot " + plot.getIdentifier());
        } else {
            logger.warning("Konnte nur " + (amount - remaining) + "/" + amount + "x " + material + " entfernen");
        }

        return success;
    }

    /**
     * Fügt Material zu einem Plot hinzu (in Empfangskiste oder verfügbare Truhe).
     *
     * @param plot Das Plot
     * @param material Der Material-Typ
     * @param amount Die Menge
     * @return true wenn erfolgreich hinzugefügt
     */
    public boolean addMaterial(Plot plot, Material material, int amount) {
        PlotStorage storage = storageProvider.getPlotStorage(plot);

        // Prüfe ob Empfangskiste vorhanden
        ChestData receiverChest = storage.getReceiverChest();
        Location targetLocation = null;
        UUID targetChestId = null;

        if (receiverChest != null) {
            targetLocation = receiverChest.getLocation();
            targetChestId = receiverChest.getChestId();
        } else {
            // Fallback: Erste verfügbare Truhe
            if (!storage.getAllChests().isEmpty()) {
                ChestData firstChest = storage.getAllChests().iterator().next();
                targetLocation = firstChest.getLocation();
                targetChestId = firstChest.getChestId();
            }
        }

        if (targetLocation == null) {
            logger.warning("Keine Truhen auf Plot " + plot.getIdentifier() + " verfügbar");
            return false;
        }

        // Füge zu Truhe hinzu
        int added = addToChest(targetLocation, material, amount);

        if (added > 0) {
            // Aktualisiere PlotStorage
            updateChestInStorage(storage, targetChestId, targetLocation);
            logger.info("Hinzugefügt: " + added + "x " + material + " zu Plot " + plot.getIdentifier());
            return added == amount;
        }

        return false;
    }

    /**
     * Entfernt Material aus einer Truhe.
     *
     * @param location Location der Truhe
     * @param material Der Material-Typ
     * @param amount Die zu entfernende Menge
     * @return true wenn erfolgreich
     */
    private boolean removeFromChest(Location location, Material material, int amount) {
        Block block = location.getBlock();
        if (!(block.getState() instanceof Chest)) {
            return false;
        }

        Chest chest = (Chest) block.getState();
        Inventory inventory = chest.getInventory();

        int remaining = amount;

        for (int i = 0; i < inventory.getSize(); i++) {
            ItemStack item = inventory.getItem(i);
            if (item != null && item.getType() == material) {
                int stackAmount = item.getAmount();
                int toRemove = Math.min(remaining, stackAmount);

                if (toRemove >= stackAmount) {
                    inventory.setItem(i, null);
                } else {
                    item.setAmount(stackAmount - toRemove);
                }

                remaining -= toRemove;
                if (remaining <= 0) break;
            }
        }

        return remaining == 0;
    }

    /**
     * Fügt Material zu einer Truhe hinzu.
     *
     * @param location Location der Truhe
     * @param material Der Material-Typ
     * @param amount Die Menge
     * @return Tatsächlich hinzugefügte Menge
     */
    private int addToChest(Location location, Material material, int amount) {
        Block block = location.getBlock();
        if (!(block.getState() instanceof Chest)) {
            return 0;
        }

        Chest chest = (Chest) block.getState();
        Inventory inventory = chest.getInventory();

        ItemStack itemStack = new ItemStack(material, amount);
        int remaining = amount;

        // Versuche zu bestehenden Stacks hinzuzufügen
        for (int i = 0; i < inventory.getSize(); i++) {
            ItemStack existingItem = inventory.getItem(i);
            if (existingItem != null && existingItem.getType() == material) {
                int maxStackSize = material.getMaxStackSize();
                int currentAmount = existingItem.getAmount();
                int canAdd = maxStackSize - currentAmount;

                if (canAdd > 0) {
                    int toAdd = Math.min(remaining, canAdd);
                    existingItem.setAmount(currentAmount + toAdd);
                    remaining -= toAdd;

                    if (remaining <= 0) break;
                }
            }
        }

        // Füge zu leeren Slots hinzu
        if (remaining > 0) {
            for (int i = 0; i < inventory.getSize(); i++) {
                ItemStack slot = inventory.getItem(i);
                if (slot == null || slot.getType() == Material.AIR) {
                    int toAdd = Math.min(remaining, material.getMaxStackSize());
                    inventory.setItem(i, new ItemStack(material, toAdd));
                    remaining -= toAdd;

                    if (remaining <= 0) break;
                }
            }
        }

        return amount - remaining;
    }

    /**
     * Aktualisiert eine Truhe im PlotStorage (nach Änderung).
     *
     * @param storage Das PlotStorage
     * @param chestId Die Truhen-ID
     * @param location Location der Truhe
     */
    private void updateChestInStorage(PlotStorage storage, UUID chestId, Location location) {
        Block block = location.getBlock();
        if (!(block.getState() instanceof Chest)) {
            return;
        }

        Chest chest = (Chest) block.getState();
        Inventory inventory = chest.getInventory();

        // Entferne alte Einträge für diese Truhe
        for (Material mat : Material.values()) {
            List<StoredMaterial> materials = storage.getMaterialLocations(mat);
            materials.removeIf(sm -> sm.getChestId().equals(chestId));
        }

        // Scanne Inventar neu
        for (ItemStack item : inventory.getContents()) {
            if (item != null && item.getType() != Material.AIR) {
                StoredMaterial storedMaterial = new StoredMaterial(
                    chestId,
                    location,
                    item.getType(),
                    item.getAmount()
                );
                storage.addMaterial(storedMaterial);
            }
        }
    }

    /**
     * Scannt ein Plot neu und aktualisiert den Storage.
     *
     * @param plot Das zu scannende Plot
     */
    public void rescanPlot(Plot plot) {
        PlotStorage storage = storageProvider.getPlotStorage(plot);
        storage.clear();
        scanService.scanPlot(plot, storage);
    }

    /**
     * Setzt eine Truhe als Empfangskiste für ein Plot.
     *
     * @param plot Das Plot
     * @param chestLocation Location der Truhe
     * @return true wenn erfolgreich
     */
    public boolean setReceiverChest(Plot plot, Location chestLocation) {
        return storageProvider.setReceiverChest(plot, chestLocation);
    }

    /**
     * @return Der ChestScanService
     */
    public ChestScanService getScanService() {
        return scanService;
    }
}
