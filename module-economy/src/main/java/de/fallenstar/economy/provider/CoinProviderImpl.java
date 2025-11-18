package de.fallenstar.economy.provider;

import de.fallenstar.core.exception.ProviderFunctionalityNotFoundException;
import de.fallenstar.core.provider.CoinProvider;
import de.fallenstar.economy.manager.CurrencyManager;
import de.fallenstar.economy.model.CurrencyItemSet;
import org.bukkit.inventory.ItemStack;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Implementierung des CoinProviders basierend auf dem CurrencyManager.
 *
 * **Zweck:**
 * - Type-Safe Bridge zwischen Core-Interface und Economy-Manager
 * - Eliminiert Reflection-Aufrufe aus anderen Modulen
 * - Delegiert an CurrencyManager für tatsächliche Münz-Erstellung
 *
 * **Verwendung:**
 * - Wird vom Economy-Modul in ProviderRegistry registriert
 * - Andere Module nutzen CoinProvider via registry.getCoinProvider()
 *
 * @author FallenStar
 * @version 1.0
 */
public class CoinProviderImpl implements CoinProvider {

    private final Logger logger;
    private final CurrencyManager currencyManager;

    /**
     * Konstruktor für CoinProviderImpl.
     *
     * @param logger Logger
     * @param currencyManager CurrencyManager
     */
    public CoinProviderImpl(Logger logger, CurrencyManager currencyManager) {
        this.logger = logger;
        this.currencyManager = currencyManager;
    }

    @Override
    public boolean isAvailable() {
        return currencyManager != null && currencyManager.getBaseCurrency() != null;
    }

    @Override
    public Optional<ItemStack> createCoinsForPrice(BigDecimal price, int maxStackSize)
            throws ProviderFunctionalityNotFoundException {
        if (!isAvailable()) {
            throw new ProviderFunctionalityNotFoundException(
                "CoinProvider",
                "createCoinsForPrice",
                "Basiswährung nicht verfügbar"
            );
        }

        CurrencyItemSet baseCurrency = currencyManager.getBaseCurrency();

        // Tier-Auswahl basierend auf Preis
        CurrencyItemSet.CurrencyTier tier;
        BigDecimal amount;

        if (price.compareTo(BigDecimal.valueOf(10)) < 0) {
            // 0.01 - 9.99 Sterne → Bronze (1er Münzen)
            tier = CurrencyItemSet.CurrencyTier.BRONZE;
            amount = price;
        } else if (price.compareTo(BigDecimal.valueOf(1000)) < 0) {
            // 10 - 999 Sterne → Silber (10er Münzen)
            tier = CurrencyItemSet.CurrencyTier.SILVER;
            amount = price.divide(BigDecimal.TEN);
        } else {
            // 1000+ Sterne → Gold (100er Münzen)
            tier = CurrencyItemSet.CurrencyTier.GOLD;
            amount = price.divide(BigDecimal.valueOf(100));
        }

        // Prüfe Stack-Size
        int coinAmount = amount.intValue();
        if (coinAmount > maxStackSize) {
            logger.warning("Coin amount " + coinAmount + " exceeds max stack size " + maxStackSize);
            coinAmount = maxStackSize;
        }

        if (coinAmount <= 0) {
            logger.warning("Coin amount is 0 or negative for price " + price);
            return Optional.empty();
        }

        // Delegiere an CurrencyManager
        ItemStack coins = currencyManager.createCoin(baseCurrency.currencyId(), tier, coinAmount);
        return Optional.ofNullable(coins);
    }

    @Override
    public Optional<ItemStack> createCoins(String tier, int amount)
            throws ProviderFunctionalityNotFoundException {
        if (!isAvailable()) {
            throw new ProviderFunctionalityNotFoundException(
                "CoinProvider",
                "createCoins",
                "Basiswährung nicht verfügbar"
            );
        }

        CurrencyItemSet baseCurrency = currencyManager.getBaseCurrency();

        // Konvertiere String tier zu CurrencyTier Enum
        CurrencyItemSet.CurrencyTier currencyTier;
        try {
            currencyTier = CurrencyItemSet.CurrencyTier.valueOf(tier.toUpperCase());
        } catch (IllegalArgumentException e) {
            logger.warning("Ungültiger Tier: " + tier);
            return Optional.empty();
        }

        // Delegiere an CurrencyManager
        ItemStack coins = currencyManager.createCoin(baseCurrency.currencyId(), currencyTier, amount);
        return Optional.ofNullable(coins);
    }

    @Override
    public String getBaseCurrencyName() throws ProviderFunctionalityNotFoundException {
        if (!isAvailable()) {
            throw new ProviderFunctionalityNotFoundException(
                "CoinProvider",
                "getBaseCurrencyName",
                "Basiswährung nicht verfügbar"
            );
        }

        CurrencyItemSet baseCurrency = currencyManager.getBaseCurrency();
        return baseCurrency.displayName();
    }

    @Override
    public String getBaseCurrencyId() throws ProviderFunctionalityNotFoundException {
        if (!isAvailable()) {
            throw new ProviderFunctionalityNotFoundException(
                "CoinProvider",
                "getBaseCurrencyId",
                "Basiswährung nicht verfügbar"
            );
        }

        CurrencyItemSet baseCurrency = currencyManager.getBaseCurrency();
        return baseCurrency.currencyId();
    }
}
