package de.fallenstar.plot.manager;

import de.fallenstar.core.provider.ItemBasePriceProvider;
import de.fallenstar.core.provider.Plot;
import de.fallenstar.plot.model.PlotPriceData;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

import java.math.BigDecimal;
import java.util.*;
import java.util.logging.Logger;

/**
 * Manager für Plot-basierte Item-Preise.
 *
 * Implementiert {@link ItemBasePriceProvider} und verwaltet Ankauf-/Verkaufspreise
 * für Handelsgilden-Grundstücke.
 *
 * **Verwendung:**
 * ```java
 * PlotPriceManager manager = plotsModule.getPriceManager();
 *
 * // Preis setzen
 * manager.setBuyPrice(plot, Material.DIAMOND, BigDecimal.valueOf(50));
 * manager.setSellPrice(plot, Material.DIAMOND, BigDecimal.valueOf(75));
 *
 * // Preis abrufen
 * Optional<BigDecimal> buyPrice = manager.getBuyPrice(plot, Material.DIAMOND);
 * ```
 *
 * **Config-Persistierung:**
 * ```yaml
 * plot-prices:
 *   plot-uuid-123:
 *     DIAMOND:
 *       buy: 50.0
 *       sell: 75.0
 * ```
 *
 * @author FallenStar
 * @version 1.0
 */
public class PlotPriceManager implements ItemBasePriceProvider {

    private final Plugin plugin;
    private final Logger logger;

    /**
     * Plot-UUID → PlotPriceData
     */
    private final Map<UUID, PlotPriceData> plotPrices;

    /**
     * Erstellt einen neuen PlotPriceManager.
     *
     * @param plugin Plugin-Instanz
     * @param logger Logger
     */
    public PlotPriceManager(Plugin plugin, Logger logger) {
        this.plugin = Objects.requireNonNull(plugin, "plugin cannot be null");
        this.logger = Objects.requireNonNull(logger, "logger cannot be null");
        this.plotPrices = new HashMap<>();
    }

    // ==================== ItemBasePriceProvider Interface ====================

    @Override
    public Optional<BigDecimal> getBuyPrice(Plot plot, Material material) {
        Objects.requireNonNull(plot, "plot cannot be null");
        Objects.requireNonNull(material, "material cannot be null");

        PlotPriceData data = plotPrices.get(plot.getUuid());
        return data != null ? data.getBuyPrice(material) : Optional.empty();
    }

    @Override
    public Optional<BigDecimal> getSellPrice(Plot plot, Material material) {
        Objects.requireNonNull(plot, "plot cannot be null");
        Objects.requireNonNull(material, "material cannot be null");

        PlotPriceData data = plotPrices.get(plot.getUuid());
        return data != null ? data.getSellPrice(material) : Optional.empty();
    }

    @Override
    public void setBuyPrice(Plot plot, Material material, BigDecimal price) {
        Objects.requireNonNull(plot, "plot cannot be null");
        Objects.requireNonNull(material, "material cannot be null");

        PlotPriceData data = plotPrices.computeIfAbsent(plot.getUuid(), PlotPriceData::new);
        data.setBuyPrice(material, price);

        // Wenn Plot keine Preise mehr hat, entferne Eintrag
        if (data.isEmpty()) {
            plotPrices.remove(plot.getUuid());
        }

        logger.fine("Set buy price for " + material + " on plot " + plot.getUuid() + ": " + price);
    }

    @Override
    public void setSellPrice(Plot plot, Material material, BigDecimal price) {
        Objects.requireNonNull(plot, "plot cannot be null");
        Objects.requireNonNull(material, "material cannot be null");

        PlotPriceData data = plotPrices.computeIfAbsent(plot.getUuid(), PlotPriceData::new);
        data.setSellPrice(material, price);

        // Wenn Plot keine Preise mehr hat, entferne Eintrag
        if (data.isEmpty()) {
            plotPrices.remove(plot.getUuid());
        }

        logger.fine("Set sell price for " + material + " on plot " + plot.getUuid() + ": " + price);
    }

