package de.fallenstar.core.currency;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/**
 * Interface für Währungs-Items (Currency).
 *
 * **SOLID-Prinzipien (Sprint 19 Phase 3):**
 *
 * **Single Responsibility:**
 * - CurrencyItem kennt NUR seine Darstellung als ItemStack
 * - Keine Logik für Wert-Berechnung oder Storage
 *
 * **Open/Closed:**
 * - Neue Währungen durch Implementierung hinzufügbar
 * - OHNE Änderung bestehenden Codes (CoinProvider, PriceManager)
 *
 * **Liskov Substitution:**
 * - Alle CurrencyItems sind austauschbar
 * - CoinProvider funktioniert mit JEDER Implementierung
 *
 * **Interface Segregation:**
 * - Nur Currency-relevante Methoden
 * - Kein "GodInterface" mit Economy-Logik
 *
 * **Dependency Inversion:**
 * - High-Level (CoinProvider) hängt von Abstraction ab
 * - NICHT von konkreten Implementierungen (VanillaCoin, MMOItemsCoin)
 *
 * **Implementierungen:**
 * - VanillaCoinItem (Items-Modul) - Vanilla Minecraft Items
 * - MMOItemsCoinItem (Items-Modul) - MMOItems Custom Items (geplant)
 * - CustomCoinItem (Plugin-Extensions) - Benutzerdefinierte Währungen
 *
 * **Registry-Pattern:**
 * CurrencyRegistry verwaltet alle registrierten CurrencyItems.
 * CoinProvider nutzt Registry statt hart-kodierter Währungen.
 *
 * **Verwendung:**
 * <pre>
 * // Registrierung (ItemsModule):
 * CurrencyItem vanillaCoin = new VanillaCoinItem("vanilla_star", Material.NETHER_STAR, "§6Stern");
 * currencyRegistry.register(vanillaCoin);
 *
 * // Nutzung (CoinProvider):
 * Optional&lt;CurrencyItem&gt; currency = currencyRegistry.get("vanilla_star");
 * ItemStack displayItem = currency.getDisplayItem();
 * </pre>
 *
 * **Anti-Pattern (vermeiden!):**
 * <pre>
 * // ❌ FALSCH: Hart-kodiert
 * ItemStack coin = new ItemStack(Material.NETHER_STAR);
 * coin.setDisplayName("§6Stern");
 *
 * // ✅ RICHTIG: Interface-basiert
 * CurrencyItem coin = currencyRegistry.get("vanilla_star").orElseThrow();
 * ItemStack displayItem = coin.getDisplayItem();
 * </pre>
 *
 * @author FallenStar
 * @version 1.0
 * @see CurrencyRegistry
 * @see de.fallenstar.items.model.VanillaCoinItem
 */
public interface CurrencyItem {

    /**
     * Eindeutige Identifikation für diese Währung.
     *
     * **Konvention:**
     * - Lowercase mit Unterstrichen: `vanilla_star`, `mmo_gold_coin`
     * - Modul-Prefix für Klarheit: `vanilla_`, `mmo_`, `custom_`
     *
     * **Beispiele:**
     * - `vanilla_star` - Vanilla Nether Star
     * - `vanilla_gold_ingot` - Vanilla Gold Ingot
     * - `mmo_guild_token` - MMOItems Guild Token
     *
     * @return Eindeutige Currency-ID
     */
    String getIdentifier();

    /**
     * Anzeigename für UI/Chat (mit Color Codes).
     *
     * **Beispiele:**
     * - `§6Stern` - Goldener Stern
     * - `§eGoldbarren` - Gelber Goldbarren
     * - `§bGilden-Token` - Aqua Guild Token
     *
     * @return Formatierter Display-Name
     */
    String getDisplayName();

    /**
     * Das Minecraft Material dieses Currency-Items.
     *
     * **Für Vanilla Items:**
     * - Material.NETHER_STAR, Material.GOLD_INGOT, etc.
     *
     * **Für MMOItems:**
     * - Material des Base-Items (z.B. Material.PAPER für Custom Items)
     *
     * @return Das Material
     */
    Material getMaterial();

    /**
     * Erstellt ein ItemStack für dieses Currency-Item.
     *
     * **Für Vanilla Items:**
     * - ItemStack mit Material + DisplayName + Lore
     *
     * **Für MMOItems:**
     * - MMOItems.getItem() für Custom Items
     * - Mit CustomModelData, Attributen, etc.
     *
     * **Amount:**
     * - Default: 1 Stack
     * - Für mehrere: Nutze getDisplayItem().setAmount(amount)
     *
     * **Verwendung:**
     * <pre>
     * ItemStack coin = currency.getDisplayItem();
     * player.getInventory().addItem(coin);
     * </pre>
     *
     * @return ItemStack mit Amount 1
     */
    ItemStack getDisplayItem();

    /**
     * Erstellt ein ItemStack mit spezifischer Anzahl.
     *
     * **Convenience-Methode** für getDisplayItem() + setAmount().
     *
     * **Beispiel:**
     * <pre>
     * ItemStack coins = currency.getDisplayItem(64); // 1 Stack
     * player.getInventory().addItem(coins);
     * </pre>
     *
     * **Default-Implementierung:**
     * <pre>
     * default ItemStack getDisplayItem(int amount) {
     *     ItemStack item = getDisplayItem();
     *     item.setAmount(amount);
     *     return item;
     * }
     * </pre>
     *
     * @param amount Anzahl (1-64 für normale Items)
     * @return ItemStack mit spezifischer Anzahl
     */
    default ItemStack getDisplayItem(int amount) {
        ItemStack item = getDisplayItem();
        item.setAmount(amount);
        return item;
    }

    /**
     * Optional: CustomModelData für Resource Packs.
     *
     * **Verwendung:**
     * - Vanilla Items ohne Custom Model: return -1
     * - MMOItems oder Custom Models: return CustomModelData
     *
     * **Beispiel:**
     * <pre>
     * @Override
     * public int getCustomModelData() {
     *     return 12345; // Custom Model aus Resource Pack
     * }
     * </pre>
     *
     * @return CustomModelData oder -1 wenn nicht verwendet
     */
    default int getCustomModelData() {
        return -1; // Default: Kein Custom Model
    }

    /**
     * Prüft ob ein ItemStack diese Currency repräsentiert.
     *
     * **Für Vanilla Items:**
     * - Vergleich via Material + DisplayName
     *
     * **Für MMOItems:**
     * - Vergleich via MMOItems.getType() + MMOItems.getId()
     *
     * **Verwendung:**
     * <pre>
     * if (currency.isCurrency(itemInHand)) {
     *     // Spieler hält Currency-Item
     * }
     * </pre>
     *
     * **WICHTIG:**
     * - Muss Amount-unabhängig sein!
     * - isCurrency(1x Stern) == isCurrency(64x Sterne)
     *
     * @param item Das zu prüfende ItemStack
     * @return true wenn item diese Currency ist
     */
    boolean isCurrency(ItemStack item);
}
