package de.fallenstar.plot.registry;

import com.palmergames.bukkit.towny.event.DeleteTownEvent;
import de.fallenstar.plot.PlotModule;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.logging.Logger;

/**
 * Listener für Towny-Events zur Plot-Registrierung.
 *
 * **HINWEIS:**
 * Auto-Registration via TownBlockSettingsChangedEvent wurde deaktiviert,
 * da dieses Event nicht in allen Towny-Versionen verfügbar ist.
 *
 * **Alternative:**
 * - Manuelle Registration via `/plot registry register <type>` Command
 * - Nur DeleteTownEvent wird behandelt (Info-Log)
 *
 * Features:
 * - Info-Log bei Town-Löschung
 * - Manuelle Plot-Registrierung via Commands
 *
 * @author FallenStar
 * @version 1.0
 */
public class PlotRegistryListener implements Listener {

    private final PlotModule plugin;
    private final Logger logger;
    private final PlotRegistry plotRegistry;

    /**
     * Konstruktor für PlotRegistryListener.
     *
     * @param plugin PlotModule-Instanz
     * @param plotRegistry PlotRegistry
     */
    public PlotRegistryListener(PlotModule plugin, PlotRegistry plotRegistry) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.plotRegistry = plotRegistry;
    }

    /**
     * Behandelt Town-Löschungen.
     *
     * Bei Town-Löschung werden alle Plots automatisch
     * von Towny entfernt. Dies dient nur als Info-Log.
     *
     * @param event DeleteTownEvent
     */
    @EventHandler
    public void onTownDelete(DeleteTownEvent event) {
        logger.info("Town gelöscht: " + event.getTownName());
        logger.info("Hinweis: Plots dieser Town sollten manuell deregistriert werden");

        // TODO: Implementiere automatische Deregistrierung aller Plots dieser Town
        // Benötigt Mapping: Town-Name → Plot-IDs
    }

    // NOTE: Auto-Registration via TownBlockSettingsChangedEvent wurde entfernt
    // Grund: Event existiert nicht in allen Towny-Versionen (API-Inkompatibilität)
    //
    // Alternative:
    // 1. Manuelle Registration via /plot registry register <type>
    // 2. Oder: Implementiere eigenes Event-Handling basierend auf verfügbarer Towny-Version
}
