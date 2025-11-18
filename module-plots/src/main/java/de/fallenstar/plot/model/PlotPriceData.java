package de.fallenstar.plot.model;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import java.math.BigDecimal;
import java.util.*;

/**
 * Speichert Ankauf- und Verkaufspreise für Items auf einem Grundstück.
 *
 * Diese Klasse wird für Handelsgilden verwendet, um festzulegen, zu welchen
 * Preisen NPCs Items vom Spieler kaufen bzw. an Spieler verkaufen.
 *
 * **Konzept:**
 * - **Ankaufpreis (Buy Price)**: NPC kauft Item vom Spieler
 * - **Verkaufspreis (Sell Price)**: Spieler kauft Item vom NPC
 *
 * **Persistierung:**
 * ```yaml
 * plot-prices:
 *   plot-uuid-123:
 *     DIAMOND:
 *       buy: 50.0
 *       sell: 75.0
 *     IRON_INGOT:
 *       buy: 1.5
 *       sell: 2.0
 * ```
 *
 * @author FallenStar
 * @version 1.0
 */
public class PlotPriceData {

    /**
     * Interner Container für Preis-Paar (Ankauf, Verkauf).
     */
    public record PricePair(BigDecimal buyPrice, BigDecimal sellPrice) {
        /**
         * Prüft ob ein Ankaufpreis festgelegt ist.
         */
        public boolean hasBuyPrice() {
            return buyPrice != null;
        }

        /**
         * Prüft ob ein Verkaufspreis festgelegt ist.
         */
        public boolean hasSellPrice() {
            return sellPrice != null;
        }

        /**
         * Prüft ob mindestens ein Preis festgelegt ist.
         */
        public boolean hasAnyPrice() {
            return hasBuyPrice() || hasSellPrice();
        }
    }

    private final UUID plotId;
    private final Map<Material, PricePair> prices;

    /**
     * Erstellt eine neue PlotPriceData für ein Grundstück.
     *
     * @param plotId UUID des Grundstücks
     */
    public PlotPriceData(UUID plotId) {
        this.plotId = Objects.requireNonNull(plotId, "plotId cannot be null");
        this.prices = new HashMap<>();
    }

    /**
     * Gibt die Plot-UUID zurück.
     *
     * @return Plot-UUID
     */
    public UUID getPlotId() {
        return plotId;
    }

    /**
     * Setzt den Ankaufpreis für ein Material.
     *
     * @param material Das Material
     * @param buyPrice Der Preis (null zum Entfernen)
     */
    public void setBuyPrice(Material material, BigDecimal buyPrice) {
        Objects.requireNonNull(material, "material cannot be null");

        PricePair current = prices.get(material);
        BigDecimal sellPrice = current != null ? current.sellPrice() : null;

        if (buyPrice == null && sellPrice == null) {
            // Beide Preise null → Eintrag entfernen
            prices.remove(material);
        } else {
            prices.put(material, new PricePair(buyPrice, sellPrice));
        }
    }

    /**
     * Setzt den Verkaufspreis für ein Material.
     *
     * @param material Das Material
     * @param sellPrice Der Preis (null zum Entfernen)
     */
    public void setSellPrice(Material material, BigDecimal sellPrice) {
        Objects.requireNonNull(material, "material cannot be null");

        PricePair current = prices.get(material);
        BigDecimal buyPrice = current != null ? current.buyPrice() : null;

        if (buyPrice == null && sellPrice == null) {
            // Beide Preise null → Eintrag entfernen
            prices.remove(material);
        } else {
            prices.put(material, new PricePair(buyPrice, sellPrice));
        }
    }

    /**
     * Gibt den Ankaufpreis für ein Material zurück.
     *
     * @param material Das Material
     * @return Ankaufpreis oder Empty
     */
    public Optional<BigDecimal> getBuyPrice(Material material) {
        PricePair pair = prices.get(material);
        return pair != null && pair.hasBuyPrice() ? Optional.of(pair.buyPrice()) : Optional.empty();
    }

    /**
     * Gibt den Verkaufspreis für ein Material zurück.
     *
     * @param material Das Material
     * @return Verkaufspreis oder Empty
     */
    public Optional<BigDecimal> getSellPrice(Material material) {
        PricePair pair = prices.get(material);
        return pair != null && pair.hasSellPrice() ? Optional.of(pair.sellPrice()) : Optional.empty();
    }

    /**
     * Prüft ob für ein Material Preise festgelegt sind.
     *
     * @param material Das Material
     * @return true wenn Ankauf ODER Verkauf festgelegt ist
     */
    public boolean hasPrices(Material material) {
        PricePair pair = prices.get(material);
        return pair != null && pair.hasAnyPrice();
    }

    /**
     * Entfernt alle Preise für ein Material.
     *
     * @param material Das Material
     */
    public void removePrices(Material material) {
        prices.remove(material);
    }

    /**
     * Gibt alle Materialien zurück, für die Preise festgelegt sind.
     *
     * @return Unveränderbare Menge aller Materialien
     */
    public Set<Material> getMaterials() {
        return Collections.unmodifiableSet(prices.keySet());
    }

    /**
     * Gibt die Anzahl der konfigurierten Materialien zurück.
     *
     * @return Anzahl
     */
    public int size() {
        return prices.size();
    }

    /**
     * Prüft ob Preise vorhanden sind.
     *
     * @return true wenn mindestens ein Preis festgelegt ist
     */
    public boolean isEmpty() {
        return prices.isEmpty();
    }

    /**
     * Löscht alle Preise.
     */
    public void clear() {
        prices.clear();
    }

    /**
     * Lädt Preise aus einer Config.
     *
     * @param section ConfigurationSection für dieses Plot
     */
    public void loadFromConfig(ConfigurationSection section) {
        if (section == null) {
            return;
        }

        prices.clear();

        for (String materialName : section.getKeys(false)) {
            try {
                Material material = Material.valueOf(materialName.toUpperCase());
                ConfigurationSection priceSection = section.getConfigurationSection(materialName);

                if (priceSection != null) {
                    BigDecimal buyPrice = null;
                    BigDecimal sellPrice = null;

                    if (priceSection.contains("buy")) {
                        buyPrice = BigDecimal.valueOf(priceSection.getDouble("buy"));
                    }

                    if (priceSection.contains("sell")) {
                        sellPrice = BigDecimal.valueOf(priceSection.getDouble("sell"));
                    }

                    if (buyPrice != null || sellPrice != null) {
                        prices.put(material, new PricePair(buyPrice, sellPrice));
                    }
                }

            } catch (IllegalArgumentException e) {
                // Ungültiges Material - ignorieren
            }
        }
    }

    /**
     * Speichert Preise in eine Config.
     *
     * @param section ConfigurationSection für dieses Plot
     */
    public void saveToConfig(ConfigurationSection section) {
        if (section == null) {
            return;
        }

        // Clear alte Daten
        for (String key : section.getKeys(false)) {
            section.set(key, null);
        }

        // Speichere Preise
        for (Map.Entry<Material, PricePair> entry : prices.entrySet()) {
            String materialName = entry.getKey().name();
            PricePair pair = entry.getValue();

            if (pair.hasBuyPrice()) {
                section.set(materialName + ".buy", pair.buyPrice().doubleValue());
            }

            if (pair.hasSellPrice()) {
                section.set(materialName + ".sell", pair.sellPrice().doubleValue());
            }
        }
    }

    @Override
    public String toString() {
        return "PlotPriceData{plotId=" + plotId + ", materials=" + prices.size() + "}";
    }
}
