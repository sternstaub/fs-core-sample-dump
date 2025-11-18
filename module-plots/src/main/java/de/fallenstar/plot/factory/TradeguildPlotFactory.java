package de.fallenstar.plot.factory;

import de.fallenstar.core.database.DataStore;
import de.fallenstar.core.provider.BasePlot;
import de.fallenstar.core.provider.Plot;
import de.fallenstar.plot.model.TradeguildPlot;
import de.fallenstar.plot.model.TradeguildPlotData;
import org.bukkit.Location;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * Factory für TradeguildPlot-Instanzen mit DataStore-Integration.
 *
 * **Konzept:**
 * - Erstellt TradeguildPlot aus BasePlot
 * - Verwaltet Cache aller Instanzen
 * - Lazy Loading (Instanz nur wenn benötigt)
 * - Persistierung über DataStore (SQLite/MySQL)
 *
 * **Verwendung:**
 * <pre>
 * TradeguildPlotFactory factory = new TradeguildPlotFactory(logger, dataStore);
 *
 * // Plot erstellen/laden
 * Plot basePlot = plotProvider.getPlot(location);
 * TradeguildPlot tradePlot = factory.createOrGet(basePlot);
 *
 * // Daten laden (async)
 * factory.loadPlotData(tradePlot).thenAccept(loaded -> {
 *     if (loaded) {
 *         logger.info("Plot-Daten geladen");
 *     }
 * });
 *
 * // Daten speichern (async)
 * factory.savePlotData(tradePlot).thenAccept(saved -> {
 *     if (saved) {
 *         logger.info("Plot-Daten gespeichert");
 *     }
 * });
 * </pre>
 *
 * **Namespace:** `tradeguild_plots`
 * **Key-Format:** `plot_<identifier>`
 *
 * @author FallenStar
 * @version 2.0
 */
public class TradeguildPlotFactory {

    private static final String NAMESPACE = "tradeguild_plots";
    private static final String KEY_PREFIX = "plot_";

    private final Logger logger;
    private final DataStore dataStore;
    private final Map<UUID, TradeguildPlot> plotCache = new ConcurrentHashMap<>();

    /**
     * Erstellt eine TradeguildPlotFactory.
     *
     * @param logger Logger
     * @param dataStore DataStore für Persistierung
     */
    public TradeguildPlotFactory(Logger logger, DataStore dataStore) {
        this.logger = logger;
        this.dataStore = dataStore;
    }

    /**
     * Erstellt oder holt eine TradeguildPlot-Instanz.
     *
     * Verwendet Cache um Instanzen wiederzuverwenden.
     * Lädt automatisch gespeicherte Daten aus DataStore (Lazy Loading).
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

        // Versuche Daten zu laden (synchron für Lazy Loading)
        boolean loaded = loadPlotDataSync(tradePlot);
        if (loaded) {
            logger.fine("TradeguildPlot geladen aus DataStore: " + basePlot.getIdentifier());
        } else {
            logger.fine("TradeguildPlot erstellt (keine gespeicherten Daten): " + basePlot.getIdentifier());
        }

        // Cache Instanz
        plotCache.put(uuid, tradePlot);

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

    // ========== Persistenz ==========

    /**
     * Speichert Plot-Daten asynchron.
     *
     * Exportiert alle TradeguildPlot-Daten und speichert sie im DataStore.
     *
     * @param plot Der zu speichernde Plot
     * @return CompletableFuture<Boolean> - true wenn erfolgreich
     */
    public CompletableFuture<Boolean> savePlotData(TradeguildPlot plot) {
        if (plot == null) {
            return CompletableFuture.completedFuture(false);
        }

        String key = getKey(plot.getIdentifier());
        TradeguildPlotData data = plot.exportData();

        return dataStore.save(NAMESPACE, key, data)
                .thenApply(success -> {
                    if (success) {
                        logger.fine("TradeguildPlot gespeichert: " + plot.getIdentifier());
                    } else {
                        logger.warning("Fehler beim Speichern von TradeguildPlot: " + plot.getIdentifier());
                    }
                    return success;
                })
                .exceptionally(ex -> {
                    logger.severe("Fehler beim Speichern von TradeguildPlot: " + plot.getIdentifier());
                    ex.printStackTrace();
                    return false;
                });
    }

