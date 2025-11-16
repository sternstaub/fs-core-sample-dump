package de.fallenstar.plot;

import com.palmergames.bukkit.towny.event.TownBlockTypeRegisterEvent;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.object.TownBlockData;
import com.palmergames.bukkit.towny.object.TownBlockType;
import com.palmergames.bukkit.towny.object.TownBlockTypeHandler;
import de.fallenstar.core.FallenStarCore;
import de.fallenstar.core.event.ProvidersReadyEvent;
import de.fallenstar.core.exception.ProviderFunctionalityNotFoundException;
import de.fallenstar.core.provider.PlotProvider;
import de.fallenstar.core.provider.TownProvider;
import de.fallenstar.core.provider.NPCProvider;
import de.fallenstar.core.registry.AdminCommandRegistry;
import de.fallenstar.core.registry.PlotTypeRegistry;
import de.fallenstar.core.registry.ProviderRegistry;
import de.fallenstar.plot.command.PlotCommand;
import de.fallenstar.plot.command.PlotsAdminHandler;
import de.fallenstar.plot.storage.listener.ChestInteractListener;
import de.fallenstar.plot.storage.manager.ChestScanService;
import de.fallenstar.plot.storage.manager.StorageManager;
import de.fallenstar.plot.storage.provider.PlotStorageProvider;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Plot-Modul - Plot-Verwaltung und NPC-System.
 *
 * Funktionen:
 * - Plot-Informationen anzeigen (Type, Owner, etc.)
 * - Storage-Verwaltung für Plots
 * - NPC-Spawning auf Plots
 * - Plot-Type-basierte Funktionalität
 *
 * Abhängigkeiten:
 * - Core Plugin (required)
 * - PlotProvider (required)
 * - TownProvider (optional für erweiterte Features)
 * - NPCProvider (optional für NPC-Spawning)
 *
 * @author FallenStar
 * @version 1.0
 */
public class PlotModule extends JavaPlugin implements Listener {

    private ProviderRegistry providers;
    private PlotTypeRegistry plotTypeRegistry;
    private FallenStarCore corePlugin;

    private PlotStorageProvider storageProvider;
    private StorageManager storageManager;

    private boolean plotSystemEnabled = false;
    private boolean townSystemEnabled = false;
    private boolean npcSystemEnabled = false;
    private boolean storageSystemEnabled = false;

    @Override
    public void onLoad() {
        // Registriere Custom-Plot-Typ in Towny (muss in onLoad() erfolgen)
        registerCustomPlotType();
    }

