package de.fallenstar.storage;

import de.fallenstar.core.event.ProvidersReadyEvent;
import de.fallenstar.core.exception.ProviderFunctionalityNotFoundException;
import de.fallenstar.core.provider.PlotProvider;
import de.fallenstar.core.registry.ProviderRegistry;
import de.fallenstar.storage.command.StorageInfoCommand;
import de.fallenstar.storage.command.StorageListCommand;
import de.fallenstar.storage.command.StorageSetReceiverCommand;
import de.fallenstar.storage.listener.ChestInteractListener;
import de.fallenstar.storage.manager.StorageManager;
import de.fallenstar.storage.storageprovider.PlotStorageProvider;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Storage-Modul - Plot-basiertes Truhen-Storage-System.
 *
 * Funktionen:
 * - Automatisches Scannen aller Truhen auf Grundstücken
 * - Material-Tracking und Bestandsverwaltung
 * - Empfangskisten für Material-Transfer
 * - Commands zur Verwaltung und Anzeige
 *
 * Abhängigkeiten:
 * - Core Plugin (required)
 * - PlotProvider (required für Plot-Storage)
 *
 * @author FallenStar
 * @version 1.0
 */
public class StorageModule extends JavaPlugin implements Listener {

    private ProviderRegistry providers;
    private PlotStorageProvider storageProvider;
    private StorageManager storageManager;
    private boolean plotBasedStorageEnabled = false;

    @Override
    public void onEnable() {
        getLogger().info("=== Storage Module Starting ===");

        // Warte auf ProvidersReadyEvent
        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        getLogger().info("Storage Module disabled");
    }

    /**
     * Wird gefeuert wenn alle Provider registriert sind.
     */
    @EventHandler
    public void onProvidersReady(ProvidersReadyEvent event) {
        this.providers = event.getRegistry();

        getLogger().info("Providers ready - initializing Storage Module...");

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
        try {
            // PlotProvider ist KRITISCH für Storage-Modul
            PlotProvider plotProvider = providers.getPlotProvider();

            if (!plotProvider.isAvailable()) {
                getLogger().severe("PlotProvider nicht verfügbar - Storage-Modul benötigt Plot-System!");
                return false;
            }

            return true;

        } catch (Exception e) {
            getLogger().severe("Fehler beim Zugriff auf Provider: " + e.getMessage());
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
            getLogger().warning("○ Plot provider not available - storage disabled");
        }
    }

    /**
     * Initialisiert das Modul mit aktivierten Features.
     */
    private void initializeModule() {
        if (!plotBasedStorageEnabled) {
            getLogger().warning("Storage Module kann nicht ohne PlotProvider laufen!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        getLogger().info("Initialisiere Storage-System...");

        // Erstelle Storage-Provider und Manager
        this.storageProvider = new PlotStorageProvider();
        this.storageManager = new StorageManager(
            getLogger(),
            providers.getPlotProvider(),
            storageProvider
        );

        // Registriere Commands
        registerCommands();

        // Registriere Listener
        registerListeners();

        // Initialer Scan wird später durchgeführt (nach vollständigem Server-Start)
        // TODO: Asynchroner Initial-Scan aller Plots
        getLogger().info("=== Storage Module Initialized ===");
        getLogger().info("  Plot-Storage: enabled");
        getLogger().info("  Commands: /storage list, /storage info, /storage setreceiver");
    }

    /**
     * Registriert alle Commands.
     */
    private void registerCommands() {
        PlotProvider plotProvider = providers.getPlotProvider();

        getCommand("storage-list").setExecutor(
            new StorageListCommand(plotProvider, storageProvider)
        );

        getCommand("storage-info").setExecutor(
            new StorageInfoCommand(plotProvider, storageProvider)
        );

        getCommand("storage-setreceiver").setExecutor(
            new StorageSetReceiverCommand(plotProvider, storageManager)
        );

        getLogger().info("✓ Commands registriert");
    }

    /**
     * Registriert alle Event-Listener.
     */
    private void registerListeners() {
        PlotProvider plotProvider = providers.getPlotProvider();

        ChestInteractListener chestListener = new ChestInteractListener(
            getLogger(),
            plotProvider,
            storageProvider,
            storageManager
        );

        getServer().getPluginManager().registerEvents(chestListener, this);

        getLogger().info("✓ Listener registriert");
    }

    /**
     * Gibt den StorageManager zurück.
     *
     * @return Der StorageManager
     */
    public StorageManager getStorageManager() {
        return storageManager;
    }

    /**
     * Gibt den PlotStorageProvider zurück.
     *
     * @return Der PlotStorageProvider
     */
    public PlotStorageProvider getStorageProvider() {
        return storageProvider;
    }
}
