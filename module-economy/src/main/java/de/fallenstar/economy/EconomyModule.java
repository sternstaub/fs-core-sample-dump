package de.fallenstar.economy;

import de.fallenstar.core.FallenStarCore;
import de.fallenstar.core.event.ProvidersReadyEvent;
import de.fallenstar.core.registry.AdminCommandRegistry;
import de.fallenstar.core.registry.ProviderRegistry;
import de.fallenstar.economy.command.EconomyAdminHandler;
import de.fallenstar.economy.manager.CurrencyManager;
import de.fallenstar.economy.model.CurrencyItemSet;
import de.fallenstar.economy.pricing.ItemBasePriceProvider;
import de.fallenstar.economy.provider.VaultEconomyProvider;
import de.fallenstar.items.manager.SpecialItemManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Economy-Modul für FallenStar.
 *
 * Funktionen:
 * - Weltwirtschaft mit mehreren Währungen
 * - Münzsystem (Bronze/Silber/Gold)
 * - Wechselkurse und Preisberechnungen
 * - Vault-Integration
 * - Item-Basispreise (für Handelsgilden)
 *
 * Abhängigkeiten:
 * - FallenStar-Core (ProviderRegistry)
 * - FallenStar-Items (SpecialItemManager)
 * - Vault (Economy-API)
 *
 * @author FallenStar
 * @version 2.0
 */
public class EconomyModule extends JavaPlugin implements Listener {

    private ProviderRegistry providers;
    private CurrencyManager currencyManager;
    private ItemBasePriceProvider priceProvider;
    private SpecialItemManager itemManager;
    private VaultEconomyProvider economyProvider;

    @Override
    public void onEnable() {
        getLogger().info("FallenStar Economy Modul wird gestartet...");

        // Config speichern (falls nicht vorhanden)
        saveDefaultConfig();

        // Event-Listener registrieren
        getServer().getPluginManager().registerEvents(this, this);

        getLogger().info("Economy-Modul wartet auf Provider-Initialisierung...");
    }

    @Override
    public void onDisable() {
        getLogger().info("FallenStar Economy Modul wird gestoppt.");
    }

