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
import de.fallenstar.core.registry.PlotTypeRegistry;
import de.fallenstar.core.registry.ProviderRegistry;
import de.fallenstar.plot.command.PlotCommand;
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
    private org.bukkit.plugin.Plugin storagePlugin;

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

        // Storage-Plugin holen
        checkStoragePlugin();

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
     * Prüft ob Storage-Plugin verfügbar ist.
     */
    private void checkStoragePlugin() {
        storagePlugin = getServer().getPluginManager().getPlugin("FallenStar-Storage");
        if (storagePlugin != null && storagePlugin.isEnabled()) {
            storageSystemEnabled = true;
            getLogger().info("✓ Storage-Plugin gefunden - /plot storage verfügbar");
        } else {
            storageSystemEnabled = false;
            getLogger().info("○ Storage-Plugin nicht gefunden - /plot storage deaktiviert");
        }
    }

    /**
     * Initialisiert das Modul mit aktivierten Features.
     */
    private void initializeModule() {
        getLogger().info("Initialisiere Plot-System...");

        // Registriere Commands
        registerCommands();

        // Initialer Status-Log
        getLogger().info("=== Plot Module Initialized ===");
        getLogger().info("  Plot-System: " + (plotSystemEnabled ? "enabled" : "disabled"));
        getLogger().info("  Town-System: " + (townSystemEnabled ? "enabled" : "disabled"));
        getLogger().info("  NPC-System: " + (npcSystemEnabled ? "enabled" : "disabled"));
        getLogger().info("  Storage-System: " + (storageSystemEnabled ? "enabled" : "disabled"));
        getLogger().info("  Commands: /plot info, /plot storage, /plot npc");
    }

    /**
     * Registriert den Custom-Plot-Typ "botschaft" in Towny.
     *
     * Diese Methode wird in onLoad() aufgerufen und auch bei Towny-Reloads
     * via TownBlockTypeRegisterEvent.
     */
    private void registerCustomPlotType() {
        // Prüfe ob der Typ bereits existiert
        if (TownBlockTypeHandler.exists("botschaft")) {
            return;
        }

        // Erstelle TownBlockType mit Custom TownBlockData
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
            storageSystemEnabled
        );

        getCommand("plot").setExecutor(plotCommand);
        getCommand("plot").setTabCompleter(plotCommand);

        getLogger().info("✓ Commands registriert");
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
     * Gibt das Storage-Plugin zurück.
     *
     * @return Storage-Plugin oder null
     */
    public org.bukkit.plugin.Plugin getStoragePlugin() {
        return storagePlugin;
    }
}
