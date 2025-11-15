package de.fallenstar.core.provider.impl;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;
import com.palmergames.bukkit.towny.object.TownBlockType;
import de.fallenstar.core.exception.ProviderFunctionalityNotFoundException;
import de.fallenstar.core.provider.Plot;
import de.fallenstar.core.provider.PlotProvider;
import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * Towny-Implementation des PlotProviders.
 * 
 * Wrapping von Towny-API Calls in unser Provider-Interface.
 * 
 * Dependencies:
 * - Towny (https://github.com/TownyAdvanced/Towny)
 * 
 * @author FallenStar
 * @version 1.0
 */
public class TownyPlotProvider implements PlotProvider {
    
    private final TownyAPI townyAPI;
    
    public TownyPlotProvider() {
        this.townyAPI = TownyAPI.getInstance();
    }
    
    @Override
    public boolean isAvailable() {
        return townyAPI != null;
    }
    
    @Override
    public Plot getPlot(Location location) throws ProviderFunctionalityNotFoundException {
        if (!isAvailable()) {
            throw new ProviderFunctionalityNotFoundException("TownyPlotProvider", 
                "getPlot", "Towny not available");
        }
        
        TownBlock townBlock = townyAPI.getTownBlock(location);
        if (townBlock == null) {
            return null; // Keine Exception - Plot kann legitimerweise null sein
        }
        
        // Wrapper erstellen
        Town town = townBlock.getTownOrNull();
        String identifier = town != null 
            ? town.getName() + "_" + townBlock.getX() + "_" + townBlock.getZ()
            : "unknown_" + townBlock.getX() + "_" + townBlock.getZ();
        
        return new Plot(
            townBlock.getUUID(),
            identifier,
            location,
            townBlock
        );
    }
    
    @Override
    public boolean canBuild(Player player, Location location) 
            throws ProviderFunctionalityNotFoundException {
        if (!isAvailable()) {
            throw new ProviderFunctionalityNotFoundException("TownyPlotProvider",
                "canBuild", "Towny not available");
        }
        
        // Towny hat eigene Permission-Checks
        return townyAPI.isWilderness(location) 
            || townyAPI.getTownBlock(location).getPermissions()
                .getResidentPerm(player.getName()).canBuild();
    }
    
    @Override
    public String getOwnerName(Plot plot) throws ProviderFunctionalityNotFoundException {
        TownBlock townBlock = plot.getNativePlot();
        Town town = townBlock.getTownOrNull();
        return town != null ? town.getName() : "Wilderness";
    }
    
    @Override
    public String getPlotType(Plot plot) throws ProviderFunctionalityNotFoundException {
        TownBlock townBlock = plot.getNativePlot();
        TownBlockType type = townBlock.getType();
        
        // Towny types zu unseren types mappen
        return type != null ? type.getName().toLowerCase() : null;
    }
    
    @Override
    public boolean hasAdminRights(Player player, Plot plot) 
            throws ProviderFunctionalityNotFoundException {
        TownBlock townBlock = plot.getNativePlot();
        Town town = townBlock.getTownOrNull();
        
        if (town == null) return false;
        
        // Mayor oder Assistant?
        return town.hasResident(player.getName()) 
            && (town.isMayor(townyAPI.getResident(player.getUniqueId()))
                || town.hasAssistant(townyAPI.getResident(player.getUniqueId())));
    }
}
