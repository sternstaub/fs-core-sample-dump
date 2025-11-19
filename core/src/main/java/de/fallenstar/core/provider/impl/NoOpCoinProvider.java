package de.fallenstar.core.provider.impl;

import de.fallenstar.core.exception.ProviderFunctionalityNotFoundException;
import de.fallenstar.core.provider.CoinProvider;
import org.bukkit.inventory.ItemStack;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * NoOp (No Operation) Implementation des CoinProviders.
 *
 * Wird verwendet wenn kein Economy-Modul verfügbar ist.
 * Alle Methoden werfen ProviderFunctionalityNotFoundException.
 *
 * Pattern: Null Object Pattern kombiniert mit Exception-Handling
 * Vorteil: Kein Null-Checking nötig, explizites Fehlerhandling
 *
 * @author FallenStar
 * @version 1.0
 */
public class NoOpCoinProvider implements CoinProvider {

    private static final String PROVIDER_NAME = "CoinProvider";
    private static final String REASON = "No economy module available";

    @Override
    public boolean isAvailable() {
        return false;
    }

    @Override
    public Optional<ItemStack> createCoinsForPrice(BigDecimal price, int maxStackSize)
            throws ProviderFunctionalityNotFoundException {
        throw new ProviderFunctionalityNotFoundException(
            PROVIDER_NAME,
            "createCoinsForPrice",
            REASON
        );
    }

    @Override
    public Optional<ItemStack> createCoins(String tier, int amount)
            throws ProviderFunctionalityNotFoundException {
        throw new ProviderFunctionalityNotFoundException(
            PROVIDER_NAME,
            "createCoins",
            REASON
        );
    }

    @Override
    public String getBaseCurrencyName() throws ProviderFunctionalityNotFoundException {
        throw new ProviderFunctionalityNotFoundException(
            PROVIDER_NAME,
            "getBaseCurrencyName",
            REASON
        );
    }

    @Override
    public String getBaseCurrencyId() throws ProviderFunctionalityNotFoundException {
        throw new ProviderFunctionalityNotFoundException(
            PROVIDER_NAME,
            "getBaseCurrencyId",
            REASON
        );
    }
}
