package de.fallenstar.core.provider.impl;

import de.fallenstar.core.exception.ProviderFunctionalityNotFoundException;
import de.fallenstar.core.provider.EconomyProvider;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.UUID;
import java.util.logging.Logger;

/**
 * Vault Economy Provider Implementation.
 *
 * Integriert mit dem Vault-Economy-System.
 * Unterstützt alle Vault-kompatiblen Economy-Plugins.
 *
 * Faction/Stadt-Economy wird über Towny-Bank-Accounts abgebildet.
 *
 * @author FallenStar
 * @version 1.0
 */
public class VaultEconomyProvider implements EconomyProvider {

    private final Economy economy;
    private final Logger logger;

    /**
     * Erstellt einen neuen VaultEconomyProvider.
     *
     * @param logger Logger für Fehlerausgaben
     */
    public VaultEconomyProvider(Logger logger) {
        this.logger = logger;
        this.economy = setupEconomy();

        if (economy != null) {
            logger.info("✓ VaultEconomyProvider initialized: " + economy.getName());
        } else {
            logger.warning("✗ VaultEconomyProvider: Vault found but no economy plugin");
        }
    }

    /**
     * Setup Vault Economy.
     *
     * @return Economy instance oder null
     */
    private Economy setupEconomy() {
        if (Bukkit.getServer().getPluginManager().getPlugin("Vault") == null) {
            return null;
        }

        RegisteredServiceProvider<Economy> rsp = Bukkit.getServer()
                .getServicesManager()
                .getRegistration(Economy.class);

        if (rsp == null) {
            return null;
        }

        return rsp.getProvider();
    }

    @Override
    public boolean isAvailable() {
        return economy != null;
    }

    @Override
    public double getBalance(Player player) throws ProviderFunctionalityNotFoundException {
        if (!isAvailable()) {
            throw new ProviderFunctionalityNotFoundException(
                "EconomyProvider",
                "getBalance",
                "Vault economy not available"
            );
        }

        return economy.getBalance(player);
    }

    @Override
    public boolean withdraw(Player player, double amount)
            throws ProviderFunctionalityNotFoundException {
        if (!isAvailable()) {
            throw new ProviderFunctionalityNotFoundException(
                "EconomyProvider",
                "withdraw",
                "Vault economy not available"
            );
        }

        if (!economy.has(player, amount)) {
            return false;
        }

        return economy.withdrawPlayer(player, amount).transactionSuccess();
    }

    @Override
    public boolean deposit(Player player, double amount)
            throws ProviderFunctionalityNotFoundException {
        if (!isAvailable()) {
            throw new ProviderFunctionalityNotFoundException(
                "EconomyProvider",
                "deposit",
                "Vault economy not available"
            );
        }

        return economy.depositPlayer(player, amount).transactionSuccess();
    }

    @Override
    public double getFactionBalance(UUID accountId)
            throws ProviderFunctionalityNotFoundException {
        if (!isAvailable()) {
            throw new ProviderFunctionalityNotFoundException(
                "EconomyProvider",
                "getFactionBalance",
                "Vault economy not available"
            );
        }

        // Faction accounts werden als OfflinePlayer mit UUID behandelt
        OfflinePlayer account = Bukkit.getOfflinePlayer(accountId);
        return economy.getBalance(account);
    }

    @Override
    public boolean withdrawFaction(UUID accountId, double amount)
            throws ProviderFunctionalityNotFoundException {
        if (!isAvailable()) {
            throw new ProviderFunctionalityNotFoundException(
                "EconomyProvider",
                "withdrawFaction",
                "Vault economy not available"
            );
        }

        OfflinePlayer account = Bukkit.getOfflinePlayer(accountId);

        if (!economy.has(account, amount)) {
            return false;
        }

        return economy.withdrawPlayer(account, amount).transactionSuccess();
    }

    @Override
    public boolean depositFaction(UUID accountId, double amount)
            throws ProviderFunctionalityNotFoundException {
        if (!isAvailable()) {
            throw new ProviderFunctionalityNotFoundException(
                "EconomyProvider",
                "depositFaction",
                "Vault economy not available"
            );
        }

        OfflinePlayer account = Bukkit.getOfflinePlayer(accountId);
        return economy.depositPlayer(account, amount).transactionSuccess();
    }
}
