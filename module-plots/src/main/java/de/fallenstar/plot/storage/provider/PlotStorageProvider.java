package de.fallenstar.plot.storage.provider;

import de.fallenstar.core.provider.Plot;
import de.fallenstar.plot.storage.model.ChestData;
import de.fallenstar.plot.storage.model.PlotStorage;
import de.fallenstar.plot.storage.model.StoredMaterial;
import org.bukkit.Location;
import org.bukkit.Material;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Provider für Plot-basiertes Storage-System.
 *
 * Verwaltet alle PlotStorage-Objekte und bietet Zugriff auf
 * Material-Bestände auf Grundstücken.
 *
 * Thread-safe für asynchrone Operationen.
 *
 * @author FallenStar
 * @version 1.0
 */
public class PlotStorageProvider {

    private final Map<UUID, PlotStorage> plotStorageMap;

    /**
     * Erstellt einen neuen PlotStorageProvider.
     */
    public PlotStorageProvider() {
        this.plotStorageMap = new ConcurrentHashMap<>();
    }

    /**
     * Gibt das PlotStorage-Objekt für ein Grundstück zurück.
     * Erstellt ein neues, falls noch nicht vorhanden.
     *
     * @param plot Das Plot-Objekt
     * @return Das PlotStorage-Objekt
     */
    public PlotStorage getPlotStorage(Plot plot) {
        return plotStorageMap.computeIfAbsent(plot.getUuid(), k -> new PlotStorage(plot));
    }

    /**
     * Gibt die Gesamtmenge eines Materials auf einem Plot zurück.
     *
     * @param plot Das Plot-Objekt
     * @param material Der Material-Typ
     * @return Die Gesamtmenge (0 wenn nicht vorhanden)
     */
    public int getMaterialAmount(Plot plot, Material material) {
        PlotStorage storage = plotStorageMap.get(plot.getUuid());
        if (storage == null) {
            return 0;
        }
        return storage.getTotalAmount(material);
    }

    /**
     * Entfernt eine Menge Material von einem Plot.
     *
     * @param plot Das Plot-Objekt
     * @param material Der Material-Typ
     * @param amount Die zu entfernende Menge
     * @return true wenn erfolgreich entfernt, false wenn nicht genug vorhanden
     */
    public boolean removeMaterial(Plot plot, Material material, int amount) {
        PlotStorage storage = plotStorageMap.get(plot.getUuid());
        if (storage == null) {
            return false;
        }

        int available = storage.getTotalAmount(material);
        if (available < amount) {
            return false;
        }

        // Entferne Material aus Truhen
        int remaining = amount;
        List<StoredMaterial> materialLocations = storage.getMaterialLocations(material);

        for (StoredMaterial stored : materialLocations) {
            if (remaining <= 0) break;

            int toRemove = Math.min(remaining, stored.getAmount());
            stored.removeAmount(toRemove);
            remaining -= toRemove;

            // Entferne Eintrag wenn leer
            if (stored.getAmount() == 0) {
                storage.removeMaterial(stored);
            }
        }

        return remaining == 0;
    }

    /**
     * Fügt Material zu einem Plot hinzu (in Empfangskiste oder erste verfügbare Truhe).
     *
     * @param plot Das Plot-Objekt
     * @param material Der Material-Typ
     * @param amount Die Menge
     * @return true wenn erfolgreich hinzugefügt
     */
    public boolean addMaterial(Plot plot, Material material, int amount) {
        PlotStorage storage = getPlotStorage(plot);

        // Prüfe ob Empfangskiste vorhanden
        ChestData receiverChest = storage.getReceiverChest();
        if (receiverChest != null) {
            // TODO: Material in Empfangskiste hinzufügen (später mit ChestManager)
            return true;
        }

        // Fallback: Erste verfügbare Truhe
        Collection<ChestData> chests = storage.getAllChests();
        if (!chests.isEmpty()) {
            ChestData firstChest = chests.iterator().next();
            // TODO: Material in Truhe hinzufügen (später mit ChestManager)
            return true;
        }

        return false; // Keine Truhen verfügbar
    }

