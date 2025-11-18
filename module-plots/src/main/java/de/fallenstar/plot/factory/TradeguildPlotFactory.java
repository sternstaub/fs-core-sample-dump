package de.fallenstar.plot.factory;

import de.fallenstar.core.provider.BasePlot;
import de.fallenstar.core.provider.Plot;
import de.fallenstar.plot.model.TradeguildPlot;
import org.bukkit.Location;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * Factory für TradeguildPlot-Instanzen.
 *
 * **Konzept:**
 * - Erstellt TradeguildPlot aus BasePlot
 * - Verwaltet Cache aller Instanzen
 * - Lazy Loading (Instanz nur wenn benötigt)
 *
 * **Verwendung:**
 * <pre>
 * TradeguildPlotFactory factory = new TradeguildPlotFactory(logger);
 *
 * Plot basePlot = plotProvider.getPlot(location);
 * TradeguildPlot tradePlot = factory.createOrGet(basePlot);
 * </pre>
 *
 * @author FallenStar
 * @version 1.0
 */
public class TradeguildPlotFactory {

    private final Logger logger;
    private final Map<UUID, TradeguildPlot> plotCache = new ConcurrentHashMap<>();

    /**
     * Erstellt eine TradeguildPlotFactory.
     *
     * @param logger Logger
     */
    public TradeguildPlotFactory(Logger logger) {
        this.logger = logger;
    }

    /**
     * Erstellt oder holt eine TradeguildPlot-Instanz.
     *
     * Verwendet Cache um Instanzen wiederzuverwenden.
     *
     * @param basePlot Basis-Plot (von PlotProvider)
     * @return TradeguildPlot-Instanz
     */
    public TradeguildPlot createOrGet(Plot basePlot) {
        UUID uuid = basePlot.getUuid();

        // Prüfe Cache
        TradeguildPlot existing = plotCache.get(uuid);
        if (existing != null) {
            return existing;
        }

        // Erstelle neue Instanz
        TradeguildPlot tradePlot = new TradeguildPlot(
            basePlot.getUuid(),
            basePlot.getIdentifier(),
            basePlot.getLocation(),
            basePlot.getNativePlot()
        );

        // Cache Instanz
        plotCache.put(uuid, tradePlot);

        logger.fine("TradeguildPlot erstellt: " + basePlot.getIdentifier());

        return tradePlot;
    }

    /**
     * Gibt eine gecachte TradeguildPlot-Instanz zurück.
     *
     * @param uuid Plot-UUID
     * @return TradeguildPlot oder null wenn nicht gecacht
     */
    public TradeguildPlot get(UUID uuid) {
        return plotCache.get(uuid);
    }

    /**
     * Entfernt eine TradeguildPlot-Instanz aus dem Cache.
     *
     * @param uuid Plot-UUID
     */
    public void remove(UUID uuid) {
        TradeguildPlot removed = plotCache.remove(uuid);
        if (removed != null) {
            logger.fine("TradeguildPlot entfernt: " + removed.getIdentifier());
        }
    }

    /**
     * Gibt die Anzahl gecachter Plots zurück.
     *
     * @return Anzahl
     */
    public int getCacheSize() {
        return plotCache.size();
    }

    /**
     * Leert den Cache.
     */
    public void clearCache() {
        plotCache.clear();
        logger.info("TradeguildPlot-Cache geleert");
    }

    /**
     * Gibt alle gecachten Plots zurück.
     *
     * @return Map von UUID zu TradeguildPlot
     */
    public Map<UUID, TradeguildPlot> getAllCached() {
        return Map.copyOf(plotCache);
    }
}
