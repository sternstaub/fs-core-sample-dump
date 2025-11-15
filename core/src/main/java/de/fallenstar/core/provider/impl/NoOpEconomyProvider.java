package de.fallenstar.core.provider.impl;

import de.fallenstar.core.exception.ProviderFunctionalityNotFoundException;
import de.fallenstar.core.provider.EconomyProvider;
import org.bukkit.entity.Player;
import java.util.UUID;

/**
 * NoOp (No Operation) Implementation des EconomyProviders.
 *
 * Wird verwendet wenn kein Economy-Plugin (Vault) verfügbar ist.
 * Alle Methoden werfen ProviderFunctionalityNotFoundException.
 *
 * Pattern: Null Object Pattern kombiniert mit Exception-Handling
 * Vorteil: Kein Null-Checking nötig, explizites Fehlerhandling
 *
 * @author FallenStar
 * @version 1.0
 */
public class NoOpEconomyProvider implements EconomyProvider {

    private static final String PROVIDER_NAME = "EconomyProvider";
    private static final String REASON = "No economy plugin (Vault) available";

    @Override
    public boolean isAvailable() {
        return false;
    }

    @Override
    public double getBalance(Player player) throws ProviderFunctionalityNotFoundException {
        throw new ProviderFunctionalityNotFoundException(
            PROVIDER_NAME,
            "getBalance",
            REASON
        );
    }

    @Override
    public boolean withdraw(Player player, double amount)
            throws ProviderFunctionalityNotFoundException {
        throw new ProviderFunctionalityNotFoundException(
            PROVIDER_NAME,
            "withdraw",
            REASON
        );
    }

    @Override
    public boolean deposit(Player player, double amount)
            throws ProviderFunctionalityNotFoundException {
        throw new ProviderFunctionalityNotFoundException(
            PROVIDER_NAME,
            "deposit",
            REASON
        );
    }

    @Override
    public double getFactionBalance(UUID accountId)
            throws ProviderFunctionalityNotFoundException {
        throw new ProviderFunctionalityNotFoundException(
            PROVIDER_NAME,
            "getFactionBalance",
            REASON
        );
    }

    @Override
    public boolean withdrawFaction(UUID accountId, double amount)
            throws ProviderFunctionalityNotFoundException {
        throw new ProviderFunctionalityNotFoundException(
            PROVIDER_NAME,
            "withdrawFaction",
            REASON
        );
    }

    @Override
    public boolean depositFaction(UUID accountId, double amount)
            throws ProviderFunctionalityNotFoundException {
        throw new ProviderFunctionalityNotFoundException(
            PROVIDER_NAME,
            "depositFaction",
            REASON
        );
    }
}
