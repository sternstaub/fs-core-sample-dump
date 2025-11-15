package de.fallenstar.core;

import de.fallenstar.core.database.DataStore;
import de.fallenstar.core.database.impl.SQLiteDataStore;
import de.fallenstar.core.event.ProvidersReadyEvent;
import de.fallenstar.core.registry.ProviderRegistry;
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
    private DataStore dataStore;
    
    @Override
    public void onEnable() {
        getLogger().info("╔════════════════════════════════════╗");
        getLogger().info("║   FallenStar Paper Core v1.0      ║");
        getLogger().info("║   Modular Plugin System            ║");
        getLogger().info("╚════════════════════════════════════╝");
        
        // Config laden/erstellen
        saveDefaultConfig();
        
        // DataStore initialisieren
        initializeDataStore();
        
        // Provider-Registry initialisieren
        initializeProviders();
        
        // Module informieren dass Provider bereit sind
        notifyModules();
        
        getLogger().info("Core initialization complete!");
    }
    
    /**
     * Initialisiert den DataStore basierend auf Config.
     */
    private void initializeDataStore() {
        String storeType = getConfig().getString("database.type", "sqlite");
        
        switch (storeType.toLowerCase()) {
            case "sqlite":
                dataStore = new SQLiteDataStore(getDataFolder());
                getLogger().info("✓ DataStore: SQLite");
                break;
            case "mysql":
                // dataStore = new MySQLDataStore(config);
                getLogger().info("✓ DataStore: MySQL");
                break;
            default:
                getLogger().warning("Unknown database type, falling back to SQLite");
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
        providerRegistry = new ProviderRegistry(getLogger());
        providerRegistry.detectAndRegister();
    }
    
    /**
     * Informiert alle Module dass Provider bereit sind.
     * 
     * Module können auf ProvidersReadyEvent reagieren und
     * ihre Provider-abhängigen Features initialisieren.
     */
    private void notifyModules() {
        // Event erst im nächsten Tick feuern, damit alle Plugins geladen sind
        Bukkit.getScheduler().runTask(this, () -> {
            ProvidersReadyEvent event = new ProvidersReadyEvent(providerRegistry);
            Bukkit.getPluginManager().callEvent(event);
            getLogger().info("✓ ProvidersReadyEvent fired");
        });
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
     * API-Methode: Gibt den DataStore zurück.
     * 
     * Wird von Modulen genutzt um Daten zu speichern/laden.
     * 
     * @return DataStore
     */
    public DataStore getDataStore() {
        return dataStore;
    }
}
