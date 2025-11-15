package de.fallenstar.storage;

import de.fallenstar.core.FallenStarCore;
import de.fallenstar.core.event.ProvidersReadyEvent;
import de.fallenstar.core.exception.ProviderFunctionalityNotFoundException;
import de.fallenstar.core.provider.PlotProvider;
import de.fallenstar.core.registry.ProviderRegistry;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Storage-Modul - Beispiel für Provider-Nutzung mit Graceful Degradation.
 * 
 * Zeigt:
 * - Provider-Zugriff über Registry
 * - Feature-Detection beim Start
 * - Graceful Degradation bei fehlenden Features
 * - Dry-Run von kritischen Features
 * 
 * @author FallenStar
 * @version 1.0
 */
public class StorageModule extends JavaPlugin implements Listener {
    
    private ProviderRegistry providers;
    private boolean plotBasedStorageEnabled = false;
    
    @Override
    public void onEnable() {
        getLogger().info("Storage Module starting...");
        
        // Warte auf ProvidersReadyEvent
        getServer().getPluginManager().registerEvents(this, this);
    }
    
    /**
     * Wird gefeuert wenn alle Provider registriert sind.
     */
    @EventHandler
    public void onProvidersReady(ProvidersReadyEvent event) {
        this.providers = event.getRegistry();
        
        // KRITISCHE Features prüfen (required)
        if (!checkRequiredFeatures()) {
            getLogger().severe("Required providers not available!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        
        // OPTIONALE Features prüfen
        checkOptionalFeatures();
        
        // Module vollständig initialisieren
        initializeModule();
    }
    
    /**
     * Prüft ob alle kritischen Provider verfügbar sind.
     * Modul wird deaktiviert wenn diese fehlen.
     */
    private boolean checkRequiredFeatures() {
        // Economy ist für Storage-Modul optional aber praktisch
        // Hier als Beispiel für "required" Features
        
        // Dry-Run: Teste ob grundlegende Funktionen verfügbar sind
        try {
            // Teste ob grundlegende Provider-Calls funktionieren
            boolean economyAvailable = providers.getEconomyProvider().isAvailable();
            
            if (!economyAvailable) {
                getLogger().warning("Economy provider not available - limited functionality");
            }
            
            return true; // Für Storage-Modul ist alles optional
            
        } catch (Exception e) {
            getLogger().severe("Failed to access providers: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Prüft welche optionalen Features verfügbar sind.
     * Features werden aktiviert/deaktiviert basierend auf Provider-Verfügbarkeit.
     */
    private void checkOptionalFeatures() {
        PlotProvider plotProvider = providers.getPlotProvider();
        
        // Test ob Plot-basierte Features verfügbar sind
        if (plotProvider.isAvailable()) {
            try {
                // Dry-Run einer Plot-Operation
                plotProvider.getPlot(null); // Wird null zurückgeben, aber nicht crashen
                
                plotBasedStorageEnabled = true;
                getLogger().info("✓ Plot-based storage enabled");
                
            } catch (ProviderFunctionalityNotFoundException e) {
                plotBasedStorageEnabled = false;
                getLogger().warning("✗ Plot-based storage disabled: " + e.getMessage());
            }
        } else {
            getLogger().info("○ Plot provider not available - global storage only");
        }
    }
    
    /**
     * Initialisiert das Modul mit aktivierten Features.
     */
    private void initializeModule() {
        getLogger().info("Storage Module initialized");
        getLogger().info("  Plot-based storage: " + (plotBasedStorageEnabled ? "enabled" : "disabled"));
        
        // Lade Configs, Commands, Listener etc.
        // ...
    }
    
    /**
     * Beispiel-Methode die Provider nutzt mit Fallback.
     */
    public void exampleMethodWithFallback() {
        if (plotBasedStorageEnabled) {
            try {
                // Nutze Plot-basierte Funktionalität
                // ...
            } catch (ProviderFunctionalityNotFoundException e) {
                // Fallback zu alternativer Logik
                getLogger().warning("Plot feature unavailable, using fallback");
            }
        } else {
            // Alternative Implementierung ohne Plots
            // ...
        }
    }
}