    @Override
    public void onEnable() {
        getLogger().info("=== Plot Module Starting ===");

        // Config laden/erstellen
        saveDefaultConfig();

        // Core-Plugin holen
        corePlugin = (FallenStarCore) getServer().getPluginManager().getPlugin("FallenStar-Core");
        if (corePlugin == null) {
            getLogger().severe("Core plugin not found! Disabling Plot Module.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Warte auf ProvidersReadyEvent
        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        getLogger().info("Plot Module disabled");
    }

    /**
     * Wird gefeuert wenn alle Provider registriert sind.
     */
    @EventHandler
    public void onProvidersReady(ProvidersReadyEvent event) {
        this.providers = event.getRegistry();
        this.plotTypeRegistry = corePlugin.getPlotTypeRegistry();

        getLogger().info("Providers ready - initializing Plot Module...");

        // KRITISCHE Features prüfen (required)
        if (!checkRequiredFeatures()) {
            getLogger().severe("Required providers not available!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // OPTIONALE Features prüfen
        checkOptionalFeatures();

        // Storage-System initialisieren (jetzt Teil des Plot-Moduls)
        initializeStorageSystem();

        // Module vollständig initialisieren
        initializeModule();
    }

    /**
     * Prüft ob alle kritischen Provider verfügbar sind.
     * Modul wird deaktiviert wenn diese fehlen.
     */
    private boolean checkRequiredFeatures() {
        try {
            // PlotProvider ist KRITISCH für Plot-Modul
            PlotProvider plotProvider = providers.getPlotProvider();

            if (!plotProvider.isAvailable()) {
                getLogger().severe("PlotProvider nicht verfügbar - Plot-Modul benötigt Plot-System!");
                return false;
            }

            plotSystemEnabled = true;
            getLogger().info("✓ PlotProvider available");
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
        // TownProvider
        TownProvider townProvider = providers.getTownProvider();
        if (townProvider != null && townProvider.isAvailable()) {
            try {
                // Test Town-Provider
                townProvider.getAllTowns();
                townSystemEnabled = true;
                getLogger().info("✓ TownProvider enabled (Town-Teleport verfügbar)");
            } catch (ProviderFunctionalityNotFoundException e) {
                townSystemEnabled = false;
                getLogger().warning("✗ TownProvider disabled: " + e.getMessage());
            }
        } else {
            getLogger().info("○ TownProvider not available");
        }

        // NPCProvider
        NPCProvider npcProvider = providers.getNpcProvider();
        if (npcProvider != null && npcProvider.isAvailable()) {
            npcSystemEnabled = true;
            getLogger().info("✓ NPCProvider enabled (NPC-Spawning verfügbar)");
        } else {
            npcSystemEnabled = false;
            getLogger().info("○ NPCProvider not available - NPC features disabled");
        }
    }

    /**
     * Initialisiert das Storage-System (integriert im Plot-Modul).
     */
    private void initializeStorageSystem() {
        PlotProvider plotProvider = providers.getPlotProvider();

        if (plotProvider.isAvailable()) {
            try {
                // Erstelle Storage-Provider und Manager
                this.storageProvider = new PlotStorageProvider();
                this.storageManager = new StorageManager(
                    getLogger(),
                    plotProvider,
                    storageProvider
                );

                // Registriere Storage-Listener
                ChestInteractListener chestListener = new ChestInteractListener(
                    getLogger(),
                    plotProvider,
                    storageProvider,
                    storageManager
                );
                getServer().getPluginManager().registerEvents(chestListener, this);

                storageSystemEnabled = true;
                getLogger().info("✓ Storage-System aktiviert");

            } catch (Exception e) {
                storageSystemEnabled = false;
                getLogger().warning("✗ Storage-System konnte nicht initialisiert werden: " + e.getMessage());
            }
        } else {
            storageSystemEnabled = false;
            getLogger().info("○ Storage-System deaktiviert (PlotProvider nicht verfügbar)");
        }
    }

    /**
     * Initialisiert das Modul mit aktivierten Features.
     */
    private void initializeModule() {
        getLogger().info("Initialisiere Plot-System...");

        // Registriere Commands
        registerCommands();
        registerAdminCommands();

        // Initialer Status-Log
        getLogger().info("=== Plot Module Initialized ===");
        getLogger().info("  Plot-System: " + (plotSystemEnabled ? "enabled" : "disabled"));
        getLogger().info("  Town-System: " + (townSystemEnabled ? "enabled" : "disabled"));
        getLogger().info("  NPC-System: " + (npcSystemEnabled ? "enabled" : "disabled"));
        getLogger().info("  Storage-System: " + (storageSystemEnabled ? "enabled" : "disabled"));
        getLogger().info("  Commands: /plot info, /plot storage, /plot npc");
    }

    /**
     * Registriert Custom-Plot-Typen ("botschaft", "handelsgilde") in Towny.
     *
     * Diese Methode wird in onLoad() aufgerufen und auch bei Towny-Reloads
     * via TownBlockTypeRegisterEvent.
     */
    private void registerCustomPlotType() {
        // Registriere "botschaft"
        if (!TownBlockTypeHandler.exists("botschaft")) {
            TownBlockType botschaftType = new TownBlockType("botschaft", new TownBlockData() {
                @Override
                public String getMapKey() {
                    return "B"; // 'B' für Botschaft auf der Map
                }

                @Override
                public double getCost() {
                    return 150.0; // Kosten zum Setzen des Plot-Typs (etwas teurer als Embassy)
                }
            });

            try {
                TownBlockTypeHandler.registerType(botschaftType);
                getLogger().info("✓ Custom-Plot-Typ 'botschaft' in Towny registriert");
            } catch (TownyException e) {
                getLogger().severe("✗ Fehler beim Registrieren von 'botschaft': " + e.getMessage());
            }
        }

        // Registriere "handelsgilde"
        if (!TownBlockTypeHandler.exists("handelsgilde")) {
            TownBlockType handelsgildeType = new TownBlockType("handelsgilde", new TownBlockData() {
                @Override
                public String getMapKey() {
                    return "H"; // 'H' für Handelsgilde auf der Map
                }

                @Override
                public double getCost() {
                    return 200.0; // Kosten zum Setzen (höher als Botschaft wegen Economy-Features)
                }
            });

            try {
                TownBlockTypeHandler.registerType(handelsgildeType);
                getLogger().info("✓ Custom-Plot-Typ 'handelsgilde' in Towny registriert");
            } catch (TownyException e) {
                getLogger().severe("✗ Fehler beim Registrieren von 'handelsgilde': " + e.getMessage());
            }
        }
    }

    /**
     * Event-Handler für Towny TownBlockTypeRegisterEvent.
     * Wird aufgerufen wenn Towny reloaded wird.
     */
    @EventHandler
    public void onTownBlockTypeRegister(TownBlockTypeRegisterEvent event) {
        registerCustomPlotType();
    }

    /**
     * Registriert alle Commands.
     */
    private void registerCommands() {
        PlotCommand plotCommand = new PlotCommand(
            this,
            providers,
            plotTypeRegistry,
            plotSystemEnabled,
            townSystemEnabled,
            npcSystemEnabled,
            storageSystemEnabled,
            storageProvider,
            storageManager
        );

        getCommand("plot").setExecutor(plotCommand);
        getCommand("plot").setTabCompleter(plotCommand);

        getLogger().info("✓ Commands registriert");
    }

    /**
     * Registriert Admin-Command-Handler in der Core AdminCommandRegistry.
     */
    private void registerAdminCommands() {
        // Hole Core Plugin
        if (corePlugin == null) {
            getLogger().warning("✗ Core nicht verfügbar - Admin-Commands können nicht registriert werden");
            return;
        }

        // Hole AdminCommandRegistry
        AdminCommandRegistry registry = corePlugin.getAdminCommandRegistry();
        if (registry == null) {
            getLogger().warning("✗ AdminCommandRegistry nicht verfügbar");
            return;
        }

        // Hole ChestScanService
        ChestScanService scanService = getScanService();
        if (scanService == null && storageSystemEnabled) {
            getLogger().warning("⚠ ChestScanService nicht verfügbar - Storage-Befehle möglicherweise eingeschränkt");
        }

        // Erstelle und registriere PlotsAdminHandler
        PlotsAdminHandler handler = new PlotsAdminHandler(providers, storageProvider, scanService);
        registry.registerHandler("plots", handler);

        getLogger().info("✓ Admin-Commands registriert");
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
     * Gibt die PlotTypeRegistry zurück.
     *
     * @return PlotTypeRegistry
     */
    public PlotTypeRegistry getPlotTypeRegistry() {
        return plotTypeRegistry;
    }

    /**
     * Prüft ob das Town-System aktiviert ist.
     *
     * @return true wenn aktiviert
     */
    public boolean isTownSystemEnabled() {
        return townSystemEnabled;
    }

    /**
     * Prüft ob das NPC-System aktiviert ist.
     *
     * @return true wenn aktiviert
     */
    public boolean isNpcSystemEnabled() {
        return npcSystemEnabled;
    }

    /**
     * Gibt den StorageManager zurück.
     *
     * @return StorageManager oder null
     */
    public StorageManager getStorageManager() {
        return storageManager;
    }

    /**
     * Gibt den PlotStorageProvider zurück.
     *
     * @return PlotStorageProvider oder null
     */
    public PlotStorageProvider getStorageProvider() {
        return storageProvider;
    }

    /**
     * Gibt den ChestScanService zurück (über StorageManager).
     *
     * @return ChestScanService oder null
     */
    public de.fallenstar.plot.storage.manager.ChestScanService getScanService() {
        if (storageManager != null) {
            // Reflection: hole scanService vom StorageManager
            try {
                java.lang.reflect.Field scanServiceField = storageManager.getClass().getDeclaredField("scanService");
                scanServiceField.setAccessible(true);
                return (de.fallenstar.plot.storage.manager.ChestScanService) scanServiceField.get(storageManager);
            } catch (Exception e) {
                getLogger().warning("Fehler beim Abrufen des ChestScanService: " + e.getMessage());
                return null;
            }
        }
        return null;
    }
}
