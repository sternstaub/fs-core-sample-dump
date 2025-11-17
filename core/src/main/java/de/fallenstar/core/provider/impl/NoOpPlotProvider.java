package de.fallenstar.core.provider.impl;

import de.fallenstar.core.exception.ProviderFunctionalityNotFoundException;
import de.fallenstar.core.provider.Plot;
import de.fallenstar.core.provider.PlotProvider;
import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * NoOp (No Operation) Implementation des PlotProviders.
 * 
 * Wird verwendet wenn kein Plot-Plugin (Towny, Factions, etc.) verfügbar ist.
 * Alle Methoden werfen ProviderFunctionalityNotFoundException.
 * 
 * Pattern: Null Object Pattern kombiniert mit Exception-Handling
 * Vorteil: Kein Null-Checking nötig, explizites Fehlerhandling
 * 
 * @author FallenStar
 * @version 1.0
 */
public class NoOpPlotProvider implements PlotProvider {
    
    private static final String PROVIDER_NAME = "PlotProvider";
    private static final String REASON = "No plot plugin (Towny, Factions, etc.) available";
    
    @Override
    public boolean isAvailable() {
        return false;
    }
    
    @Override
    public Plot getPlot(Location location) throws ProviderFunctionalityNotFoundException {
        throw new ProviderFunctionalityNotFoundException(
            PROVIDER_NAME, 
            "getPlot", 
            REASON
        );
    }
    
    @Override
    public boolean canBuild(Player player, Location location) 
            throws ProviderFunctionalityNotFoundException {
        throw new ProviderFunctionalityNotFoundException(
            PROVIDER_NAME,
            "canBuild",
            REASON
        );
    }
    
    @Override
    public String getOwnerName(Plot plot) throws ProviderFunctionalityNotFoundException {
        throw new ProviderFunctionalityNotFoundException(
            PROVIDER_NAME,
            "getOwnerName",
            REASON
        );
    }
    
    @Override
    public String getPlotType(Plot plot) throws ProviderFunctionalityNotFoundException {
        throw new ProviderFunctionalityNotFoundException(
            PROVIDER_NAME,
            "getPlotType",
            REASON
        );
    }
    
    @Override
    public boolean hasAdminRights(Player player, Plot plot)
            throws ProviderFunctionalityNotFoundException {
        throw new ProviderFunctionalityNotFoundException(
            PROVIDER_NAME,
            "hasAdminRights",
            REASON
        );
    }

    @Override
    public boolean isOwner(Plot plot, Player player)
            throws ProviderFunctionalityNotFoundException {
        throw new ProviderFunctionalityNotFoundException(
            PROVIDER_NAME,
            "isOwner",
            REASON
        );
    }
}
