package de.fallenstar.core.provider;

import org.bukkit.Location;
import java.util.UUID;

/**
 * Abstrakte Repräsentation eines Grundstücks/Plots.
 * Wrapper-Klasse die konkrete Plot-Implementierungen verbirgt.
 * 
 * Diese Klasse ermöglicht es, mit Plots zu arbeiten ohne die
 * konkrete Implementierung (Towny, Factions, etc.) zu kennen.
 * 
 * @author FallenStar
 * @version 1.0
 */
public class Plot {
    
    private final UUID uuid;
    private final String identifier;
    private final Location location;
    private final Object nativePlot; // TownBlock, Claim, etc.
    
    /**
     * Erstellt ein neues Plot-Objekt.
     * 
     * @param uuid Eindeutige ID des Plots
     * @param identifier Lesbarer Identifier (z.B. "TownName_PlotID")
     * @param location Eine Location innerhalb des Plots
     * @param nativePlot Das originale Plot-Objekt (TownBlock, Claim, etc.)
     */
    public Plot(UUID uuid, String identifier, Location location, Object nativePlot) {
        this.uuid = uuid;
        this.identifier = identifier;
        this.location = location;
        this.nativePlot = nativePlot;
    }
    
    /**
     * @return Eindeutige UUID des Plots
     */
    public UUID getUuid() {
        return uuid;
    }
    
    /**
     * @return Lesbarer Identifier
     */
    public String getIdentifier() {
        return identifier;
    }
    
    /**
     * @return Eine Location innerhalb des Plots
     */
    public Location getLocation() {
        return location;
    }
    
    /**
     * Gibt das originale Plot-Objekt zurück.
     * Sollte nur von Provider-Implementierungen verwendet werden.
     * 
     * @param <T> Typ des nativen Plot-Objekts
     * @return Das native Plot-Objekt
     */
    @SuppressWarnings("unchecked")
    public <T> T getNativePlot() {
        return (T) nativePlot;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Plot plot = (Plot) o;
        return uuid.equals(plot.uuid);
    }
    
    @Override
    public int hashCode() {
        return uuid.hashCode();
    }
}
