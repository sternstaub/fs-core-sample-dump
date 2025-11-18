package de.fallenstar.core.provider;

import org.bukkit.Material;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Provider für Item-Basis-Preise.
 *
 * Dieses Interface wird von Grundstücken implementiert, die Preise für Items festlegen
 * (z.B. Handelsgilden). Es unterscheidet zwischen Ankauf- und Verkaufspreisen.
 *
 * **Konzept:**
 * - **Ankaufpreis (Buy Price)**: NPC kauft Item vom Spieler
 * - **Verkaufspreis (Sell Price)**: Spieler kauft Item vom NPC
 *
 * **Verwendung:**
 * ```java
 * ItemBasePriceProvider provider = plot.getPriceProvider();
 *
 * // Spieler verkauft Diamond an NPC
 * BigDecimal buyPrice = provider.getBuyPrice(plot, Material.DIAMOND).orElse(BigDecimal.ZERO);
 *
 * // Spieler kauft Diamond vom NPC
 * BigDecimal sellPrice = provider.getSellPrice(plot, Material.DIAMOND).orElse(BigDecimal.ZERO);
 * ```
 *
 * **Implementierungen:**
 * - {@code PlotPriceManager} (Plots-Modul) - Plot-basierte Preise
 *
 * @author FallenStar
 * @version 1.0
 */
public interface ItemBasePriceProvider {

    /**
     * Gibt den Ankaufpreis für ein Material zurück.
     *
     * Dies ist der Preis, den der NPC zahlt, wenn ein Spieler das Item verkauft.
     *
     * @param plot Das Grundstück
     * @param material Das Material
     * @return Ankaufpreis oder Empty wenn kein Preis festgelegt
     */
    Optional<BigDecimal> getBuyPrice(Plot plot, Material material);

    /**
     * Gibt den Verkaufspreis für ein Material zurück.
     *
     * Dies ist der Preis, den der Spieler zahlen muss, um das Item vom NPC zu kaufen.
     *
     * @param plot Das Grundstück
     * @param material Das Material
     * @return Verkaufspreis oder Empty wenn kein Preis festgelegt
     */
    Optional<BigDecimal> getSellPrice(Plot plot, Material material);

    /**
     * Setzt den Ankaufpreis für ein Material.
     *
     * @param plot Das Grundstück
     * @param material Das Material
     * @param price Der Preis (null zum Entfernen)
     */
    void setBuyPrice(Plot plot, Material material, BigDecimal price);

    /**
     * Setzt den Verkaufspreis für ein Material.
     *
     * @param plot Das Grundstück
     * @param material Das Material
     * @param price Der Preis (null zum Entfernen)
     */
    void setSellPrice(Plot plot, Material material, BigDecimal price);

    /**
     * Prüft ob für ein Material Preise festgelegt sind.
     *
     * @param plot Das Grundstück
     * @param material Das Material
     * @return true wenn Ankauf ODER Verkauf festgelegt ist
     */
    boolean hasPrices(Plot plot, Material material);

    /**
     * Entfernt alle Preise für ein Material.
     *
     * @param plot Das Grundstück
     * @param material Das Material
     */
    void removePrices(Plot plot, Material material);

    /**
     * Prüft ob dieser Provider verfügbar ist.
     *
     * @return true wenn verfügbar
     */
    boolean isAvailable();
}
