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
     * Items-Modul ist optional (graceful degradation via NoOpItemProvider).
     * Nur Vault ist kritisch für Economy-Funktionen.
     *
     * @return true wenn alle kritischen Dependencies vorhanden
     */
    private boolean checkRequiredDependencies() {
        // Prüfe Vault (kritisch für Economy-Features)
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            getLogger().severe("✗ Vault Plugin nicht gefunden!");
            return false;
        }

        // Items-Modul ist optional (für Münz-Features)
        if (getServer().getPluginManager().getPlugin("FallenStar-Items") == null) {
            getLogger().warning("○ FallenStar-Items nicht gefunden - Münz-Features deaktiviert");
            getLogger().info("  Economy läuft im reduzierten Modus (nur Preise, keine Münzen)");
        } else {
            getLogger().info("✓ FallenStar-Items gefunden - Münz-Features aktiviert");
        }

        getLogger().info("✓ Alle kritischen Dependencies verfügbar");
        return true;
    }

    /**
     * Initialisiert die Manager.
     *
     * Nutzt ItemProvider (Core-Interface) statt direktem Items-Modul Zugriff.
     * Eliminiert Module-Dependency: Economy → Items.
     */
    private void initializeManagers() {
        // Hole ItemProvider aus ProviderRegistry (eliminiert Items-Modul Dependency!)
        de.fallenstar.core.provider.ItemProvider itemProvider = providers.getItemProvider();

        if (itemProvider == null || !itemProvider.isAvailable()) {
            getLogger().warning("ItemProvider nicht verfügbar - Münz-Features deaktiviert!");
            getLogger().warning("Economy-Modul läuft im reduzierten Modus (nur Preise, keine Münzen)");
            // Graceful Degradation: CurrencyManager mit NoOpItemProvider
        }

        // Initialisiere CurrencyManager (nutzt ItemProvider statt SpecialItemManager)
        this.currencyManager = new CurrencyManager(getLogger(), itemProvider);

        // Initialisiere ItemBasePriceProvider
        this.priceProvider = new ItemBasePriceProvider(getLogger());
        priceProvider.loadFromConfig(getConfig());

        getLogger().info("✓ Manager initialisiert");
        getLogger().info("  - Item-Basispreise: " + priceProvider.getVanillaPriceCount() +
                " Vanilla, " + priceProvider.getCustomPriceCount() + " Custom");
        getLogger().info("  - ItemProvider: " + (itemProvider.isAvailable() ? "verfügbar" : "nicht verfügbar"));
    }

    /**
     * Registriert den VaultEconomyProvider in der ProviderRegistry.
     */
    private void registerEconomyProvider() {
        // Erstelle VaultEconomyProvider
        this.economyProvider = new VaultEconomyProvider(getLogger());

        // Setter-Injection: Gebe economyProvider Zugriff auf ItemBasePriceProvider
        economyProvider.setPriceProvider(priceProvider);
        getLogger().fine("✓ ItemBasePriceProvider in VaultEconomyProvider injiziert");

        // Setter-Injection: Gebe economyProvider Zugriff auf Plugin (für Config-Speicherung)
        economyProvider.setPlugin(this);
        getLogger().fine("✓ EconomyModule in VaultEconomyProvider injiziert");

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
