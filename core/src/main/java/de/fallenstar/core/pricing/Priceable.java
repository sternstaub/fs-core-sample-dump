package de.fallenstar.core.pricing;

import org.bukkit.Material;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

/**
 * Interface für Preis-Verwaltung (Sprint 19 Phase 3).
 *
 * **Problem:**
 * - Nur StorageContainerPlot hat Preise (SRP-Verstoß!)
 * - Plot + Preis-Logik vermischt
 * - Nicht erweiterbar für andere Entitäten
 *
 * **Lösung:**
 * - Priceable Interface (Universelle Preis-Verwaltung)
 * - Plots, NPCs, Services können Priceable implementieren
 * - PriceManager für persistente Storage
 *
 * **SOLID-Prinzipien:**
 *
 * **Single Responsibility:**
 * - Priceable kennt NUR Preis-Getter/Setter
 * - KEINE Storage-Logik, KEINE Plot-Logik
 *
 * **Open/Closed:**
 * - Neue Priceable-Entities ohne Code-Änderung
 * - PriceManager funktioniert mit ALLEN Priceable-Implementierungen
 *
 * **Liskov Substitution:**
 * - Alle Priceable sind austauschbar
 * - PriceManager funktioniert mit Plot, NPC, Service gleich
 *
 * **Interface Segregation:**
 * - Nur Preis-relevante Methoden
 * - Kein "GodInterface" mit Storage + Trading + etc.
 *
 * **Dependency Inversion:**
 * - High-Level (PriceManager) hängt von Priceable ab
 * - NICHT von konkreten Implementierungen (StorageContainerPlot)
 *
 * **Implementierungen:**
 * - StorageContainerPlot (Plots-Modul) - Plot-Preise
 * - PriceableNpc (NPCs-Modul) - NPC-Service-Preise (geplant)
 * - PriceableService (Economy-Modul) - Generische Services (geplant)
 *
 * **Verwendung:**
 * <pre>
 * // StorageContainerPlot implements Priceable:
 * public class StorageContainerPlot extends Plot implements Priceable {
 *     private final Map&lt;Material, BigDecimal&gt; buyPrices = new HashMap&lt;&gt;();
 *     private final Map&lt;Material, BigDecimal&gt; sellPrices = new HashMap&lt;&gt;();
 *
 *     @Override
 *     public Optional&lt;BigDecimal&gt; getBuyPrice(Material material) {
 *         return Optional.ofNullable(buyPrices.get(material));
 *     }
 *
 *     @Override
 *     public void setBuyPrice(Material material, BigDecimal price) {
 *         buyPrices.put(material, price);
 *         priceManager.savePrices(this); // Persistierung
 *     }
 * }
 *
 * // PriceManager Verwendung:
 * if (plot instanceof Priceable priceable) {
 *     Optional&lt;BigDecimal&gt; buyPrice = priceable.getBuyPrice(Material.DIAMOND);
 *     buyPrice.ifPresent(price -> {
 *         player.sendMessage("§7Kaufpreis: §e" + price + " Sterne");
 *     });
 * }
 * </pre>
 *
 * **Kaufen vs. Verkaufen:**
 * - **Buy Price**: Preis bei dem PLOT/NPC vom Spieler KAUFT (Spieler verkauft)
 * - **Sell Price**: Preis bei dem PLOT/NPC an Spieler VERKAUFT (Spieler kauft)
 *
 * **Beispiel:**
 * - Diamond Buy Price: 100 Sterne (Spieler verkauft Diamond für 100 Sterne)
 * - Diamond Sell Price: 150 Sterne (Spieler kauft Diamond für 150 Sterne)
 * - Differenz: 50 Sterne Gewinn für Plot-Owner
 *
 * **Architektur:**
 * <pre>
 * Core → Priceable Interface
 *  ↑
 *  ├── Plots-Modul → StorageContainerPlot implements Priceable
 *  ├── NPCs-Modul → PriceableNpc implements Priceable (geplant)
 *  └── Economy-Modul → PriceManager (universelle Persistierung)
 * </pre>
 *
 * @author FallenStar
 * @version 1.0
 * @see PriceManager
 * @see de.fallenstar.core.provider.StorageContainerPlot
 */
public interface Priceable {

    /**
     * Gibt den Kaufpreis (Buy Price) für ein Material zurück.
     *
     * **Buy Price:** Preis bei dem dieses Priceable vom Spieler KAUFT.
     *
     * **Beispiel:**
     * - Plot kauft Diamond für 100 Sterne
     * - Spieler verkauft Diamond, erhält 100 Sterne
     *
     * @param material Das Material
     * @return Optional mit Preis, oder leer wenn kein Preis gesetzt
     */
    Optional<BigDecimal> getBuyPrice(Material material);

