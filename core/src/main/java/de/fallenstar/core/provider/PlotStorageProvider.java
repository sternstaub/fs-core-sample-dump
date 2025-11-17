package de.fallenstar.core.provider;

import de.fallenstar.core.exception.ProviderFunctionalityNotFoundException;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;

/**
 * Provider-Interface für Plot-basiertes Storage-System.
 *
 * Implementierungen:
 * - TownyPlotStorageProvider (Towny-basierte Storage-Verwaltung)
 * - NoOpPlotStorageProvider (Fallback)
 *
 * Features:
 * - Material-Tracking auf Plots
 * - Input/Output-Chest-Verwaltung
 * - Storage-Scan und -Verwaltung
 *
 * @author FallenStar
 * @version 1.0
 */
public interface PlotStorageProvider {

    /**
     * Prüft ob dieser Provider verfügbar ist.
     *
     * @return true wenn verfügbar
     */
    boolean isAvailable();

    /**
     * Gibt die Gesamtmenge eines Materials auf einem Plot zurück.
     *
     * @param plot Das Plot-Objekt
     * @param material Der Material-Typ
     * @return Die Gesamtmenge (0 wenn nicht vorhanden)
     * @throws ProviderFunctionalityNotFoundException wenn nicht verfügbar
     */
    int getMaterialAmount(Plot plot, Material material)
            throws ProviderFunctionalityNotFoundException;

    /**
     * Entfernt eine Menge Material von einem Plot.
     *
     * @param plot Das Plot-Objekt
     * @param material Der Material-Typ
     * @param amount Die zu entfernende Menge
     * @return true wenn erfolgreich entfernt
     * @throws ProviderFunctionalityNotFoundException wenn nicht verfügbar
     */
    boolean removeMaterial(Plot plot, Material material, int amount)
            throws ProviderFunctionalityNotFoundException;

    /**
     * Fügt Material zu den Input-Chests hinzu.
     *
     * @param plot Das Plot-Objekt
     * @param item Das hinzuzufügende Item
     * @return true wenn erfolgreich
     * @throws ProviderFunctionalityNotFoundException wenn nicht verfügbar
     */
    boolean addToInputChests(Plot plot, ItemStack item)
            throws ProviderFunctionalityNotFoundException;

    /**
     * Gibt alle verfügbaren Materialien auf einem Plot zurück.
     *
     * @param plot Das Plot-Objekt
     * @return Map von Material → Gesamtmenge
     * @throws ProviderFunctionalityNotFoundException wenn nicht verfügbar
     */
    Map<Material, Integer> getAllMaterials(Plot plot)
            throws ProviderFunctionalityNotFoundException;

    /**
     * Scannt die Storage-Truhen eines Plots neu.
     *
     * @param plot Das Plot-Objekt
     * @throws ProviderFunctionalityNotFoundException wenn nicht verfügbar
     */
    void scanPlotStorage(Plot plot)
            throws ProviderFunctionalityNotFoundException;

    /**
     * Gibt die Input-Chest-Locations zurück.
     *
     * @param plot Das Plot-Objekt
     * @return Liste der Input-Chest-Locations
     * @throws ProviderFunctionalityNotFoundException wenn nicht verfügbar
     */
    List<Location> getInputChestLocations(Plot plot)
            throws ProviderFunctionalityNotFoundException;

    /**
     * Gibt die Output-Chest-Locations zurück.
     *
     * @param plot Das Plot-Objekt
     * @return Liste der Output-Chest-Locations
     * @throws ProviderFunctionalityNotFoundException wenn nicht verfügbar
     */
    List<Location> getOutputChestLocations(Plot plot)
            throws ProviderFunctionalityNotFoundException;

    /**
     * Setzt die Empfangskiste (Input-Chest) für einen Plot.
     *
     * @param plot Das Plot-Objekt
     * @param location Die Location der Chest
     * @return true wenn erfolgreich
     * @throws ProviderFunctionalityNotFoundException wenn nicht verfügbar
     */
    boolean setInputChest(Plot plot, Location location)
            throws ProviderFunctionalityNotFoundException;
}
