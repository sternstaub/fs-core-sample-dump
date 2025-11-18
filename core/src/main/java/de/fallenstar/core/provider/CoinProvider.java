package de.fallenstar.core.provider;

import org.bukkit.inventory.ItemStack;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Provider-Interface für Münz-Erstellung.
 *
 * **Zweck:**
 * - Abstraktion der Währungs-/Münz-Systeme
 * - Type-Safe Alternative zu Reflection
 * - Ermöglicht Module, Münzen zu erstellen ohne Economy-Modul direkt zu kennen
 *
 * **Implementierung:**
 * - Economy-Modul registriert CoinProvider
 * - Andere Module nutzen Provider über ProviderRegistry
 *
 * **Verwendung:**
 * <pre>
 * CoinProvider provider = registry.getCoinProvider();
 * if (provider.isAvailable()) {
 *     Optional&lt;ItemStack&gt; coins = provider.createCoinsForPrice(
 *         BigDecimal.valueOf(100), // Preis in Basiswährung (Sterne)
 *         64  // Max. Stack-Size
 *     );
 * }
 * </pre>
 *
 * @author FallenStar
 * @version 1.0
 */
public interface CoinProvider {

    /**
     * Prüft ob der Provider verfügbar ist.
     *
     * @return true wenn verfügbar
     */
    boolean isAvailable();

    /**
     * Erstellt Münzen für einen Preis in Basiswährung.
     *
     * Diese Methode:
     * 1. Konvertiert Preis in passende Münz-Tier (Bronze/Silber/Gold)
     * 2. Erstellt ItemStack mit korrekter Menge
     * 3. Berücksichtigt Max-Stack-Size
     *
     * **Tier-Auswahl:**
     * - 0.01 - 9.99 Sterne → Bronze (1er Münzen)
     * - 10 - 999 Sterne → Silber (10er Münzen)
     * - 1000+ Sterne → Gold (100er Münzen)
     *
     * **Beispiele:**
     * - 5 Sterne → 5x Bronze-Münze
     * - 50 Sterne → 5x Silber-Münze
     * - 500 Sterne → 50x Silber-Münze
     * - 5000 Sterne → 50x Gold-Münze
     *
     * @param price Preis in Basiswährung (Sterne)
     * @param maxStackSize Maximale Stack-Größe (typischerweise 64)
     * @return Optional mit Münz-ItemStack, oder empty wenn fehlgeschlagen
     */
    Optional<ItemStack> createCoinsForPrice(BigDecimal price, int maxStackSize);

    /**
     * Erstellt Münzen mit spezifischem Tier und Menge.
     *
     * **Tier-Werte:**
     * - "BRONZE": 1 Stern pro Münze
     * - "SILVER": 10 Sterne pro Münze
     * - "GOLD": 100 Sterne pro Münze
     *
     * @param tier Münz-Tier ("BRONZE", "SILVER", "GOLD")
     * @param amount Anzahl Münzen
     * @return Optional mit Münz-ItemStack, oder empty wenn fehlgeschlagen
     */
    Optional<ItemStack> createCoins(String tier, int amount);

    /**
     * Gibt den Namen der Basiswährung zurück.
     *
     * @return Währungs-Name (z.B. "Sterne", "Stars")
     */
    String getBaseCurrencyName();

    /**
     * Gibt die Basiswährungs-ID zurück.
     *
     * @return Währungs-ID (z.B. "sterne")
     */
    String getBaseCurrencyId();
}