    /**
     * Setzt eine Truhe als Empfangskiste für ein Plot.
     *
     * @param plot Das Plot-Objekt
     * @param chestLocation Location der Truhe
     * @return true wenn erfolgreich gesetzt
     */
    public boolean setReceiverChest(Plot plot, Location chestLocation) {
        PlotStorage storage = getPlotStorage(plot);

        // Finde Truhe an Location
        for (ChestData chest : storage.getAllChests()) {
            if (chest.getLocation().equals(chestLocation)) {
                // Entferne vorherige Empfangskiste
                ChestData oldReceiver = storage.getReceiverChest();
                if (oldReceiver != null) {
                    oldReceiver.setReceiverChest(false);
                }

                // Setze neue Empfangskiste
                chest.setReceiverChest(true);
                return true;
            }
        }

        return false; // Truhe nicht gefunden
    }

    /**
     * Gibt alle registrierten Plots mit Storage zurück.
     *
     * @return Collection aller PlotStorage-Objekte
     */
    public Collection<PlotStorage> getAllPlotStorages() {
        return new ArrayList<>(plotStorageMap.values());
    }

    /**
     * Entfernt ein PlotStorage-Objekt (z.B. bei Rescan).
     *
     * @param plotId Die Plot-ID
     */
    public void removePlotStorage(UUID plotId) {
        plotStorageMap.remove(plotId);
    }

    /**
     * Leert alle Storage-Daten (z.B. bei vollständigem Rescan).
     */
    public void clearAll() {
        plotStorageMap.clear();
    }

    /**
     * @return Anzahl der verwalteten Plots
     */
    public int getPlotCount() {
        return plotStorageMap.size();
    }

    /**
     * Sammelt alle Items aus allen Output-Chests eines Plots.
     *
     * Output-Chests enthalten Items, die zum Verkauf angeboten werden.
     *
     * @param plot Das Plot-Objekt
     * @return Liste aller ItemStacks aus allen Output-Chests
     */
    public List<org.bukkit.inventory.ItemStack> getOutputChestContents(Plot plot) {
        PlotStorage storage = plotStorageMap.get(plot.getUuid());
        if (storage == null) {
            return Collections.emptyList();
        }

        List<org.bukkit.inventory.ItemStack> allItems = new ArrayList<>();

        // Hole alle Output-Chests
        List<ChestData> outputChests = storage.getOutputChests();

        for (ChestData chestData : outputChests) {
            Location chestLocation = chestData.getLocation();

            // Prüfe ob Truhe noch vorhanden ist
            if (chestLocation.getBlock().getState() instanceof org.bukkit.block.Chest) {
                org.bukkit.block.Chest chest = (org.bukkit.block.Chest) chestLocation.getBlock().getState();
                org.bukkit.inventory.Inventory inv = chest.getInventory();

                // Sammle alle Items aus der Truhe
                for (org.bukkit.inventory.ItemStack item : inv.getContents()) {
                    if (item != null && item.getType() != Material.AIR) {
                        allItems.add(item.clone()); // Clone um Referenz-Probleme zu vermeiden
                    }
                }
            }
        }

        return allItems;
    }

    /**
     * Fügt ein ItemStack zur Input-Chest (Empfangskiste) eines Plots hinzu.
     *
     * Input-Chests empfangen gekaufte Items.
     *
     * @param plot Das Plot-Objekt
     * @param stack Der ItemStack zum Hinzufügen
     * @return true wenn erfolgreich hinzugefügt, false wenn keine Input-Chest vorhanden
     */
    public boolean addToInputChests(Plot plot, org.bukkit.inventory.ItemStack stack) {
        PlotStorage storage = plotStorageMap.get(plot.getUuid());
        if (storage == null) {
            return false;
        }

        // Hole Input-Chest
        ChestData inputChest = storage.getInputChest();
        if (inputChest == null) {
            return false; // Keine Input-Chest gesetzt
        }

        Location chestLocation = inputChest.getLocation();

        // Prüfe ob Truhe noch vorhanden ist
        if (!(chestLocation.getBlock().getState() instanceof org.bukkit.block.Chest)) {
            return false; // Truhe wurde entfernt
        }

        org.bukkit.block.Chest chest = (org.bukkit.block.Chest) chestLocation.getBlock().getState();
        org.bukkit.inventory.Inventory inv = chest.getInventory();

        // Versuche Item hinzuzufügen
        HashMap<Integer, org.bukkit.inventory.ItemStack> remainingItems = inv.addItem(stack);

        // Aktualisiere Zugriffs-Zeitstempel
        inputChest.updateAccessTime();

        // Rückgabe: true wenn vollständig hinzugefügt, false wenn Teile übrig blieben
        return remainingItems.isEmpty();
    }
}
