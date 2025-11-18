package de.fallenstar.core;

import de.fallenstar.core.command.CoreCommand;
import de.fallenstar.core.database.DataStore;
import de.fallenstar.core.database.impl.MySQLDataStore;
import de.fallenstar.core.database.impl.PostgreSQLDataStore;
import de.fallenstar.core.database.impl.SQLiteDataStore;
import de.fallenstar.core.event.ProvidersReadyEvent;
import de.fallenstar.core.registry.AdminCommandRegistry;
import de.fallenstar.core.registry.PlotTypeRegistry;
import de.fallenstar.core.registry.ProviderRegistry;
import de.fallenstar.core.registry.UIRegistry;
import de.fallenstar.core.ui.BaseUI;
import de.fallenstar.core.ui.ConfirmationUI;
import de.fallenstar.core.ui.SimpleTradeUI;
import de.fallenstar.core.ui.manager.UIButtonManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * FallenStar Paper Core Plugin.
 * 
 * Zentrale Komponente des modularen Systems.
 * 
 * Verantwortlichkeiten:
 * - Provider-Registry initialisieren
 * - DataStore bereitstellen
 * - ProvidersReadyEvent feuern
 * - Koordination zwischen Modulen
 * 
 * Keine Business-Logic!
 * Alle Features werden in Modulen implementiert.
 * 
 * @author FallenStar
 * @version 1.0
 */
public class FallenStarCore extends JavaPlugin {

    private ProviderRegistry providerRegistry;
    private PlotTypeRegistry plotTypeRegistry;
    private UIRegistry uiRegistry;
    private AdminCommandRegistry adminCommandRegistry;
    private UIButtonManager uiButtonManager;
    private DataStore dataStore;
    
    @Override
    public void onEnable() {
        getLogger().info("╔════════════════════════════════════╗");
        getLogger().info("║   FallenStar Paper Core v1.0      ║");
        getLogger().info("║   Modular Plugin System            ║");
        getLogger().info("╚════════════════════════════════════╝");

        // Config laden/erstellen
        saveDefaultConfig();

        // UI-Framework initialisieren (Plugin-Instanz setzen)
        BaseUI.setPlugin(this);
        getLogger().info("✓ UI-Framework initialized");

        // DataStore initialisieren
        initializeDataStore();

        // Commands registrieren
        registerCommands();

        // Provider-Registry und Module-Benachrichtigung mit Verzögerung
        // um sicherzustellen, dass alle Plugins geladen sind
        Bukkit.getScheduler().runTask(this, () -> {
            // Provider-Registry initialisieren
            initializeProviders();

            // Module informieren dass Provider bereit sind
            notifyModules();

            getLogger().info("✓ Core initialization complete!");
        });
    }
    
    /**
     * Registriert alle Commands.
     */
    private void registerCommands() {
        CoreCommand coreCommand = new CoreCommand(this);
        getCommand("fscore").setExecutor(coreCommand);
        getCommand("fscore").setTabCompleter(coreCommand);
        getLogger().info("✓ Commands registered");
    }

    /**
     * Initialisiert den DataStore basierend auf Config.
     */
    private void initializeDataStore() {
        String storeType = getConfig().getString("database.type", "sqlite");

        try {
            switch (storeType.toLowerCase()) {
                case "sqlite" -> {
                    dataStore = new SQLiteDataStore(getDataFolder());
                    getLogger().info("✓ DataStore: SQLite");
                }
                case "mysql" -> {
                    dataStore = new MySQLDataStore(
                        getConfig().getConfigurationSection("database.mysql"),
                        getLogger()
                    );
                    getLogger().info("✓ DataStore: MySQL");
                }
                case "postgresql" -> {
                    dataStore = new PostgreSQLDataStore(
                        getConfig().getConfigurationSection("database.postgresql"),
                        getLogger()
                    );
                    getLogger().info("✓ DataStore: PostgreSQL");
                }
                default -> {
                    getLogger().warning("Unknown database type '" + storeType + "', falling back to SQLite");
                    dataStore = new SQLiteDataStore(getDataFolder());
                }
            }
        } catch (Exception e) {
            getLogger().severe("Failed to initialize DataStore: " + e.getMessage());
            getLogger().warning("Falling back to SQLite");
            dataStore = new SQLiteDataStore(getDataFolder());
        }
    }
    
