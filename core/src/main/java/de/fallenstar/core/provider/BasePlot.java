package de.fallenstar.core.provider;

import org.bukkit.Location;
import java.util.UUID;

/**
 * Basis-Implementierung des Plot-Interfaces.
 *
 * Wrapper-Klasse die konkrete Plot-Implementierungen verbirgt.
 *
 * Diese Klasse ermöglicht es, mit Plots zu arbeiten ohne die
 * konkrete Implementierung (Towny, Factions, etc.) zu kennen.
 *
 * **Verwendung:**
 * - Für einfache Plots ohne zusätzliche Traits
 * - Als Basis-Klasse für erweiterte Plot-Typen
 *
 * **Erweiterte Plot-Typen:**
 * Nutzen BasePlot als Basis und implementieren zusätzliche Traits:
 * <pre>
 * class TradeguildPlot extends BasePlot implements NamedPlot,
 *                                                   StorageContainerPlot {
 *     // ...
 * }
 * </pre>
 *
 * @author FallenStar
 * @version 1.0
 */
public class BasePlot implements Plot {
    
    private final UUID uuid;
    private final String identifier;
    private final Location location;
    private final Object nativePlot; // TownBlock, Claim, etc.
    
    /**
     * Erstellt ein neues BasePlot-Objekt.
     *
     * @param uuid Eindeutige ID des Plots
     * @param identifier Lesbarer Identifier (z.B. "TownName_PlotID")
     * @param location Eine Location innerhalb des Plots
     * @param nativePlot Das originale Plot-Objekt (TownBlock, Claim, etc.)
     */
    public BasePlot(UUID uuid, String identifier, Location location, Object nativePlot) {
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
        if (!(o instanceof Plot plot)) return false;
        return uuid.equals(plot.getUuid());
    }
    
    @Override
    public int hashCode() {
        return uuid.hashCode();
    }
}
