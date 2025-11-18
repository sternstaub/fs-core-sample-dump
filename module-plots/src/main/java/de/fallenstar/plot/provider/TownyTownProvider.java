package de.fallenstar.plot.provider;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;
import com.palmergames.bukkit.towny.object.Resident;
import de.fallenstar.core.exception.ProviderFunctionalityNotFoundException;
import de.fallenstar.core.provider.TownProvider;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Towny-Integration für TownProvider.
 *
 * Nutzt die Towny-API um Town-Informationen bereitzustellen.
 *
 * Implementierung:
 * - Towns werden direkt von Towny abgerufen
 * - Spawn-Points nutzen Towny's Spawn-System
 * - Resident-Checks über Towny-API
 *
 * @author FallenStar
 * @version 1.0
 */
public class TownyTownProvider implements TownProvider {

    private final TownyAPI townyAPI;

    /**
     * Erstellt einen neuen TownyTownProvider.
     */
    public TownyTownProvider() {
        this.townyAPI = TownyAPI.getInstance();
    }

    @Override
    public boolean isAvailable() {
        return townyAPI != null;
    }

    @Override
    public Location getTownSpawn(String townName)
            throws ProviderFunctionalityNotFoundException {
        if (!isAvailable()) {
            throw new ProviderFunctionalityNotFoundException(
                "TownProvider", "getTownSpawn", "Towny API not available"
            );
        }

        try {
            Town town = townyAPI.getTown(townName);

            if (town == null) {
                throw new ProviderFunctionalityNotFoundException(
                    "TownProvider", "getTownSpawn",
                    "Town '" + townName + "' not found"
                );
            }

            // Hole Spawn-Location der Town
            Location spawn = town.getSpawn();

            if (spawn == null) {
                throw new ProviderFunctionalityNotFoundException(
                    "TownProvider", "getTownSpawn",
                    "Town '" + townName + "' has no spawn point set"
                );
            }

            return spawn;

        } catch (ProviderFunctionalityNotFoundException e) {
            throw e; // Re-throw our custom exceptions
        } catch (Exception e) {
            throw new ProviderFunctionalityNotFoundException(
                "TownProvider", "getTownSpawn",
                "Error accessing town spawn: " + e.getMessage()
            );
        }
    }

    @Override
    public String getTownName(Location location)
            throws ProviderFunctionalityNotFoundException {
        if (!isAvailable()) {
            throw new ProviderFunctionalityNotFoundException(
                "TownProvider", "getTownName", "Towny API not available"
            );
        }

        if (location == null) {
            return null;
        }

        try {
            TownBlock townBlock = townyAPI.getTownBlock(location);

            if (townBlock == null) {
                return null; // Keine Town an dieser Location
            }

            Town town = townBlock.getTownOrNull();
            return (town != null) ? town.getName() : null;

        } catch (Exception e) {
            throw new ProviderFunctionalityNotFoundException(
                "TownProvider", "getTownName",
                "Error getting town name: " + e.getMessage()
            );
        }
    }

    @Override
    public List<String> getAllTowns()
            throws ProviderFunctionalityNotFoundException {
        if (!isAvailable()) {
            throw new ProviderFunctionalityNotFoundException(
                "TownProvider", "getAllTowns", "Towny API not available"
            );
        }

        try {
            // Hole alle Towns und konvertiere zu Namen
            return townyAPI.getTowns().stream()
                    .map(Town::getName)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            throw new ProviderFunctionalityNotFoundException(
                "TownProvider", "getAllTowns",
                "Error getting town list: " + e.getMessage()
            );
        }
    }

    @Override
    public boolean isResident(UUID playerUuid, String townName)
            throws ProviderFunctionalityNotFoundException {
        if (!isAvailable()) {
            throw new ProviderFunctionalityNotFoundException(
                "TownProvider", "isResident", "Towny API not available"
            );
        }

        try {
            // Hole Resident
            Resident resident = townyAPI.getResident(playerUuid);
            if (resident == null) {
                return false; // Spieler ist kein Resident
            }

            // Prüfe ob Resident eine Town hat
            if (!resident.hasTown()) {
                return false;
            }

            // Prüfe ob die Town übereinstimmt
            Town town = resident.getTownOrNull();
            if (town == null) {
                return false;
            }

            return town.getName().equalsIgnoreCase(townName);

        } catch (Exception e) {
            throw new ProviderFunctionalityNotFoundException(
                "TownProvider", "isResident",
                "Error checking residency: " + e.getMessage()
            );
        }
    }

    @Override
    public boolean townExists(String townName)
            throws ProviderFunctionalityNotFoundException {
        if (!isAvailable()) {
            throw new ProviderFunctionalityNotFoundException(
                "TownProvider", "townExists", "Towny API not available"
            );
        }

        try {
            Town town = townyAPI.getTown(townName);
            return town != null;

        } catch (Exception e) {
            // Bei Exception (z.B. Town nicht gefunden) -> false
            return false;
        }
    }

    @Override
    public int getResidentCount(String townName)
            throws ProviderFunctionalityNotFoundException {
        if (!isAvailable()) {
            throw new ProviderFunctionalityNotFoundException(
                "TownProvider", "getResidentCount", "Towny API not available"
            );
        }

        try {
            Town town = townyAPI.getTown(townName);

            if (town == null) {
                throw new ProviderFunctionalityNotFoundException(
                    "TownProvider", "getResidentCount",
                    "Town '" + townName + "' not found"
                );
            }

            return town.getResidents().size();

        } catch (ProviderFunctionalityNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new ProviderFunctionalityNotFoundException(
                "TownProvider", "getResidentCount",
                "Error getting resident count: " + e.getMessage()
            );
        }
    }
}
