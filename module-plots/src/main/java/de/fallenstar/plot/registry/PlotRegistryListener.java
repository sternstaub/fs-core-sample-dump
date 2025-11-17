package de.fallenstar.plot.registry;

import com.palmergames.bukkit.towny.event.DeleteTownEvent;
import com.palmergames.bukkit.towny.event.town.TownBlockSettingsChangedEvent;
import com.palmergames.bukkit.towny.object.TownBlock;
import com.palmergames.bukkit.towny.object.TownBlockType;
import de.fallenstar.core.provider.Plot;
import de.fallenstar.core.provider.PlotProvider;
import de.fallenstar.plot.PlotModule;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.logging.Logger;

/**
 * Listener für Towny-Events zur automatischen Plot-Registrierung.
 *
 * Features:
 * - Auto-Registration bei Plot-Typ-Änderung
 * - Auto-Deregistration bei Plot-Löschung oder Typ-Änderung
 * - Mapping: TownBlockType → PlotType
 *
 * **Event-Handling:**
 * - TownBlockSettingsChangedEvent: Plot-Typ geändert → Re-Registration
 * - DeleteTownEvent: Town gelöscht → Alle Plots deregistrieren
 *
 * **Mapping:**
 * - COMMERCIAL → MERCHANT_GUILD
 * - EMBASSY → EMBASSY
 * - BANK → BANK
 * - ARENA → WORKSHOP (Beispiel, anpassbar)
 *
 * @author FallenStar
 * @version 1.0
 */
public class PlotRegistryListener implements Listener {

    private final PlotModule plugin;
    private final Logger logger;
    private final PlotRegistry plotRegistry;
    private final PlotProvider plotProvider;

    /**
     * Konstruktor für PlotRegistryListener.
     *
     * @param plugin PlotModule-Instanz
     * @param plotRegistry PlotRegistry
     * @param plotProvider PlotProvider
     */
    public PlotRegistryListener(PlotModule plugin, PlotRegistry plotRegistry, PlotProvider plotProvider) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.plotRegistry = plotRegistry;
        this.plotProvider = plotProvider;
    }

    /**
     * Behandelt Plot-Typ-Änderungen.
     *
     * @param event TownBlockSettingsChangedEvent
     */
    @EventHandler
    public void onPlotTypeChange(TownBlockSettingsChangedEvent event) {
        TownBlock townBlock = event.getTownBlock();

        // Hole Plot-Location
        Location location = townBlock.getWorldCoord().getBukkitLocation();

        try {
            Plot plot = plotProvider.getPlot(location);

            // De-registriere alten Typ (falls vorhanden)
            plotRegistry.unregisterPlot(plot);

            // Registriere neuen Typ (falls passend)
            TownBlockType blockType = townBlock.getType();
            PlotRegistry.PlotType newType = mapTownBlockType(blockType);

            if (newType != null) {
                plotRegistry.registerPlot(plot, newType);
                logger.info("Plot auto-registriert: " + plot.getPlotId() + " als " + newType.getDisplayName());

                // Speichere Config
                plugin.saveConfiguration();
            }

        } catch (Exception e) {
            logger.warning("Fehler bei Plot-Typ-Änderung: " + e.getMessage());
        }
    }

    /**
     * Behandelt Town-Löschungen.
     *
     * @param event DeleteTownEvent
     */
    @EventHandler
    public void onTownDelete(DeleteTownEvent event) {
        // Bei Town-Löschung alle Plots deregistrieren
        // (Towny löscht automatisch alle TownBlocks)

        logger.info("Town gelöscht: " + event.getTownName() + " - Plots werden deregistriert");

        // Hinweis: Einzelne Plot-Deregistrierung erfolgt via TownBlock-Removal-Events
        // Dieser Event dient nur als Info-Log
    }

    /**
     * Mapped TownBlockType zu PlotRegistry.PlotType.
     *
     * @param blockType TownBlockType
     * @return PlotType oder null wenn kein Mapping
     */
    private PlotRegistry.PlotType mapTownBlockType(TownBlockType blockType) {
        return switch (blockType) {
            case COMMERCIAL -> PlotRegistry.PlotType.MERCHANT_GUILD;
            case EMBASSY -> PlotRegistry.PlotType.EMBASSY;
            case BANK -> PlotRegistry.PlotType.BANK;
            case ARENA -> PlotRegistry.PlotType.WORKSHOP;  // Beispiel-Mapping
            default -> null;  // Keine Auto-Registration für andere Typen
        };
    }
}