    /**
     * Setzt den Kaufpreis (Buy Price) für ein Material.
     *
     * **Wichtig:**
     * - Implementierungen sollten PriceManager.savePrices() aufrufen!
     * - Für Persistierung der Preise
     *
     * **Validierung:**
     * - price >= 0 (keine negativen Preise)
     * - price != null
     *
     * **Beispiel:**
     * <pre>
     * priceable.setBuyPrice(Material.DIAMOND, BigDecimal.valueOf(100));
     * // Plot kauft jetzt Diamonds für 100 Sterne
     * </pre>
     *
     * @param material Das Material
     * @param price Der Preis (muss >= 0 sein)
     * @throws IllegalArgumentException wenn price < 0 oder null
     */
    void setBuyPrice(Material material, BigDecimal price);

    /**
     * Gibt den Verkaufspreis (Sell Price) für ein Material zurück.
     *
     * **Sell Price:** Preis bei dem dieses Priceable an Spieler VERKAUFT.
     *
     * **Beispiel:**
     * - Plot verkauft Diamond für 150 Sterne
     * - Spieler kauft Diamond, zahlt 150 Sterne
     *
     * @param material Das Material
     * @return Optional mit Preis, oder leer wenn kein Preis gesetzt
     */
    Optional<BigDecimal> getSellPrice(Material material);

    /**
     * Setzt den Verkaufspreis (Sell Price) für ein Material.
     *
     * **Wichtig:**
     * - Implementierungen sollten PriceManager.savePrices() aufrufen!
     * - Für Persistierung der Preise
     *
     * **Validierung:**
     * - price >= 0 (keine negativen Preise)
     * - price != null
     *
     * **Beispiel:**
     * <pre>
     * priceable.setSellPrice(Material.DIAMOND, BigDecimal.valueOf(150));
     * // Spieler können jetzt Diamonds für 150 Sterne kaufen
     * </pre>
     *
     * @param material Das Material
     * @param price Der Preis (muss >= 0 sein)
     * @throws IllegalArgumentException wenn price < 0 oder null
     */
    void setSellPrice(Material material, BigDecimal price);

    /**
     * Prüft ob ein Kaufpreis für das Material gesetzt ist.
     *
     * @param material Das Material
     * @return true wenn Kaufpreis gesetzt ist
     */
    default boolean hasBuyPrice(Material material) {
        return getBuyPrice(material).isPresent();
    }

    /**
     * Prüft ob ein Verkaufspreis für das Material gesetzt ist.
     *
     * @param material Das Material
     * @return true wenn Verkaufspreis gesetzt ist
     */
    default boolean hasSellPrice(Material material) {
        return getSellPrice(material).isPresent();
    }

    /**
     * Entfernt den Kaufpreis für ein Material.
     *
     * **Verwendung:**
     * <pre>
     * priceable.removeBuyPrice(Material.DIAMOND);
     * // Diamond wird nicht mehr gekauft
     * </pre>
     *
     * @param material Das Material
     */
    void removeBuyPrice(Material material);

    /**
     * Entfernt den Verkaufspreis für ein Material.
     *
     * **Verwendung:**
     * <pre>
     * priceable.removeSellPrice(Material.DIAMOND);
     * // Diamond wird nicht mehr verkauft
     * </pre>
     *
     * @param material Das Material
     */
    void removeSellPrice(Material material);

    /**
     * Gibt alle Kaufpreise zurück.
     *
     * **Immutability:**
     * - Sollte unmodifiable Map zurückgeben
     * - Verhindert externe Änderungen ohne Persistierung
     *
     * **Verwendung:**
     * <pre>
     * Map&lt;Material, BigDecimal&gt; buyPrices = priceable.getAllBuyPrices();
     * buyPrices.forEach((material, price) -> {
     *     player.sendMessage("§7" + material + ": §e" + price + " Sterne (Kaufpreis)");
     * });
     * </pre>
     *
     * @return Unmodifiable Map aller Kaufpreise
     */
    Map<Material, BigDecimal> getAllBuyPrices();

    /**
     * Gibt alle Verkaufspreise zurück.
     *
     * **Immutability:**
     * - Sollte unmodifiable Map zurückgeben
     * - Verhindert externe Änderungen ohne Persistierung
     *
     * **Verwendung:**
     * <pre>
     * Map&lt;Material, BigDecimal&gt; sellPrices = priceable.getAllSellPrices();
     * sellPrices.forEach((material, price) -> {
     *     player.sendMessage("§7" + material + ": §e" + price + " Sterne (Verkaufspreis)");
     * });
     * </pre>
     *
     * @return Unmodifiable Map aller Verkaufspreise
     */
    Map<Material, BigDecimal> getAllSellPrices();

    /**
     * Löscht ALLE Kaufpreise.
     *
     * **ACHTUNG:** Irreversible Operation!
     * Sollte Bestätigung vom User erfordern.
     *
     * **Verwendung:**
     * <pre>
     * priceable.clearAllBuyPrices();
     * priceManager.savePrices(priceable);
     * </pre>
     */
    void clearAllBuyPrices();

    /**
     * Löscht ALLE Verkaufspreise.
     *
     * **ACHTUNG:** Irreversible Operation!
     * Sollte Bestätigung vom User erfordern.
     *
     * **Verwendung:**
     * <pre>
     * priceable.clearAllSellPrices();
     * priceManager.savePrices(priceable);
     * </pre>
     */
    void clearAllSellPrices();
}
