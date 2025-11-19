package de.fallenstar.core.provider;

import de.fallenstar.core.interaction.action.UiActionInfo;
import de.fallenstar.core.pricing.Priceable;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Trait-Interface für Plots mit Lager-Funktionalität.
 *
 * **Features:**
 * - Virtuelles Inventar für Items
 * - Ankauf/Verkauf-Preise pro Material (via Priceable)
 * - Unbegrenzte Kapazität
 * - Persistente Speicherung
 *
 * **SOLID-Refactoring (Sprint 19 Phase 3):**
 * - **Interface Segregation**: StorageContainerPlot extends Priceable
 * - **Single Responsibility**: Storage-Logik getrennt von Preis-Logik
 * - **Composition**: Priceable wird komponiert statt dupliziert
 *
 * **Verwendung:**
 * <pre>
 * class TradeguildPlot extends BasePlot implements StorageContainerPlot {
 *     private Map&lt;Material, Integer&gt; storage = new HashMap&lt;&gt;();
 *     private Map&lt;Material, BigDecimal&gt; buyPrices = new HashMap&lt;&gt;();
 *     private Map&lt;Material, BigDecimal&gt; sellPrices = new HashMap&lt;&gt;();
 *
 *     // Storage-Methoden:
 *     {@literal @}Override
 *     public int getStoredAmount(Material material) {
 *         return storage.getOrDefault(material, 0);
 *     }
 *
 *     {@literal @}Override
 *     public void addToStorage(ItemStack item) {
 *         storage.merge(item.getType(), item.getAmount(), Integer::sum);
 *     }
 *
 *     // Priceable-Methoden (geerbt):
 *     {@literal @}Override
 *     public Optional&lt;BigDecimal&gt; getBuyPrice(Material material) {
 *         return Optional.ofNullable(buyPrices.get(material));
 *     }
 *
 *     {@literal @}Override
 *     public void setBuyPrice(Material material, BigDecimal price) {
 *         buyPrices.put(material, price);
 *     }
 * }
 * </pre>
 *
 * **Integration:**
 * - StoragePriceUi: Preise verwalten
 * - PlotStorageUi: Lager-Übersicht
 * - GuildTraderNPC: Nutzt Lager für Handel
 *
 * @author FallenStar
 * @version 2.0 (Sprint 19 - extends Priceable)
 * @see Priceable
 */
public interface StorageContainerPlot extends Plot, Priceable {

    /**
     * Gibt die Anzahl eines Materials im Lager zurück.
     *
     * @param material Material
     * @return Anzahl (0 wenn nicht vorhanden)
     */
    int getStoredAmount(Material material);

    /**
     * Fügt Items zum Lager hinzu.
     *
     * @param item ItemStack
     */
    void addToStorage(ItemStack item);

    /**
     * Entfernt Items aus dem Lager.
     *
     * @param material Material
     * @param amount Anzahl
     * @return Tatsächlich entfernte Anzahl
     */
    int removeFromStorage(Material material, int amount);

    /**
     * Prüft ob genug Items im Lager sind.
     *
     * @param material Material
     * @param amount Anzahl
     * @return true wenn genug vorhanden
     */
    default boolean hasInStorage(Material material, int amount) {
        return getStoredAmount(material) >= amount;
    }

    /**
     * Gibt alle Items im Lager zurück.
     *
     * @return Map von Material zu Anzahl
     */
    Map<Material, Integer> getStorageContents();

    /**
     * Leert das Lager.
     */
    void clearStorage();

    // ========== Preis-Methoden (geerbt von Priceable) ==========
    // Folgende Methoden sind jetzt von Priceable geerbt:
    // - Optional<BigDecimal> getBuyPrice(Material material)
    // - Optional<BigDecimal> getSellPrice(Material material)
    // - void setBuyPrice(Material material, BigDecimal price)
    // - void setSellPrice(Material material, BigDecimal price)
    // - Map<Material, BigDecimal> getAllBuyPrices()
    // - Map<Material, BigDecimal> getAllSellPrices()
    // - void removeBuyPrice(Material material)
    // - void removeSellPrice(Material material)
    // - void clearAllBuyPrices()
    // - void clearAllSellPrices()
    //
    // Siehe: de.fallenstar.core.pricing.Priceable
    // ===========================================================

    /**
     * Gibt die Standard-Actions für StorageContainerPlot zurück.
     *
     * Diese Methode liefert alle Lager-bezogenen Actions für das UI.
     * Die tatsächliche Filterung (Owner vs Guest) erfolgt in der
     * Implementierung (z.B. TradeguildPlot.getAvailableActions()).
     *
     * @return Liste von UI-Actions
     */
    default List<UiActionInfo> getStorageActions() {
        List<UiActionInfo> actions = new ArrayList<>();

        // Lager verwalten (Owner-Action)
        actions.add(UiActionInfo.builder()
                .id("manage_storage")
                .displayName("§aLager verwalten")
                .lore(List.of(
                        "§7Verwalte das Plot-Storage",
                        "§7für Handelswaren",
                        "§7",
                        "§7Klicke um das Lager zu öffnen",
                        "§7(Nur Owner)"
                ))
                .icon(Material.CHEST)
                .requiredPermission("fallenstar.plot.storage.manage")
                .build());

        // Preise verwalten (Owner-Action)
        actions.add(UiActionInfo.builder()
                .id("manage_prices")
                .displayName("§ePreise verwalten")
                .lore(List.of(
                        "§7Setze Ankauf- und Verkaufspreise",
                        "§7für Items in diesem Plot",
                        "§7",
                        "§7Klicke um Preise zu setzen",
                        "§7(Nur Owner)"
                ))
                .icon(Material.GOLD_INGOT)
                .requiredPermission("fallenstar.plot.prices.manage")
                .build());

        // Preisliste anzeigen (Guest-Action)
        actions.add(UiActionInfo.builder()
                .id("view_prices")
                .displayName("§ePreisliste anzeigen")
                .lore(List.of(
                        "§7Zeigt alle verfügbaren Items",
                        "§7und deren Preise an",
                        "§7",
                        "§7Klicke zum Öffnen"
                ))
                .icon(Material.BOOK)
                .build());

        return actions;
    }
}