    /**
     * Initialisiert die Provider-Registry.
     *
     * Auto-Detection aller verfügbaren Plugins und
     * Registrierung entsprechender Provider.
     */
    private void initializeProviders() {
        providerRegistry = new ProviderRegistry(this, getLogger());
        providerRegistry.detectAndRegister();

        // PlotTypeRegistry initialisieren
        plotTypeRegistry = new PlotTypeRegistry(getLogger());
        getLogger().info("✓ PlotTypeRegistry initialized");

        // UIRegistry initialisieren
        uiRegistry = new UIRegistry(getLogger());
        getLogger().info("✓ UIRegistry initialized");

        // AdminCommandRegistry initialisieren
        adminCommandRegistry = new AdminCommandRegistry(getLogger());
        getLogger().info("✓ AdminCommandRegistry initialized");

        // UIButtonManager initialisieren
        uiButtonManager = new UIButtonManager();
        uiButtonManager.initialize();
        getLogger().info("✓ UIButtonManager initialized");

        // Test-UIs registrieren
        registerTestUIs();
    }
    
    /**
     * Registriert Test-UIs in der UIRegistry.
     *
     * Diese UIs sind für Testzwecke und Demos verfügbar.
     */
    private void registerTestUIs() {
        // ConfirmationUI registrieren
        uiRegistry.registerUI(
                "confirm",
                "Bestätigungs-Dialog (Ja/Nein)",
                "Generisches Ja/Nein Confirmation UI",
                () -> ConfirmationUI.createSimple(
                        this,
                        uiButtonManager,
                        "Bist du sicher?",
                        player -> player.sendMessage("§a✓ Bestätigt!")
                )
        );
        getLogger().info("✓ ConfirmationUI registered (ID: confirm)");

        // SimpleTradeUI registrieren
        uiRegistry.registerUI(
                "trade",
                "Händler-Demo (Vanilla Items)",
                "Test-Händler mit Vanilla Merchant-Interface",
                () -> new SimpleTradeUI(uiButtonManager)
        );
        getLogger().info("✓ SimpleTradeUI registered (ID: trade)");

        getLogger().info("Alle Test-UIs registriert!");
        getLogger().info("Verwende: /fscore admin gui list");
    }

    /**
     * Informiert alle Module dass Provider bereit sind.
     *
     * Module können auf ProvidersReadyEvent reagieren und
     * ihre Provider-abhängigen Features initialisieren.
     */
    private void notifyModules() {
        ProvidersReadyEvent event = new ProvidersReadyEvent(providerRegistry);
        Bukkit.getPluginManager().callEvent(event);
        getLogger().info("✓ ProvidersReadyEvent fired");
    }
    
    @Override
    public void onDisable() {
        getLogger().info("Shutting down FallenStar Core...");
        
        // DataStore ordentlich schließen
        if (dataStore != null) {
            dataStore.shutdown();
        }
        
        getLogger().info("Core shutdown complete");
    }
    
    /**
     * API-Methode: Gibt die Provider-Registry zurück.
     *
     * Wird von Modulen genutzt um Provider zu erhalten.
     *
     * @return ProviderRegistry
     */
    public ProviderRegistry getProviderRegistry() {
        return providerRegistry;
    }

    /**
     * API-Methode: Gibt die PlotTypeRegistry zurück.
     *
     * Wird von Modulen genutzt um Plot-Typen zu verwalten.
     *
     * @return PlotTypeRegistry
     */
    public PlotTypeRegistry getPlotTypeRegistry() {
        return plotTypeRegistry;
    }

    /**
     * API-Methode: Gibt die UIRegistry zurück.
     *
     * Wird von Modulen genutzt um Test-UIs zu registrieren.
     *
     * @return UIRegistry
     */
    public UIRegistry getUIRegistry() {
        return uiRegistry;
    }

    /**
     * API-Methode: Gibt den DataStore zurück.
     *
     * Wird von Modulen genutzt um Daten zu speichern/laden.
     *
     * @return DataStore
     */
    public DataStore getDataStore() {
        return dataStore;
    }

    /**
     * API-Methode: Gibt die AdminCommandRegistry zurück.
     *
     * Wird von Modulen genutzt um Admin-Subcommand-Handler zu registrieren.
     *
     * @return AdminCommandRegistry
     */
    public AdminCommandRegistry getAdminCommandRegistry() {
        return adminCommandRegistry;
    }

    /**
     * API-Methode: Gibt den UIButtonManager zurück.
     *
     * Wird von Modulen genutzt um UI-Buttons zu erstellen.
     *
     * @return UIButtonManager
     */
    public UIButtonManager getUIButtonManager() {
        return uiButtonManager;
    }
}