    /**
     * Lädt Plot-Daten asynchron.
     *
     * Lädt TradeguildPlotData aus dem DataStore und importiert sie in den Plot.
     *
     * @param plot Der zu ladende Plot
     * @return CompletableFuture<Boolean> - true wenn erfolgreich
     */
    public CompletableFuture<Boolean> loadPlotData(TradeguildPlot plot) {
        if (plot == null) {
            return CompletableFuture.completedFuture(false);
        }

        String key = getKey(plot.getIdentifier());

        return dataStore.load(NAMESPACE, key, TradeguildPlotData.class)
                .thenApply(dataOpt -> {
                    if (dataOpt.isPresent()) {
                        plot.importData(dataOpt.get());
                        logger.fine("TradeguildPlot geladen: " + plot.getIdentifier());
                        return true;
                    } else {
                        logger.fine("Keine gespeicherten Daten für TradeguildPlot: " + plot.getIdentifier());
                        return false;
                    }
                })
                .exceptionally(ex -> {
                    logger.severe("Fehler beim Laden von TradeguildPlot: " + plot.getIdentifier());
                    ex.printStackTrace();
                    return false;
                });
    }

    /**
     * Speichert Plot-Daten synchron.
     *
     * NUR für Server-Shutdown verwenden!
     *
     * @param plot Der zu speichernde Plot
     * @return true wenn erfolgreich
     */
    public boolean savePlotDataSync(TradeguildPlot plot) {
        if (plot == null) {
            return false;
        }

        String key = getKey(plot.getIdentifier());
        TradeguildPlotData data = plot.exportData();

        boolean success = dataStore.saveSync(NAMESPACE, key, data);

        if (success) {
            logger.fine("TradeguildPlot synchron gespeichert: " + plot.getIdentifier());
        } else {
            logger.warning("Fehler beim synchronen Speichern von TradeguildPlot: " + plot.getIdentifier());
        }

        return success;
    }

    /**
     * Lädt Plot-Daten synchron.
     *
     * NUR für Server-Startup verwenden!
     *
     * @param plot Der zu ladende Plot
     * @return true wenn erfolgreich
     */
    public boolean loadPlotDataSync(TradeguildPlot plot) {
        if (plot == null) {
            return false;
        }

        String key = getKey(plot.getIdentifier());
        Optional<TradeguildPlotData> dataOpt = dataStore.loadSync(NAMESPACE, key, TradeguildPlotData.class);

        if (dataOpt.isPresent()) {
            plot.importData(dataOpt.get());
            logger.fine("TradeguildPlot synchron geladen: " + plot.getIdentifier());
            return true;
        } else {
            logger.fine("Keine gespeicherten Daten für TradeguildPlot: " + plot.getIdentifier());
            return false;
        }
    }

    /**
     * Speichert alle gecachten Plots synchron.
     *
     * NUR für Server-Shutdown verwenden!
     *
     * @return Anzahl erfolgreich gespeicherter Plots
     */
    public int saveAllSync() {
        int saved = 0;

        for (TradeguildPlot plot : plotCache.values()) {
            if (savePlotDataSync(plot)) {
                saved++;
            }
        }

        logger.info("TradeguildPlots gespeichert: " + saved + "/" + plotCache.size());
        return saved;
    }

    /**
     * Erstellt einen DataStore-Key für einen Plot.
     *
     * @param plotIdentifier Plot-Identifier
     * @return DataStore-Key
     */
    private String getKey(String plotIdentifier) {
        return KEY_PREFIX + plotIdentifier;
    }
}
