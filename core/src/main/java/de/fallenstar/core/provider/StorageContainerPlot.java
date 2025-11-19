package de.fallenstar.core.provider;

import de.fallenstar.core.interaction.action.UiActionInfo;
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
 * - Ankauf/Verkauf-Preise pro Material
 * - Unbegrenzte Kapazität
 * - Persistente Speicherung
 *
 * **Verwendung:**
 * <pre>
 * class TradeguildPlot extends BasePlot implements StorageContainerPlot {
 *     private Map&lt;Material, Integer&gt; storage = new HashMap&lt;&gt;();
 *     private Map&lt;Material, BigDecimal&gt; buyPrices = new HashMap&lt;&gt;();
 *     private Map&lt;Material, BigDecimal&gt; sellPrices = new HashMap&lt;&gt;();
 *
 *     {@literal @}Override
 *     public int getStoredAmount(Material material) {
 *         return storage.getOrDefault(material, 0);
 *     }
 *
 *     {@literal @}Override
 *     public void addToStorage(ItemStack item) {
 *         storage.merge(item.getType(), item.getAmount(), Integer::sum);
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
 * @version 1.0
 */
public interface StorageContainerPlot extends Plot {

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

    /**
     * Gibt den Ankaufspreis zurück (NPC kauft von Spieler).
     *
     * @param material Material
     * @return Optional mit Preis, oder empty wenn nicht gesetzt
     */
    Optional<BigDecimal> getBuyPrice(Material material);

    /**
     * Gibt den Verkaufspreis zurück (Spieler kauft von NPC).
     *
     * @param material Material
     * @return Optional mit Preis, oder empty wenn nicht gesetzt
     */
    Optional<BigDecimal> getSellPrice(Material material);

    /**
     * Setzt den Ankaufspreis.
     *
     * @param material Material
     * @param price Preis (null zum Löschen)
     */
    void setBuyPrice(Material material, BigDecimal price);

    /**
     * Setzt den Verkaufspreis.
     *
     * @param material Material
     * @param price Preis (null zum Löschen)
     */
    void setSellPrice(Material material, BigDecimal price);

    /**
     * Gibt alle Ankaufspreise zurück.
     *
     * @return Map von Material zu Preis
     */
    Map<Material, BigDecimal> getAllBuyPrices();

    /**
     * Gibt alle Verkaufspreise zurück.
     *
     * @return Map von Material zu Preis
     */
    Map<Material, BigDecimal> getAllSellPrices();

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
