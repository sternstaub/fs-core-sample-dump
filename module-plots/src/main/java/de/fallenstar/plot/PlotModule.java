package de.fallenstar.plot;

import com.palmergames.bukkit.towny.TownyCommandAddonAPI;
import com.palmergames.bukkit.towny.TownyCommandAddonAPI.CommandType;
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
import de.fallenstar.plot.npc.manager.NPCManager;
import de.fallenstar.plot.slot.PlotSlotManager;
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
    private PlotSlotManager plotSlotManager;
    private NPCManager npcManager;
    private de.fallenstar.plot.manager.PlotNameManager plotNameManager;
    private de.fallenstar.plot.manager.PlotPriceManager plotPriceManager;
    private de.fallenstar.plot.manager.PlotBoundNPCRegistry npcRegistry;
    private de.fallenstar.plot.command.PlotCommand plotCommand;
    private de.fallenstar.plot.registry.PlotRegistry plotRegistry;
    private de.fallenstar.plot.factory.TradeguildPlotFactory tradeguildPlotFactory;

    private boolean plotSystemEnabled = false;
    private boolean townSystemEnabled = false;
    private boolean npcSystemEnabled = false;
    private boolean storageSystemEnabled = false;
    private boolean slotSystemEnabled = false;

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

        // PlotNameManager initialisieren
        this.plotNameManager = new de.fallenstar.plot.manager.PlotNameManager(getLogger());
        this.plotNameManager.loadFromConfig(getConfig());

        // Warte auf ProvidersReadyEvent
        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        // Speichere Slot-Daten und NPC-Daten
        if ((slotSystemEnabled && plotSlotManager != null) || (npcSystemEnabled && npcManager != null)) {
            saveConfiguration();
        }

        getLogger().info("Plot Module disabled");
    }

    /**
     * Speichert die Config auf Festplatte.
     *
     * MUSS nach JEDER Daten-Änderung aufgerufen werden!
     */
    public void saveConfiguration() {
        if (plotSlotManager != null) {
            plotSlotManager.saveToConfig(getConfig());
        }
        if (npcManager != null) {
            npcManager.saveToConfig(getConfig());
        }
        if (plotNameManager != null) {
            plotNameManager.saveToConfig(getConfig());
        }
        if (plotRegistry != null) {
            plotRegistry.saveToConfig(getConfig());
        }
        if (plotPriceManager != null) {
            plotPriceManager.saveToConfig(getConfig());
        }
        if (npcRegistry != null) {
            npcRegistry.saveToConfig(getConfig());
        }
        saveConfig();
        getLogger().fine("Config gespeichert");
    }

    /**
     * Wird gefeuert wenn alle Provider registriert sind.
     */
    @EventHandler
    public void onProvidersReady(ProvidersReadyEvent event) {
        this.providers = event.getRegistry();
        this.plotTypeRegistry = corePlugin.getPlotTypeRegistry();

        getLogger().info("Providers ready - initializing Plot Module...");

        // Registriere TownyPlotProvider (Modul-eigene Implementierung)
        registerTownyPlotProvider();

        // Registriere TownyTownProvider (Modul-eigene Implementierung)
        registerTownyTownProvider();

        // KRITISCHE Features prüfen (required)
        if (!checkRequiredFeatures()) {
            getLogger().severe("Required providers not available!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // OPTIONALE Features prüfen
        checkOptionalFeatures();

        // Plot-Registry initialisieren
        initializePlotRegistry();

        // Plot-Preis-Manager initialisieren
        initializePriceManager();

        // NPC-Registry initialisieren
        initializeNPCRegistry();

        // Storage-System initialisieren (jetzt Teil des Plot-Moduls)
        initializeStorageSystem();

        // Plot-Slot-System initialisieren
        initializeSlotSystem();

        // NPC-System initialisieren
        initializeNPCSystem();

        // Module vollständig initialisieren
        initializeModule();
    }

    /**
     * Registriert TownyPlotProvider in der ProviderRegistry.
     *
     * Diese Methode ersetzt die Core-eigene Towny-Integration.
     * Das Plots-Modul stellt nun seine eigene TownyPlotProvider-Implementierung bereit.
     */
    private void registerTownyPlotProvider() {
        // Prüfe ob Towny verfügbar ist
        if (getServer().getPluginManager().getPlugin("Towny") == null &&
            getServer().getPluginManager().getPlugin("TownyAdvanced") == null) {
            getLogger().info("○ Towny nicht gefunden - verwende NoOp PlotProvider");
            return;
        }

        try {
            // Erstelle TownyPlotProvider (Modul-eigene Implementierung)
            de.fallenstar.plot.provider.TownyPlotProvider townyProvider =
                new de.fallenstar.plot.provider.TownyPlotProvider();

            // Registriere in ProviderRegistry
            providers.setPlotProvider(townyProvider);

            getLogger().info("✓ TownyPlotProvider (Plots-Modul) registriert");

        } catch (Exception e) {
            getLogger().warning("✗ Fehler beim Registrieren von TownyPlotProvider: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Registriert TownyTownProvider in der ProviderRegistry.
     *
     * Diese Methode ersetzt die Core-eigene Towny-Town-Integration.
     * Das Plots-Modul stellt nun seine eigene TownyTownProvider-Implementierung bereit.
     */
    private void registerTownyTownProvider() {
        // Prüfe ob Towny verfügbar ist
        if (getServer().getPluginManager().getPlugin("Towny") == null &&
            getServer().getPluginManager().getPlugin("TownyAdvanced") == null) {
            getLogger().info("○ Towny nicht gefunden - verwende NoOp TownProvider");
            return;
        }

        try {
            // Erstelle TownyTownProvider (Modul-eigene Implementierung)
            de.fallenstar.plot.provider.TownyTownProvider townyProvider =
                new de.fallenstar.plot.provider.TownyTownProvider();

            // Registriere in ProviderRegistry
            providers.setTownProvider(townyProvider);

            getLogger().info("✓ TownyTownProvider (Plots-Modul) registriert");

        } catch (Exception e) {
            getLogger().warning("✗ Fehler beim Registrieren von TownyTownProvider: " + e.getMessage());
            e.printStackTrace();
        }
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
     * Initialisiert das Plot-Slot-System.
     */
    private void initializeSlotSystem() {
        try {
            this.plotSlotManager = new PlotSlotManager(getLogger());
            this.plotSlotManager.loadFromConfig(getConfig());

            slotSystemEnabled = true;
            getLogger().info("✓ Plot-Slot-System aktiviert");
            getLogger().info("  Market-Plots: " + plotSlotManager.getMarketPlotCount() + " geladen");
            getLogger().info("  Initial Slots: " + plotSlotManager.getInitialSlots());
            getLogger().info("  Max Slots: " + plotSlotManager.getMaxSlots());

        } catch (Exception e) {
            slotSystemEnabled = false;
            getLogger().warning("✗ Plot-Slot-System konnte nicht initialisiert werden: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Initialisiert das NPC-System.
     */
    private void initializeNPCSystem() {
        // Prüfe ob NPC-System in Config aktiviert ist
        boolean npcEnabled = getConfig().getBoolean("npc.enabled", true);

        if (!npcEnabled) {
            npcSystemEnabled = false;
            getLogger().info("○ NPC-System deaktiviert (Config)");
            return;
        }

        try {
            this.npcManager = new NPCManager(getLogger());
            this.npcManager.loadFromConfig(getConfig());

            npcSystemEnabled = true;
            getLogger().info("✓ NPC-System aktiviert");
            getLogger().info("  NPCs geladen: " + npcManager.getNPCCount());

        } catch (Exception e) {
            npcSystemEnabled = false;
            getLogger().warning("✗ NPC-System konnte nicht initialisiert werden: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Initialisiert die Plot-Registry.
     */
    private void initializePlotRegistry() {
        try {
            this.plotRegistry = new de.fallenstar.plot.registry.PlotRegistry(getLogger());
            this.plotRegistry.loadFromConfig(getConfig());

            getLogger().info("✓ PlotRegistry initialisiert");
            getLogger().info("  Registrierte Handelsgilden: " + plotRegistry.getPlotIdsByType(de.fallenstar.plot.registry.PlotRegistry.PlotType.MERCHANT_GUILD).size());

            // Registriere PlotRegistryListener für Auto-Updates
            de.fallenstar.plot.registry.PlotRegistryListener registryListener =
                new de.fallenstar.plot.registry.PlotRegistryListener(
                    this,
                    plotRegistry
                );
            getServer().getPluginManager().registerEvents(registryListener, this);
            getLogger().info("✓ PlotRegistryListener registriert");

        } catch (Exception e) {
            getLogger().warning("✗ PlotRegistry konnte nicht initialisiert werden: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Initialisiert den PlotPriceManager.
     */
    private void initializePriceManager() {
        try {
            this.plotPriceManager = new de.fallenstar.plot.manager.PlotPriceManager(this, getLogger());
            this.plotPriceManager.loadFromConfig(getConfig());

            getLogger().info("✓ PlotPriceManager initialisiert");
            getLogger().info("  Preise für " + plotPriceManager.getPlotCount() + " Plots geladen");

        } catch (Exception e) {
            getLogger().warning("✗ PlotPriceManager konnte nicht initialisiert werden: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Initialisiert die PlotBoundNPCRegistry.
     */
    private void initializeNPCRegistry() {
        try {
            this.npcRegistry = new de.fallenstar.plot.manager.PlotBoundNPCRegistry(this, getLogger());
            this.npcRegistry.loadFromConfig(getConfig());

            getLogger().info("✓ PlotBoundNPCRegistry initialisiert");
            getLogger().info("  " + npcRegistry.getNPCCount() + " NPCs auf " + npcRegistry.getPlotCount() + " Plots geladen");

        } catch (Exception e) {
            getLogger().warning("✗ PlotBoundNPCRegistry konnte nicht initialisiert werden: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Initialisiert das Storage-System (integriert im Plot-Modul).
     */
    private void initializeStorageSystem() {
        PlotProvider plotProvider = providers.getPlotProvider();

        if (plotProvider.isAvailable()) {
            try {
                // Erstelle ChestScanService zuerst (benötigt von PlotStorageProvider und StorageManager)
                ChestScanService scanService = new ChestScanService(getLogger(), plotProvider);

                // Erstelle Storage-Provider mit ScanService für Auto-Scans
                this.storageProvider = new PlotStorageProvider(scanService, getLogger());

                // Erstelle StorageManager mit geteiltem ScanService
                this.storageManager = new StorageManager(
                    getLogger(),
                    plotProvider,
                    storageProvider,
                    scanService
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
                getLogger().info("✓ Storage-System aktiviert (mit Auto-Scan)");

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

        // Initialisiere TradeguildPlotFactory
        initializeTradeguildPlotFactory();

        // Lade alle Tradeguild-Plots und registriere in InteractionRegistry
        loadTradeguildPlots();

        // Registriere Commands
        registerCommands();
        registerAdminCommands();

        // Registriere Listener
        registerListeners();

        // Initialer Status-Log
        getLogger().info("=== Plot Module Initialized ===");
        getLogger().info("  Plot-System: " + (plotSystemEnabled ? "enabled" : "disabled"));
        getLogger().info("  Town-System: " + (townSystemEnabled ? "enabled" : "disabled"));
        getLogger().info("  NPC-System: " + (npcSystemEnabled ? "enabled" : "disabled"));
        getLogger().info("  Storage-System: " + (storageSystemEnabled ? "enabled" : "disabled"));
        getLogger().info("  Slot-System: " + (slotSystemEnabled ? "enabled" : "disabled"));
        getLogger().info("  Commands: /plot info, /plot storage, /plot npc, /plot gui, /plot price, /plot slots");
    }

    /**
     * Registriert alle Listener.
     */
    private void registerListeners() {
        // Registriere PriceSetListener (für Handelsgilde-Preisverwaltung)
        if (plotCommand != null) {
            de.fallenstar.plot.listener.PriceSetListener priceSetListener =
                new de.fallenstar.plot.listener.PriceSetListener(
                    getLogger(),
                    providers,
                    plotCommand.getPriceCommand()
                );

            getServer().getPluginManager().registerEvents(priceSetListener, this);
            getLogger().info("✓ PriceSetListener registriert");
        } else {
            getLogger().warning("✗ PlotCommand nicht verfügbar - PriceSetListener konnte nicht registriert werden");
        }
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
     * Registriert alle Commands über Towny Command Addon API.
     *
     * Unsere Commands werden als Subcommands zu Townys /plot hinzugefügt:
     * - /plot info - Plot-Informationen
     * - /plot gui - Plot-GUI (typ-abhängig)
     * - /plot price - Preisverwaltung (Handelsgilde)
     * - /plot storage - Storage-Verwaltung
     * - /plot npc - NPC-Verwaltung
     * - /plot slots - Händler-Slots Verwaltung (Market)
     */
    private void registerCommands() {
        this.plotCommand = new de.fallenstar.plot.command.PlotCommand(
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

        // Registriere Subcommands über Towny API
        // Jeder Command erhält einen eigenen Wrapper
        try {
            // /plot info
            TownyCommandAddonAPI.addSubCommand(CommandType.PLOT, "info", (sender, cmd, label, args) -> {
                String[] newArgs = new String[args.length + 1];
                newArgs[0] = "info";
                System.arraycopy(args, 0, newArgs, 1, args.length);
                return plotCommand.onCommand(sender, cmd, "plot", newArgs);
            });

            // /plot gui
            TownyCommandAddonAPI.addSubCommand(CommandType.PLOT, "gui", (sender, cmd, label, args) -> {
                String[] newArgs = new String[args.length + 1];
                newArgs[0] = "gui";
                System.arraycopy(args, 0, newArgs, 1, args.length);
                return plotCommand.onCommand(sender, cmd, "plot", newArgs);
            });

            // /plot price
            TownyCommandAddonAPI.addSubCommand(CommandType.PLOT, "price", (sender, cmd, label, args) -> {
                String[] newArgs = new String[args.length + 1];
                newArgs[0] = "price";
                System.arraycopy(args, 0, newArgs, 1, args.length);
                return plotCommand.onCommand(sender, cmd, "plot", newArgs);
            });

            // /plot storage (optional)
            if (storageSystemEnabled) {
                TownyCommandAddonAPI.addSubCommand(CommandType.PLOT, "storage", (sender, cmd, label, args) -> {
                    String[] newArgs = new String[args.length + 1];
                    newArgs[0] = "storage";
                    System.arraycopy(args, 0, newArgs, 1, args.length);
                    return plotCommand.onCommand(sender, cmd, "plot", newArgs);
                });
            }

            // /plot npc (optional)
            if (npcSystemEnabled) {
                TownyCommandAddonAPI.addSubCommand(CommandType.PLOT, "npc", (sender, cmd, label, args) -> {
                    String[] newArgs = new String[args.length + 1];
                    newArgs[0] = "npc";
                    System.arraycopy(args, 0, newArgs, 1, args.length);
                    return plotCommand.onCommand(sender, cmd, "plot", newArgs);
                });
            }

            // /plot slots (optional)
            if (slotSystemEnabled) {
                TownyCommandAddonAPI.addSubCommand(CommandType.PLOT, "slots", (sender, cmd, label, args) -> {
                    String[] newArgs = new String[args.length + 1];
                    newArgs[0] = "slots";
                    System.arraycopy(args, 0, newArgs, 1, args.length);
                    return plotCommand.onCommand(sender, cmd, "plot", newArgs);
                });
            }

            // /plot name (Plot-Namen-Verwaltung)
            TownyCommandAddonAPI.addSubCommand(CommandType.PLOT, "name", (sender, cmd, label, args) -> {
                String[] newArgs = new String[args.length + 1];
                newArgs[0] = "name";
                System.arraycopy(args, 0, newArgs, 1, args.length);
                return plotCommand.onCommand(sender, cmd, "plot", newArgs);
            });

            getLogger().info("✓ Commands über Towny API registriert");
            getLogger().info("  Verfügbar: /plot info, /plot gui, /plot name, /plot price" +
                (storageSystemEnabled ? ", /plot storage" : "") +
                (npcSystemEnabled ? ", /plot npc" : "") +
                (slotSystemEnabled ? ", /plot slots" : ""));
        } catch (Exception e) {
            getLogger().severe("Fehler beim Registrieren der Towny-Commands: " + e.getMessage());
            e.printStackTrace();
        }
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
     * Gibt den NPCManager zurück.
     *
     * @return NPCManager oder null
     */
    public NPCManager getNPCManager() {
        return npcManager;
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

    /**
     * Gibt den PlotSlotManager zurück.
     *
     * @return PlotSlotManager oder null
     */
    public PlotSlotManager getPlotSlotManager() {
        return plotSlotManager;
    }

    /**
     * Gibt die PlotRegistry zurück.
     *
     * @return PlotRegistry oder null
     */
    public de.fallenstar.plot.registry.PlotRegistry getPlotRegistry() {
        return plotRegistry;
    }

    /**
     * Prüft ob das Slot-System aktiviert ist.
     *
     * @return true wenn aktiviert
     */
    public boolean isSlotSystemEnabled() {
        return slotSystemEnabled;
    }

    /**
     * Gibt den PlotNameManager zurück.
     *
     * @return PlotNameManager oder null
     */
    public de.fallenstar.plot.manager.PlotNameManager getPlotNameManager() {
        return plotNameManager;
    }

    /**
     * Gibt den PlotPriceManager zurück.
     *
     * @return PlotPriceManager oder null
     */
    public de.fallenstar.plot.manager.PlotPriceManager getPriceManager() {
        return plotPriceManager;
    }

    /**
     * Gibt die PlotBoundNPCRegistry zurück.
     *
     * @return PlotBoundNPCRegistry oder null
     */
    public de.fallenstar.plot.manager.PlotBoundNPCRegistry getNPCRegistry() {
        return npcRegistry;
    }

    /**
     * Initialisiert die TradeguildPlotFactory.
     */
    private void initializeTradeguildPlotFactory() {
        this.tradeguildPlotFactory = new de.fallenstar.plot.factory.TradeguildPlotFactory(getLogger());
        getLogger().info("✓ TradeguildPlotFactory initialized");
    }

    /**
     * Lädt alle Tradeguild-Plots und registriert sie in der InteractionRegistry.
     *
     * Ablauf:
     * 1. Hole alle MERCHANT_GUILD Plots aus PlotRegistry
     * 2. Für jeden Plot: Erstelle TradeguildPlot via Factory
     * 3. Registriere in InteractionRegistry
     */
    private void loadTradeguildPlots() {
        if (plotRegistry == null) {
            getLogger().warning("PlotRegistry nicht verfügbar - überspringe TradeguildPlot-Loading");
            return;
        }

        if (!plotSystemEnabled) {
            getLogger().info("Plot-System nicht verfügbar - überspringe TradeguildPlot-Loading");
            return;
        }

        try {
            // Hole InteractionRegistry von Core
            de.fallenstar.core.interaction.handler.InteractionRegistry interactionRegistry =
                corePlugin.getInteractionRegistry();

            if (interactionRegistry == null) {
                getLogger().warning("InteractionRegistry nicht verfügbar - überspringe TradeguildPlot-Loading");
                return;
            }

            // Hole PlotProvider
            de.fallenstar.core.provider.PlotProvider plotProvider = providers.getPlotProvider();
            if (plotProvider == null || !plotProvider.isAvailable()) {
                getLogger().warning("PlotProvider nicht verfügbar - überspringe TradeguildPlot-Loading");
                return;
            }

            // Hole alle MERCHANT_GUILD Plot-IDs
            java.util.List<String> merchantGuildPlotIds =
                plotRegistry.getPlotIdentifiers(de.fallenstar.plot.registry.PlotRegistry.PlotType.MERCHANT_GUILD);

            getLogger().info("Lade " + merchantGuildPlotIds.size() + " Tradeguild-Plots...");

            int loaded = 0;
            int failed = 0;

            for (String plotId : merchantGuildPlotIds) {
                try {
                    // Hole BasePlot via PlotProvider
                    // HINWEIS: Wir brauchen eine Location um getPlot() aufzurufen
                    // Da PlotRegistry nur IDs speichert, müssen wir eine andere Methode finden
                    // TODO: PlotRegistry sollte Locations oder Plot-Objekte cachen

                    // WORKAROUND: Überspringen bis PlotRegistry erweitert wurde
                    getLogger().fine("Plot " + plotId + " - Location-Lookup noch nicht implementiert");

                } catch (Exception e) {
                    getLogger().warning("Fehler beim Laden von TradeguildPlot " + plotId + ": " + e.getMessage());
                    failed++;
                }
            }

            getLogger().info("✓ TradeguildPlot-Loading abgeschlossen: " + loaded + " geladen, " + failed + " fehlgeschlagen");

            if (failed > 0) {
                getLogger().warning("HINWEIS: PlotRegistry muss erweitert werden um Locations zu speichern");
                getLogger().warning("        Aktuell können TradeguildPlots noch nicht automatisch geladen werden");
                getLogger().warning("        → Implementiere PlotRegistry.getPlotLocation(String plotId)");
            }

        } catch (Exception e) {
            getLogger().severe("Fehler beim Laden der TradeguildPlots: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Gibt die TradeguildPlotFactory zurück.
     *
     * @return TradeguildPlotFactory oder null
     */
    public de.fallenstar.plot.factory.TradeguildPlotFactory getTradeguildPlotFactory() {
        return tradeguildPlotFactory;
    }
}
