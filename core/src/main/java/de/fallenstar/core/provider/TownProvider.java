package de.fallenstar.core.provider;

import org.bukkit.Location;
import de.fallenstar.core.exception.ProviderFunctionalityNotFoundException;
import java.util.List;
import java.util.UUID;

/**
 * Provider-Interface für Town-/Stadt-Systeme.
 *
 * Implementierungen:
 * - TownyTownProvider (Towny-Integration)
 * - NoOpTownProvider (Fallback wenn kein Town-Plugin vorhanden)
 *
 * @author FallenStar
 * @version 1.0
 */
public interface TownProvider {

    /**
     * Prüft ob dieser Provider verfügbar/funktionsfähig ist.
     *
     * @return true wenn Provider funktioniert, false sonst
     */
    boolean isAvailable();

    /**
     * Gibt den Spawn-Punkt einer Town zurück.
     *
     * @param townName Name der Town
     * @return Spawn-Location der Town
     * @throws ProviderFunctionalityNotFoundException wenn Feature nicht verfügbar
     */
    Location getTownSpawn(String townName)
            throws ProviderFunctionalityNotFoundException;

    /**
     * Gibt den Namen der Town an einer Location zurück.
     *
     * @param location Die zu prüfende Location
     * @return Name der Town oder null wenn keine Town vorhanden
     * @throws ProviderFunctionalityNotFoundException wenn Feature nicht verfügbar
     */
    String getTownName(Location location)
            throws ProviderFunctionalityNotFoundException;

    /**
     * Gibt eine Liste aller Towns zurück.
     *
     * @return Liste aller Town-Namen
     * @throws ProviderFunctionalityNotFoundException wenn Feature nicht verfügbar
     */
    List<String> getAllTowns()
            throws ProviderFunctionalityNotFoundException;

    /**
     * Prüft ob ein Spieler Bürger einer bestimmten Town ist.
     *
     * @param playerUuid UUID des Spielers
     * @param townName Name der Town
     * @return true wenn Spieler Bürger ist
     * @throws ProviderFunctionalityNotFoundException wenn Feature nicht verfügbar
     */
    boolean isResident(UUID playerUuid, String townName)
            throws ProviderFunctionalityNotFoundException;

    /**
     * Prüft ob eine Town existiert.
     *
     * @param townName Name der Town
     * @return true wenn Town existiert
     * @throws ProviderFunctionalityNotFoundException wenn Feature nicht verfügbar
     */
    boolean townExists(String townName)
            throws ProviderFunctionalityNotFoundException;

    /**
     * Gibt die Anzahl der Einwohner einer Town zurück.
     *
     * @param townName Name der Town
     * @return Anzahl der Einwohner
     * @throws ProviderFunctionalityNotFoundException wenn Feature nicht verfügbar
     */
    int getResidentCount(String townName)
            throws ProviderFunctionalityNotFoundException;
}
