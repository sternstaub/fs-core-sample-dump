package de.fallenstar.economy.provider;

import de.fallenstar.core.exception.ProviderFunctionalityNotFoundException;
import de.fallenstar.core.provider.EconomyProvider;
import de.fallenstar.economy.pricing.ItemBasePriceProvider;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Vault-basierter Economy Provider.
 *
 * Nutzt die Vault Economy API für Balance-Verwaltung.
 * Integration mit dem Minecraft-Wirtschaftssystem.
 *
 * Features:
 * - Spieler-Balances (getBalance, withdraw, deposit, setBalance)
 * - Konten-Verwaltung (hasAccount, createAccount)
 * - Währungs-Formatierung (format, getCurrencyName)
 * - Fraktions-/Stadt-Konten (future)
 * - Vault-Kompatibilität
 *
 * @author FallenStar
 * @version 2.0
 */
public class VaultEconomyProvider implements EconomyProvider {

    private final Logger logger;
    private Economy vaultEconomy;
    private boolean available;
    private ItemBasePriceProvider priceProvider; // Setter-injected
    private de.fallenstar.economy.EconomyModule plugin; // Setter-injected (für Config-Speicherung)

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
     * Setzt den ItemBasePriceProvider (Setter-Injection).
     *
     * @param priceProvider ItemBasePriceProvider-Instanz
     */
    public void setPriceProvider(ItemBasePriceProvider priceProvider) {
        this.priceProvider = priceProvider;
        logger.fine("ItemBasePriceProvider injected into VaultEconomyProvider");
    }

    /**
     * Setzt das Plugin (Setter-Injection für Config-Speicherung).
     *
     * @param plugin EconomyModule-Instanz
     */
    public void setPlugin(de.fallenstar.economy.EconomyModule plugin) {
        this.plugin = plugin;
        logger.fine("EconomyModule injected into VaultEconomyProvider");
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

    @Override
    public boolean setBalance(Player player, double amount) throws ProviderFunctionalityNotFoundException {
        if (!isAvailable()) {
            throw new ProviderFunctionalityNotFoundException(
                    "EconomyProvider",
                    "setBalance",
                    "Vault Economy nicht verfügbar"
            );
        }

        if (amount < 0) {
            logger.warning("Versuch, negativen Kontostand zu setzen: " + amount);
            return false;
        }

        // Vault hat keine direkte setBalance-Methode, wir nutzen withdraw/deposit
        double currentBalance = vaultEconomy.getBalance(player);
        double difference = amount - currentBalance;

        if (difference > 0) {
            // Einzahlen
            net.milkbowl.vault.economy.EconomyResponse response = vaultEconomy.depositPlayer(player, difference);
            return response.transactionSuccess();
        } else if (difference < 0) {
            // Abheben
            net.milkbowl.vault.economy.EconomyResponse response = vaultEconomy.withdrawPlayer(player, Math.abs(difference));
            return response.transactionSuccess();
        }

        return true; // Kontostand ist bereits korrekt
    }

    @Override
    public boolean hasAccount(OfflinePlayer player) throws ProviderFunctionalityNotFoundException {
        if (!isAvailable()) {
            throw new ProviderFunctionalityNotFoundException(
                    "EconomyProvider",
                    "hasAccount",
                    "Vault Economy nicht verfügbar"
            );
        }

        return vaultEconomy.hasAccount(player);
    }

    @Override
    public boolean createAccount(OfflinePlayer player) throws ProviderFunctionalityNotFoundException {
        if (!isAvailable()) {
            throw new ProviderFunctionalityNotFoundException(
                    "EconomyProvider",
                    "createAccount",
                    "Vault Economy nicht verfügbar"
            );
        }

        return vaultEconomy.createPlayerAccount(player);
    }

    @Override
    public String format(double amount) throws ProviderFunctionalityNotFoundException {
        if (!isAvailable()) {
            throw new ProviderFunctionalityNotFoundException(
                    "EconomyProvider",
                    "format",
                    "Vault Economy nicht verfügbar"
            );
        }

        return vaultEconomy.format(amount);
    }

    @Override
    public String getEconomyName() throws ProviderFunctionalityNotFoundException {
        if (!isAvailable()) {
            throw new ProviderFunctionalityNotFoundException(
                    "EconomyProvider",
                    "getEconomyName",
                    "Vault Economy nicht verfügbar"
            );
        }

        return vaultEconomy.getName();
    }

    @Override
    public String getCurrencyNameSingular() throws ProviderFunctionalityNotFoundException {
        if (!isAvailable()) {
            throw new ProviderFunctionalityNotFoundException(
                    "EconomyProvider",
                    "getCurrencyNameSingular",
                    "Vault Economy nicht verfügbar"
            );
        }

        return vaultEconomy.currencyNameSingular();
    }

    @Override
    public String getCurrencyNamePlural() throws ProviderFunctionalityNotFoundException {
        if (!isAvailable()) {
            throw new ProviderFunctionalityNotFoundException(
                    "EconomyProvider",
                    "getCurrencyNamePlural",
                    "Vault Economy nicht verfügbar"
            );
        }

        return vaultEconomy.currencyNamePlural();
    }

    // ================== Item-Preis-Verwaltung (delegiert an ItemBasePriceProvider) ==================

    @Override
    public Optional<BigDecimal> getBuyPrice(Material material)
            throws ProviderFunctionalityNotFoundException {
        if (priceProvider == null) {
            logger.warning("ItemBasePriceProvider nicht verfügbar - kann Buy-Preis nicht abrufen!");
            return Optional.empty();
        }

        // Hole VanillaItemPrice und extrahiere buyPrice
        return priceProvider.getVanillaPrice(material)
                .map(price -> price.buyPrice());
    }

    @Override
    public Optional<BigDecimal> getSellPrice(Material material)
            throws ProviderFunctionalityNotFoundException {
        if (priceProvider == null) {
            logger.warning("ItemBasePriceProvider nicht verfügbar - kann Sell-Preis nicht abrufen!");
            return Optional.empty();
        }

        // Hole VanillaItemPrice und extrahiere sellPrice
        return priceProvider.getVanillaPrice(material)
                .map(price -> price.sellPrice());
    }

    @Override
    public boolean setItemPrice(Material material, BigDecimal buyPrice, BigDecimal sellPrice)
            throws ProviderFunctionalityNotFoundException {
        if (priceProvider == null) {
            logger.warning("ItemBasePriceProvider nicht verfügbar - kann Preis nicht setzen!");
            return false;
        }

        // Delegiere an ItemBasePriceProvider
        priceProvider.registerVanillaPrice(material, buyPrice, sellPrice);

        // Speichere Config automatisch (eliminiert Reflection in PlotPriceCommand!)
        if (plugin != null) {
            plugin.saveConfiguration();
            logger.fine("Config automatisch nach setItemPrice() gespeichert");
        } else {
            logger.warning("Plugin nicht verfügbar - Config nicht gespeichert!");
        }

        return true;
    }

    @Override
    public Collection<Material> getAllPricedMaterials()
            throws ProviderFunctionalityNotFoundException {
        if (priceProvider == null) {
            logger.warning("ItemBasePriceProvider nicht verfügbar - kann Material-Liste nicht abrufen!");
            return java.util.Collections.emptyList();
        }

        // Hole alle VanillaItemPrice Objekte und extrahiere Material
        return priceProvider.getAllVanillaPrices().stream()
                .map(price -> price.material())
                .collect(Collectors.toList());
    }
}
