package de.fallenstar.core.provider.impl;

import de.fallenstar.core.exception.ProviderFunctionalityNotFoundException;
import de.fallenstar.core.provider.Plot;
import de.fallenstar.core.provider.PlotStorageProvider;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;

/**
 * NoOp Implementation des PlotStorageProviders.
 *
 * Wird verwendet wenn kein Plot-Storage-System verfügbar ist.
 * Alle Methoden werfen ProviderFunctionalityNotFoundException.
 *
 * @author FallenStar
 * @version 1.0
 */
public class NoOpPlotStorageProvider implements PlotStorageProvider {

    private static final String PROVIDER_NAME = "PlotStorageProvider";
    private static final String REASON = "No plot storage system available";

    @Override
    public boolean isAvailable() {
        return false;
    }

    @Override
    public int getMaterialAmount(Plot plot, Material material)
            throws ProviderFunctionalityNotFoundException {
        throw new ProviderFunctionalityNotFoundException(
                PROVIDER_NAME, "getMaterialAmount", REASON
        );
    }

    @Override
    public boolean removeMaterial(Plot plot, Material material, int amount)
            throws ProviderFunctionalityNotFoundException {
        throw new ProviderFunctionalityNotFoundException(
                PROVIDER_NAME, "removeMaterial", REASON
        );
    }

    @Override
    public boolean addToInputChests(Plot plot, ItemStack item)
            throws ProviderFunctionalityNotFoundException {
        throw new ProviderFunctionalityNotFoundException(
                PROVIDER_NAME, "addToInputChests", REASON
        );
    }

    @Override
    public Map<Material, Integer> getAllMaterials(Plot plot)
            throws ProviderFunctionalityNotFoundException {
        throw new ProviderFunctionalityNotFoundException(
                PROVIDER_NAME, "getAllMaterials", REASON
        );
    }

    @Override
    public void scanPlotStorage(Plot plot)
            throws ProviderFunctionalityNotFoundException {
        throw new ProviderFunctionalityNotFoundException(
                PROVIDER_NAME, "scanPlotStorage", REASON
        );
    }

    @Override
    public List<Location> getInputChestLocations(Plot plot)
            throws ProviderFunctionalityNotFoundException {
        throw new ProviderFunctionalityNotFoundException(
                PROVIDER_NAME, "getInputChestLocations", REASON
        );
    }

    @Override
    public List<Location> getOutputChestLocations(Plot plot)
            throws ProviderFunctionalityNotFoundException {
        throw new ProviderFunctionalityNotFoundException(
                PROVIDER_NAME, "getOutputChestLocations", REASON
        );
    }

    @Override
    public boolean setInputChest(Plot plot, Location location)
            throws ProviderFunctionalityNotFoundException {
        throw new ProviderFunctionalityNotFoundException(
                PROVIDER_NAME, "setInputChest", REASON
        );
    }

    @Override
    public int getAllChestsCount(Plot plot)
            throws ProviderFunctionalityNotFoundException {
        throw new ProviderFunctionalityNotFoundException(
                PROVIDER_NAME, "getAllChestsCount", REASON
        );
    }

    @Override
    public long getLastUpdateTime(Plot plot)
            throws ProviderFunctionalityNotFoundException {
        throw new ProviderFunctionalityNotFoundException(
                PROVIDER_NAME, "getLastUpdateTime", REASON
        );
    }

    @Override
    public boolean hasReceiverChest(Plot plot)
            throws ProviderFunctionalityNotFoundException {
        throw new ProviderFunctionalityNotFoundException(
                PROVIDER_NAME, "hasReceiverChest", REASON
        );
    }

    @Override
    public Location getReceiverChestLocation(Plot plot)
            throws ProviderFunctionalityNotFoundException {
        throw new ProviderFunctionalityNotFoundException(
                PROVIDER_NAME, "getReceiverChestLocation", REASON
        );
    }
}
