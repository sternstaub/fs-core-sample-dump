package de.fallenstar.plot.provider;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;
import com.palmergames.bukkit.towny.object.TownBlockType;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.utils.PlayerCacheUtil;
import de.fallenstar.core.exception.ProviderFunctionalityNotFoundException;
import de.fallenstar.core.provider.Plot;
import de.fallenstar.core.provider.PlotProvider;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Towny-Integration für PlotProvider.
 *
 * Nutzt die Towny-API um Grundstücks-Informationen bereitzustellen.
 *
 * Implementierung:
 * - TownBlocks werden als Plots behandelt
 * - Plot-UUID wird aus Town + Koordinaten generiert
 * - Plot-Typen: default, shop, embassy, arena, etc.
 * - Admin-Rechte: Mayor, Assistants
 *
 * @author FallenStar
 * @version 1.0
 */
public class TownyPlotProvider implements PlotProvider {

    private final TownyAPI townyAPI;

    /**
     * Erstellt einen neuen TownyPlotProvider.
     */
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
            throw new ProviderFunctionalityNotFoundException(
                "PlotProvider", "getPlot", "Towny API not available"
            );
        }

        if (location == null) {
            return null; // Erlaubt für Null-Checks
        }

        try {
            TownBlock townBlock = townyAPI.getTownBlock(location);

            if (townBlock == null) {
                return null; // Keine Stadt an dieser Location
            }

            // Generiere UUID aus Town + Koordinaten
            UUID plotId = generatePlotUUID(townBlock);

            // Generiere lesbaren Identifier
            String identifier = generatePlotIdentifier(townBlock);

            // Erstelle Plot-Objekt (nativePlot = TownBlock)
            return new Plot(plotId, identifier, location, townBlock);

        } catch (Exception e) {
            throw new ProviderFunctionalityNotFoundException(
                "PlotProvider", "getPlot",
                "Error accessing Towny: " + e.getMessage()
            );
        }
    }

    @Override
    public boolean canBuild(Player player, Location location)
            throws ProviderFunctionalityNotFoundException {
        if (!isAvailable()) {
            throw new ProviderFunctionalityNotFoundException(
                "PlotProvider", "canBuild", "Towny API not available"
            );
        }

        try {
            // Nutze Townys eigene Permission-Prüfung
            return PlayerCacheUtil.getCachePermission(
                player,
                location,
                location.getBlock().getType(),
                com.palmergames.bukkit.towny.object.TownyPermission.ActionType.BUILD
            );

        } catch (Exception e) {
            throw new ProviderFunctionalityNotFoundException(
                "PlotProvider", "canBuild",
                "Error checking build permission: " + e.getMessage()
            );
        }
    }

    @Override
    public String getOwnerName(Plot plot) throws ProviderFunctionalityNotFoundException {
        if (!isAvailable()) {
            throw new ProviderFunctionalityNotFoundException(
                "PlotProvider", "getOwnerName", "Towny API not available"
            );
        }

        try {
            TownBlock townBlock = plot.getNativePlot();

            if (townBlock == null) {
                return "Unknown";
            }

            // Prüfe ob Plot einen Resident-Owner hat
            if (townBlock.hasResident()) {
                Resident resident = townBlock.getResident();
                return resident.getName();
            }

            // Sonst gehört es der Stadt
            Town town = townBlock.getTownOrNull();
            if (town != null) {
                return town.getName();
            }

            return "Unknown";

        } catch (Exception e) {
            throw new ProviderFunctionalityNotFoundException(
                "PlotProvider", "getOwnerName",
                "Error getting owner: " + e.getMessage()
            );
        }
    }

    @Override
    public String getPlotType(Plot plot) throws ProviderFunctionalityNotFoundException {
        if (!isAvailable()) {
            throw new ProviderFunctionalityNotFoundException(
                "PlotProvider", "getPlotType", "Towny API not available"
            );
        }

        try {
            TownBlock townBlock = plot.getNativePlot();

            if (townBlock == null) {
                return "unknown";
            }

            // Hole Plot-Typ (default, shop, embassy, arena, etc.)
            TownBlockType type = townBlock.getType();
            return type.getName().toLowerCase();

        } catch (Exception e) {
            throw new ProviderFunctionalityNotFoundException(
                "PlotProvider", "getPlotType",
                "Error getting plot type: " + e.getMessage()
            );
        }
    }

    @Override
    public boolean hasAdminRights(Player player, Plot plot)
            throws ProviderFunctionalityNotFoundException {
        if (!isAvailable()) {
            throw new ProviderFunctionalityNotFoundException(
                "PlotProvider", "hasAdminRights", "Towny API not available"
            );
        }

        try {
            TownBlock townBlock = plot.getNativePlot();

            if (townBlock == null) {
                return false;
            }

            Town town = townBlock.getTownOrNull();
            if (town == null) {
                return false;
            }

            // Hole Resident des Spielers
            Resident resident = townyAPI.getResident(player.getUniqueId());
            if (resident == null) {
                return false;
            }

            // Prüfe ob Mayor
            if (town.isMayor(resident)) {
                return true;
            }

            // Prüfe ob Assistant (Deputy)
            if (resident.hasNationRank("assistant") || resident.hasTownRank("assistant")) {
                return true;
            }

            // Prüfe ob Plot-Owner
            if (townBlock.hasResident() && townBlock.getResident().equals(resident)) {
                return true;
            }

            return false;

        } catch (Exception e) {
            throw new ProviderFunctionalityNotFoundException(
                "PlotProvider", "hasAdminRights",
                "Error checking admin rights: " + e.getMessage()
            );
        }
    }

    @Override
    public boolean isOwner(Plot plot, Player player)
            throws ProviderFunctionalityNotFoundException {
        if (!isAvailable()) {
            throw new ProviderFunctionalityNotFoundException(
                "PlotProvider", "isOwner", "Towny API not available"
            );
        }

        try {
            TownBlock townBlock = plot.getNativePlot();

            if (townBlock == null) {
                return false;
            }

            // Hole Resident des Spielers
            Resident resident = townyAPI.getResident(player.getUniqueId());
            if (resident == null) {
                return false;
            }

            // Prüfe ob Spieler der Resident-Owner des TownBlocks ist
            if (townBlock.hasResident()) {
                return townBlock.getResident().equals(resident);
            }

            // Wenn kein Resident-Owner: Prüfe ob Spieler der Mayor ist
            Town town = townBlock.getTownOrNull();
            if (town != null && town.isMayor(resident)) {
                return true;
            }

            return false;

        } catch (Exception e) {
            throw new ProviderFunctionalityNotFoundException(
                "PlotProvider", "isOwner",
                "Error checking ownership: " + e.getMessage()
            );
        }
    }

    /**
     * Generiert eine eindeutige UUID für ein Plot.
     *
     * Basiert auf Town-Name + Koordinaten für Konsistenz.
     *
     * @param townBlock Der TownBlock
     * @return Eindeutige UUID
     */
    private UUID generatePlotUUID(TownBlock townBlock) {
        Town town = townBlock.getTownOrNull();
        String townName = (town != null) ? town.getName() : "unknown";

        String uniqueString = townName + "_" +
                             townBlock.getX() + "_" +
                             townBlock.getZ() + "_" +
                             townBlock.getWorldCoord().getWorldName();

        return UUID.nameUUIDFromBytes(uniqueString.getBytes());
    }

    /**
     * Generiert einen lesbaren Identifier für ein Plot.
     *
     * Format: "TownName (x,z)"
     *
     * @param townBlock Der TownBlock
     * @return Lesbarer Identifier
     */
    private String generatePlotIdentifier(TownBlock townBlock) {
        Town town = townBlock.getTownOrNull();
        String townName = (town != null) ? town.getName() : "Unknown";

        return townName + " (" + townBlock.getX() + "," + townBlock.getZ() + ")";
    }

    @Override
    public List<UUID> getAssociates(Plot plot)
            throws ProviderFunctionalityNotFoundException {
        if (!isAvailable()) {
            throw new ProviderFunctionalityNotFoundException(
                "PlotProvider", "getAssociates", "Towny API not available"
            );
        }

        List<UUID> associates = new ArrayList<>();

        try {
            TownBlock townBlock = plot.getNativePlot();

            if (townBlock == null) {
                return associates; // Leere Liste
            }

            // Hole die Town des Plots
            Town town = townBlock.getTownOrNull();
            if (town == null) {
                return associates; // Keine Stadt = keine Bewohner
            }

            // Hole alle Residents der Town
            for (Resident resident : town.getResidents()) {
                UUID playerUUID = resident.getUUID();
                if (playerUUID != null) {
                    associates.add(playerUUID);
                }
            }

            return associates;

        } catch (Exception e) {
            throw new ProviderFunctionalityNotFoundException(
                "PlotProvider", "getAssociates",
                "Error getting town residents: " + e.getMessage()
            );
        }
    }

    @Override
    public List<Plot> getPlayerPlots(UUID playerUUID)
            throws ProviderFunctionalityNotFoundException {
        if (!isAvailable()) {
            throw new ProviderFunctionalityNotFoundException(
                "PlotProvider", "getPlayerPlots", "Towny API not available"
            );
        }

        List<Plot> playerPlots = new ArrayList<>();

        try {
            // Hole Resident des Spielers
            Resident resident = townyAPI.getResident(playerUUID);
            if (resident == null) {
                return playerPlots; // Spieler hat keine Grundstücke
            }

            // Hole alle TownBlocks des Residents
            for (TownBlock townBlock : resident.getTownBlocks()) {
                // Generiere UUID und Identifier
                UUID plotId = generatePlotUUID(townBlock);
                String identifier = generatePlotIdentifier(townBlock);

                // Hole World
                org.bukkit.World world = org.bukkit.Bukkit.getWorld(townBlock.getWorldCoord().getWorldName());
                if (world == null) {
                    continue; // Skip wenn Welt nicht geladen
                }

                // Erstelle Plot-Objekt
                Plot plot = new Plot(
                    plotId,
                    identifier,
                    world.getBlockAt(
                        townBlock.getX() * 16,
                        64,
                        townBlock.getZ() * 16
                    ).getLocation(),
                    townBlock
                );

                playerPlots.add(plot);
            }

            return playerPlots;

        } catch (Exception e) {
            throw new ProviderFunctionalityNotFoundException(
                "PlotProvider", "getPlayerPlots",
                "Error getting player plots: " + e.getMessage()
            );
        }
    }
}
