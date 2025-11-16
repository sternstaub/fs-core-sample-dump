package de.fallenstar.core.provider.impl;

import de.fallenstar.core.exception.ProviderFunctionalityNotFoundException;
import de.fallenstar.core.provider.TownProvider;
import org.bukkit.Location;

import java.util.List;
import java.util.UUID;

/**
 * NoOp (No Operation) Implementation des TownProviders.
 *
 * Wird verwendet wenn kein Town-Plugin (Towny, etc.) verfügbar ist.
 * Alle Methoden werfen ProviderFunctionalityNotFoundException.
 *
 * Pattern: Null Object Pattern kombiniert mit Exception-Handling
 * Vorteil: Kein Null-Checking nötig, explizites Fehlerhandling
 *
 * @author FallenStar
 * @version 1.0
 */
public class NoOpTownProvider implements TownProvider {

    private static final String PROVIDER_NAME = "TownProvider";
    private static final String REASON = "No town plugin (Towny, etc.) available";

    @Override
    public boolean isAvailable() {
        return false;
    }

    @Override
    public Location getTownSpawn(String townName)
            throws ProviderFunctionalityNotFoundException {
        throw new ProviderFunctionalityNotFoundException(
            PROVIDER_NAME,
            "getTownSpawn",
            REASON
        );
    }

    @Override
    public String getTownName(Location location)
            throws ProviderFunctionalityNotFoundException {
        throw new ProviderFunctionalityNotFoundException(
            PROVIDER_NAME,
            "getTownName",
            REASON
        );
    }

    @Override
    public List<String> getAllTowns()
            throws ProviderFunctionalityNotFoundException {
        throw new ProviderFunctionalityNotFoundException(
            PROVIDER_NAME,
            "getAllTowns",
            REASON
        );
    }

    @Override
    public boolean isResident(UUID playerUuid, String townName)
            throws ProviderFunctionalityNotFoundException {
        throw new ProviderFunctionalityNotFoundException(
            PROVIDER_NAME,
            "isResident",
            REASON
        );
    }

    @Override
    public boolean townExists(String townName)
            throws ProviderFunctionalityNotFoundException {
        throw new ProviderFunctionalityNotFoundException(
            PROVIDER_NAME,
            "townExists",
            REASON
        );
    }

    @Override
    public int getResidentCount(String townName)
            throws ProviderFunctionalityNotFoundException {
        throw new ProviderFunctionalityNotFoundException(
            PROVIDER_NAME,
            "getResidentCount",
            REASON
        );
    }
}
