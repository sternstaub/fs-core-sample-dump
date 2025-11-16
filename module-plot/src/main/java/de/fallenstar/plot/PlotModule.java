package de.fallenstar.plot;

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

    private boolean plotSystemEnabled = false;
    private boolean townSystemEnabled = false;
    private boolean npcSystemEnabled = false;

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
        getLogger().info("  Commands: /plot info, /plot storage, /plot npc");
    }

    /**
     * Event-Handler für Towny TownBlockTypeRegisterEvent.
     * Registriert den Custom-Plot-Typ "botschaft".
     */
    @EventHandler
    public void onTownBlockTypeRegister(org.bukkit.event.Event event) {
        // Prüfe ob es das richtige Event ist (via Reflection für optionale Dependency)
        if (!event.getClass().getName().equals("com.palmergames.bukkit.towny.event.TownBlockTypeRegisterEvent")) {
            return;
        }

        try {
            // Erstelle TownBlockType für "botschaft"
            Class<?> townBlockTypeClass = Class.forName("com.palmergames.bukkit.towny.object.TownBlockType");

            // TownBlockType(name, formattedName)
            Object botschaftType = townBlockTypeClass
                .getConstructor(String.class, String.class)
                .newInstance("botschaft", "Botschaft");

            // Registriere über TownBlockTypeHandler.registerType()
            Class<?> handlerClass = Class.forName("com.palmergames.bukkit.towny.object.TownBlockTypeHandler");
            handlerClass.getMethod("registerType", townBlockTypeClass)
                .invoke(null, botschaftType);

            getLogger().info("✓ Custom-Plot-Typ 'botschaft' in Towny registriert");

        } catch (Exception e) {
            getLogger().warning("✗ Fehler beim Registrieren von 'botschaft': " + e.getMessage());
        }
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
            npcSystemEnabled
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
}