    /**
     * Wird aufgerufen wenn Core alle Provider initialisiert hat.
     *
     * @param event ProvidersReadyEvent
     */
    @EventHandler
    public void onProvidersReady(ProvidersReadyEvent event) {
        this.providers = event.getRegistry();

        getLogger().info("Provider verfügbar - Economy-Modul wird initialisiert...");

        // Prüfe erforderliche Dependencies
        if (!checkRequiredDependencies()) {
            getLogger().severe("Economy-Modul kann nicht geladen werden - fehlende Dependencies!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Initialisiere Module
        initializeManagers();
        registerEconomyProvider();

        // Setze EconomyProvider im CurrencyManager (für withdrawCoins)
        currencyManager.setEconomyProvider(economyProvider);

        registerBaseCurrency();
        registerAdminCommands();

        getLogger().info("✓ Economy-Modul erfolgreich initialisiert!");
        getLogger().info("  - Registrierte Währungen: " + currencyManager.getCurrencyCount());

        String economyProviderName = "Nicht verfügbar";
        if (economyProvider.isAvailable()) {
            try {
                economyProviderName = economyProvider.getEconomyName();
            } catch (Exception e) {
                economyProviderName = "Fehler: " + e.getMessage();
            }
        }
        getLogger().info("  - Economy Provider: " + economyProviderName);
    }

    /**
     * Prüft ob alle erforderlichen Dependencies verfügbar sind.
     *
     * @return true wenn alle Dependencies vorhanden
     */
    private boolean checkRequiredDependencies() {
        // Prüfe Items-Modul
        if (getServer().getPluginManager().getPlugin("FallenStar-Items") == null) {
            getLogger().severe("✗ FallenStar-Items Plugin nicht gefunden!");
            return false;
        }

        // Prüfe Vault
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            getLogger().severe("✗ Vault Plugin nicht gefunden!");
            return false;
        }

        getLogger().info("✓ Alle Dependencies verfügbar");
        return true;
    }

    /**
     * Initialisiert die Manager.
     */
    private void initializeManagers() {
        // Hole SpecialItemManager vom Items-Modul
        de.fallenstar.items.ItemsModule itemsModule =
                (de.fallenstar.items.ItemsModule) getServer().getPluginManager().getPlugin("FallenStar-Items");

        if (itemsModule == null) {
            getLogger().severe("Items-Modul nicht verfügbar!");
            return;
        }

        this.itemManager = itemsModule.getSpecialItemManager();

        // Initialisiere CurrencyManager
        this.currencyManager = new CurrencyManager(getLogger(), itemManager);

        // Initialisiere ItemBasePriceProvider
        this.priceProvider = new ItemBasePriceProvider(getLogger());
        priceProvider.loadFromConfig(getConfig());

        getLogger().info("✓ Manager initialisiert");
        getLogger().info("  - Item-Basispreise: " + priceProvider.getVanillaPriceCount() +
                " Vanilla, " + priceProvider.getCustomPriceCount() + " Custom");
    }

    /**
     * Registriert den VaultEconomyProvider in der ProviderRegistry.
     */
    private void registerEconomyProvider() {
        // Erstelle VaultEconomyProvider
        this.economyProvider = new VaultEconomyProvider(getLogger());

        // Registriere in ProviderRegistry
        providers.setEconomyProvider(economyProvider);

        if (economyProvider.isAvailable()) {
            try {
                getLogger().info("✓ VaultEconomyProvider registriert: " + economyProvider.getEconomyName());
            } catch (Exception e) {
                getLogger().info("✓ VaultEconomyProvider registriert (Name nicht abrufbar)");
            }
        } else {
            getLogger().warning("✗ VaultEconomyProvider nicht verfügbar (Vault-Plugin fehlt?)");
        }
    }

    /**
     * Registriert die Basiswährung "Sterne".
     */
    private void registerBaseCurrency() {
        CurrencyItemSet sterne = CurrencyItemSet.createBaseCurrency();
        currencyManager.registerCurrency(sterne);

        getLogger().info("✓ Basiswährung registriert: " + sterne.displayName() +
                " (Wechselkurs: " + sterne.exchangeRate() + ")");
    }

    /**
     * Registriert Admin-Command-Handler in der Core AdminCommandRegistry.
     */
    private void registerAdminCommands() {
        // Hole Core Plugin
        FallenStarCore core = (FallenStarCore) getServer().getPluginManager().getPlugin("FallenStar-Core");
        if (core == null) {
            getLogger().warning("✗ Core nicht verfügbar - Admin-Commands können nicht registriert werden");
            return;
        }

        // Hole AdminCommandRegistry
        AdminCommandRegistry registry = core.getAdminCommandRegistry();
        if (registry == null) {
            getLogger().warning("✗ AdminCommandRegistry nicht verfügbar");
            return;
        }

        // Erstelle und registriere EconomyAdminHandler
        EconomyAdminHandler handler = new EconomyAdminHandler(currencyManager, providers);
        registry.registerHandler("economy", handler);

        getLogger().info("✓ Admin-Commands registriert");
    }

    // ==================== Getter ====================

    /**
     * Gibt den CurrencyManager zurück.
     *
     * @return CurrencyManager
     */
    public CurrencyManager getCurrencyManager() {
        return currencyManager;
    }

    /**
     * Gibt den ItemBasePriceProvider zurück.
     *
     * @return ItemBasePriceProvider
     */
    public ItemBasePriceProvider getPriceProvider() {
        return priceProvider;
    }

    /**
     * Gibt die ProviderRegistry zurück.
     *
     * @return ProviderRegistry
     */
    public ProviderRegistry getProviders() {
        return providers;
    }

    /**
     * Speichert die Config zurück auf die Festplatte.
     *
     * Diese Methode wird nach Preis-Änderungen aufgerufen.
     */
    public void saveConfiguration() {
        // Speichere Preise in Config
        priceProvider.saveToConfig(getConfig());

        // Schreibe Config auf Festplatte
        saveConfig();

        getLogger().fine("Config gespeichert");
    }
}
