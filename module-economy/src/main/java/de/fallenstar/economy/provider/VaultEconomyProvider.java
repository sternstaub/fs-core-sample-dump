package de.fallenstar.economy.provider;

import de.fallenstar.core.exception.ProviderFunctionalityNotFoundException;
import de.fallenstar.core.provider.EconomyProvider;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.UUID;
import java.util.logging.Logger;

/**
 * Vault-basierter Economy Provider.
 *
 * Nutzt die Vault Economy API für Balance-Verwaltung.
 * Integration mit dem Minecraft-Wirtschaftssystem.
 *
 * Features:
 * - Spieler-Balances (getBalance, withdraw, deposit)
 * - Fraktions-/Stadt-Konten (future)
 * - Vault-Kompatibilität
 *
 * @author FallenStar
 * @version 1.0
 */
public class VaultEconomyProvider implements EconomyProvider {

    private final Logger logger;
    private Economy vaultEconomy;
    private boolean available;

    /**
     * Konstruktor für VaultEconomyProvider.
     *
     * @param logger Logger-Instanz
     */
    public VaultEconomyProvider(Logger logger) {
        this.logger = logger;
        this.available = setupEconomy();
    }

    /**
     * Initialisiert die Vault Economy API.
     *
     * @return true wenn erfolgreich
     */
    private boolean setupEconomy() {
        if (Bukkit.getServer().getPluginManager().getPlugin("Vault") == null) {
            logger.warning("Vault nicht gefunden - Economy-Provider nicht verfügbar!");
            return false;
        }

        RegisteredServiceProvider<Economy> rsp = Bukkit.getServer()
                .getServicesManager()
                .getRegistration(Economy.class);

        if (rsp == null) {
            logger.warning("Keine Vault Economy Implementation gefunden!");
            return false;
        }

        vaultEconomy = rsp.getProvider();
        logger.info("✓ Vault Economy API initialisiert: " + vaultEconomy.getName());
        return true;
    }

    @Override
    public boolean isAvailable() {
        return available && vaultEconomy != null;
    }

    @Override
    public double getBalance(Player player) throws ProviderFunctionalityNotFoundException {
        if (!isAvailable()) {
            throw new ProviderFunctionalityNotFoundException(
                    "EconomyProvider",
                    "getBalance",
                    "Vault Economy nicht verfügbar"
            );
        }

        return vaultEconomy.getBalance(player);
    }

    @Override
    public boolean withdraw(Player player, double amount) throws ProviderFunctionalityNotFoundException {
        if (!isAvailable()) {
            throw new ProviderFunctionalityNotFoundException(
                    "EconomyProvider",
                    "withdraw",
                    "Vault Economy nicht verfügbar"
            );
        }

        if (amount <= 0) {
            logger.warning("Versuch, negativen/null Betrag abzuheben: " + amount);
            return false;
        }

        if (vaultEconomy.getBalance(player) < amount) {
            return false; // Nicht genug Guthaben
        }

        net.milkbowl.vault.economy.EconomyResponse response = vaultEconomy.withdrawPlayer(player, amount);
        return response.transactionSuccess();
    }

    @Override
    public boolean deposit(Player player, double amount) throws ProviderFunctionalityNotFoundException {
        if (!isAvailable()) {
            throw new ProviderFunctionalityNotFoundException(
                    "EconomyProvider",
                    "deposit",
                    "Vault Economy nicht verfügbar"
            );
        }

        if (amount <= 0) {
            logger.warning("Versuch, negativen/null Betrag einzuzahlen: " + amount);
            return false;
        }

        net.milkbowl.vault.economy.EconomyResponse response = vaultEconomy.depositPlayer(player, amount);
        return response.transactionSuccess();
    }

    @Override
    public double getFactionBalance(UUID accountId) throws ProviderFunctionalityNotFoundException {
        throw new ProviderFunctionalityNotFoundException(
                "EconomyProvider",
                "getFactionBalance",
                "Fraktions-Konten noch nicht implementiert"
        );
    }

    @Override
    public boolean withdrawFaction(UUID accountId, double amount) throws ProviderFunctionalityNotFoundException {
        throw new ProviderFunctionalityNotFoundException(
                "EconomyProvider",
                "withdrawFaction",
                "Fraktions-Konten noch nicht implementiert"
        );
    }

    @Override
    public boolean depositFaction(UUID accountId, double amount) throws ProviderFunctionalityNotFoundException {
        throw new ProviderFunctionalityNotFoundException(
                "EconomyProvider",
                "depositFaction",
                "Fraktions-Konten noch nicht implementiert"
        );
    }

    /**
     * Gibt den Namen des Economy-Plugins zurück.
     *
     * @return Economy-Plugin-Name
     */
    public String getEconomyName() {
        if (vaultEconomy != null) {
            return vaultEconomy.getName();
        }
        return "Unknown";
    }

    /**
     * Formatiert einen Betrag als Währungs-String.
     *
     * @param amount Betrag
     * @return Formatierter String (z.B. "$100.00")
     */
    public String format(double amount) {
        if (vaultEconomy != null) {
            return vaultEconomy.format(amount);
        }
        return String.format("%.2f", amount);
    }
}
