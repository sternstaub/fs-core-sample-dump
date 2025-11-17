package de.fallenstar.core.provider;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import de.fallenstar.core.exception.ProviderFunctionalityNotFoundException;

/**
 * Provider-Interface für Grundstücks-/Plot-Systeme.
 * 
 * Implementierungen:
 * - TownyPlotProvider (Towny-Integration)
 * - FactionsPlotProvider (Factions-Integration)
 * - NoOpPlotProvider (Fallback wenn keine Plot-Plugin vorhanden)
 * 
 * @author FallenStar
 * @version 1.0
 */
public interface PlotProvider {
    
    /**
     * Prüft ob dieser Provider verfügbar/funktionsfähig ist.
     * 
     * @return true wenn Provider funktioniert, false sonst
     */
    boolean isAvailable();
    
    /**
     * Gibt das Grundstück an der angegebenen Location zurück.
     * 
     * @param location Die zu prüfende Location
     * @return Plot-Objekt oder null wenn kein Plot vorhanden
     * @throws ProviderFunctionalityNotFoundException wenn Feature nicht verfügbar
     */
    Plot getPlot(Location location) throws ProviderFunctionalityNotFoundException;
    
    /**
     * Prüft ob ein Spieler Baurechte an einer Location hat.
     * 
     * @param player Der zu prüfende Spieler
     * @param location Die Location
     * @return true wenn Spieler bauen darf
     * @throws ProviderFunctionalityNotFoundException wenn Feature nicht verfügbar
     */
    boolean canBuild(Player player, Location location) 
            throws ProviderFunctionalityNotFoundException;
    
    /**
     * Gibt den Besitzer/Fraktion des Grundstücks zurück.
     * 
     * @param plot Das Plot-Objekt
     * @return Name des Besitzers/der Fraktion
     * @throws ProviderFunctionalityNotFoundException wenn Feature nicht verfügbar
     */
    String getOwnerName(Plot plot) throws ProviderFunctionalityNotFoundException;
    
    /**
     * Gibt den Typ des Grundstücks zurück (z.B. "market", "embassy", "bank").
     * 
     * @param plot Das Plot-Objekt
     * @return Plot-Typ oder null wenn nicht definiert
     * @throws ProviderFunctionalityNotFoundException wenn Feature nicht verfügbar
     */
    String getPlotType(Plot plot) throws ProviderFunctionalityNotFoundException;
    
    /**
     * Prüft ob ein Spieler Admin-Rechte auf einem Grundstück hat.
     * (Mayor, Assistant, etc.)
     *
     * @param player Der Spieler
     * @param plot Das Grundstück
     * @return true wenn Admin-Rechte vorhanden
     * @throws ProviderFunctionalityNotFoundException wenn Feature nicht verfügbar
     */
    boolean hasAdminRights(Player player, Plot plot)
            throws ProviderFunctionalityNotFoundException;

    /**
     * Prüft ob ein Spieler der Besitzer eines Grundstücks ist.
     *
     * @param plot Das Grundstück
     * @param player Der Spieler
     * @return true wenn Spieler der Besitzer ist
     * @throws ProviderFunctionalityNotFoundException wenn Feature nicht verfügbar
     */
    boolean isOwner(Plot plot, Player player)
            throws ProviderFunctionalityNotFoundException;
}
