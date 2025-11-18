package de.fallenstar.core.provider.impl;

import de.fallenstar.core.exception.ProviderFunctionalityNotFoundException;
import de.fallenstar.core.provider.EconomyProvider;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Optional;
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
 * @version 2.0
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

    @Override
    public boolean setBalance(Player player, double amount)
            throws ProviderFunctionalityNotFoundException {
        throw new ProviderFunctionalityNotFoundException(
            PROVIDER_NAME,
            "setBalance",
            REASON
        );
    }

    @Override
    public boolean hasAccount(OfflinePlayer player)
            throws ProviderFunctionalityNotFoundException {
        throw new ProviderFunctionalityNotFoundException(
            PROVIDER_NAME,
            "hasAccount",
            REASON
        );
    }

    @Override
    public boolean createAccount(OfflinePlayer player)
            throws ProviderFunctionalityNotFoundException {
        throw new ProviderFunctionalityNotFoundException(
            PROVIDER_NAME,
            "createAccount",
            REASON
        );
    }

    @Override
    public String format(double amount)
            throws ProviderFunctionalityNotFoundException {
        throw new ProviderFunctionalityNotFoundException(
            PROVIDER_NAME,
            "format",
            REASON
        );
    }

    @Override
    public String getEconomyName()
            throws ProviderFunctionalityNotFoundException {
        throw new ProviderFunctionalityNotFoundException(
            PROVIDER_NAME,
            "getEconomyName",
            REASON
        );
    }

    @Override
    public String getCurrencyNameSingular()
            throws ProviderFunctionalityNotFoundException {
        throw new ProviderFunctionalityNotFoundException(
            PROVIDER_NAME,
            "getCurrencyNameSingular",
            REASON
        );
    }

    @Override
    public String getCurrencyNamePlural()
            throws ProviderFunctionalityNotFoundException {
        throw new ProviderFunctionalityNotFoundException(
            PROVIDER_NAME,
            "getCurrencyNamePlural",
            REASON
        );
    }

    @Override
    public Optional<BigDecimal> getBuyPrice(Material material)
            throws ProviderFunctionalityNotFoundException {
        throw new ProviderFunctionalityNotFoundException(
            PROVIDER_NAME,
            "getBuyPrice",
            REASON
        );
    }

    @Override
    public Optional<BigDecimal> getSellPrice(Material material)
            throws ProviderFunctionalityNotFoundException {
        throw new ProviderFunctionalityNotFoundException(
            PROVIDER_NAME,
            "getSellPrice",
            REASON
        );
    }

    @Override
    public boolean setItemPrice(Material material, BigDecimal buyPrice, BigDecimal sellPrice)
            throws ProviderFunctionalityNotFoundException {
        throw new ProviderFunctionalityNotFoundException(
            PROVIDER_NAME,
            "setItemPrice",
            REASON
        );
    }

    @Override
    public Collection<Material> getAllPricedMaterials()
            throws ProviderFunctionalityNotFoundException {
        throw new ProviderFunctionalityNotFoundException(
            PROVIDER_NAME,
            "getAllPricedMaterials",
            REASON
        );
    }
}
