package de.fallenstar.storage.storageprovider;

import de.fallenstar.core.provider.Plot;
import de.fallenstar.storage.model.ChestData;
import de.fallenstar.storage.model.PlotStorage;
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
        List<de.fallenstar.storage.model.StoredMaterial> materialLocations =
            storage.getMaterialLocations(material);

        for (de.fallenstar.storage.model.StoredMaterial stored : materialLocations) {
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
}