    @Override
    public boolean hasPrices(Plot plot, Material material) {
        Objects.requireNonNull(plot, "plot cannot be null");
        Objects.requireNonNull(material, "material cannot be null");

        PlotPriceData data = plotPrices.get(plot.getUuid());
        return data != null && data.hasPrices(material);
    }

    @Override
    public void removePrices(Plot plot, Material material) {
        Objects.requireNonNull(plot, "plot cannot be null");
        Objects.requireNonNull(material, "material cannot be null");

        PlotPriceData data = plotPrices.get(plot.getUuid());
        if (data != null) {
            data.removePrices(material);

            // Wenn Plot keine Preise mehr hat, entferne Eintrag
            if (data.isEmpty()) {
                plotPrices.remove(plot.getUuid());
            }

            logger.fine("Removed prices for " + material + " on plot " + plot.getUuid());
        }
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    // ==================== Zusätzliche Manager-Methoden ====================

    /**
     * Gibt alle Materialien mit Preisen für ein Plot zurück.
     *
     * @param plot Das Plot
     * @return Set aller Materialien
     */
    public Set<Material> getMaterialsForPlot(Plot plot) {
        Objects.requireNonNull(plot, "plot cannot be null");

        PlotPriceData data = plotPrices.get(plot.getUuid());
        return data != null ? data.getMaterials() : Collections.emptySet();
    }

    /**
     * Gibt die PlotPriceData für ein Plot zurück.
     *
     * @param plot Das Plot
     * @return PlotPriceData oder Empty
     */
    public Optional<PlotPriceData> getPriceData(Plot plot) {
        Objects.requireNonNull(plot, "plot cannot be null");
        return Optional.ofNullable(plotPrices.get(plot.getUuid()));
    }

    /**
     * Entfernt alle Preise für ein Plot.
     *
     * @param plot Das Plot
     */
    public void clearPlotPrices(Plot plot) {
        Objects.requireNonNull(plot, "plot cannot be null");

        plotPrices.remove(plot.getUuid());
        logger.info("Cleared all prices for plot " + plot.getUuid());
    }

    /**
     * Gibt die Anzahl der Plots mit Preisen zurück.
     *
     * @return Anzahl
     */
    public int getPlotCount() {
        return plotPrices.size();
    }

    // ==================== Config-Persistierung ====================

    /**
     * Lädt Plot-Preise aus der Config.
     *
     * @param config Die FileConfiguration
     */
    public void loadFromConfig(FileConfiguration config) {
        plotPrices.clear();

        ConfigurationSection section = config.getConfigurationSection("plot-prices");
        if (section == null) {
            logger.info("No plot prices found in config");
            return;
        }

        int loaded = 0;
        for (String plotIdStr : section.getKeys(false)) {
            try {
                UUID plotId = UUID.fromString(plotIdStr);
                PlotPriceData data = new PlotPriceData(plotId);

                ConfigurationSection plotSection = section.getConfigurationSection(plotIdStr);
                if (plotSection != null) {
                    data.loadFromConfig(plotSection);

                    if (!data.isEmpty()) {
                        plotPrices.put(plotId, data);
                        loaded++;
                    }
                }

            } catch (IllegalArgumentException e) {
                logger.warning("Invalid plot UUID in config: " + plotIdStr);
            }
        }

        logger.info("Loaded prices for " + loaded + " plots");
    }

    /**
     * Speichert Plot-Preise in die Config.
     *
     * @param config Die FileConfiguration
     */
    public void saveToConfig(FileConfiguration config) {
        // Clear alte Daten
        config.set("plot-prices", null);

        if (plotPrices.isEmpty()) {
            return;
        }

        // Speichere Preise
        for (Map.Entry<UUID, PlotPriceData> entry : plotPrices.entrySet()) {
            String plotIdStr = entry.getKey().toString();
            PlotPriceData data = entry.getValue();

            ConfigurationSection plotSection = config.createSection("plot-prices." + plotIdStr);
            data.saveToConfig(plotSection);
        }

        logger.fine("Saved prices for " + plotPrices.size() + " plots");
    }
}
