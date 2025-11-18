package de.fallenstar.npc;

import de.fallenstar.core.FallenStarCore;
import de.fallenstar.core.event.ProvidersReadyEvent;
import de.fallenstar.core.provider.NPCProvider;
import de.fallenstar.core.provider.PlotProvider;
import de.fallenstar.core.provider.TownProvider;
import de.fallenstar.core.provider.EconomyProvider;
import de.fallenstar.core.registry.AdminCommandRegistry;
import de.fallenstar.core.registry.PlotTypeRegistry;
import de.fallenstar.core.registry.ProviderRegistry;
import de.fallenstar.npc.command.NPCAdminHandler;
import de.fallenstar.npc.manager.NPCManager;
import de.fallenstar.npc.manager.GuildTraderManager;
import de.fallenstar.npc.npctype.AmbassadorNPC;
import de.fallenstar.npc.npctype.GuildTraderNPC;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * NPC-Modul - Verwaltet spezielle NPCs auf Plots.
 *
 * Funktionen:
 * - Botschafter-NPC (Teleport zu anderen Towns gegen Geld)
 * - Gildenhändler-NPC (Plot-basierter Händler)
 * - Spielerhändler-NPC (Virtueller Händler)
 * - Weltbankier-NPC (Währungsumtausch)
 *
 * Abhängigkeiten:
 * - Core Plugin (required)
 * - NPCProvider (required)
 * - TownProvider (optional für Botschafter)
 * - PlotProvider (optional für Gildenhändler)
 * - EconomyProvider (optional für Kosten)
 *
 * @author FallenStar
 * @version 1.0
 */
public class NPCModule extends JavaPlugin implements Listener {

    private ProviderRegistry providers;
    private PlotTypeRegistry plotTypeRegistry;
    private FallenStarCore corePlugin;

    private NPCManager npcManager;
    private GuildTraderManager guildTraderManager;

    private boolean npcSystemEnabled = false;
    private boolean townSystemEnabled = false;
    private boolean economySystemEnabled = false;
    private boolean plotSystemEnabled = false;

    @Override
    public void onEnable() {
        getLogger().info("=== NPC Module Starting ===");

        // Config laden/erstellen
        saveDefaultConfig();

        // Core-Plugin holen
        corePlugin = (FallenStarCore) getServer().getPluginManager().getPlugin("FallenStar-Core");
        if (corePlugin == null) {
            getLogger().severe("Core plugin not found! Disabling NPC Module.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Warte auf ProvidersReadyEvent
        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        // Speichere Config (Gildenhändler)
        if (guildTraderManager != null) {
            guildTraderManager.saveToConfig(getConfig());
            saveConfig();
            getLogger().info("GuildTrader config saved");
        }

        // Cleanup NPCs
        if (guildTraderManager != null) {
            guildTraderManager.shutdown();
        }

        if (npcManager != null) {
            npcManager.shutdown();
        }

        getLogger().info("NPC Module disabled");
    }

    /**
     * Wird gefeuert wenn alle Provider registriert sind.
     */
    @EventHandler
    public void onProvidersReady(ProvidersReadyEvent event) {
        this.providers = event.getRegistry();
        this.plotTypeRegistry = corePlugin.getPlotTypeRegistry();

        getLogger().info("Providers ready - initializing NPC Module...");

        // Registriere CitizensNPCProvider (Modul-eigene Implementierung)
        registerCitizensNPCProvider();

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
     * Registriert CitizensNPCProvider in der ProviderRegistry.
     *
     * Diese Methode ersetzt die Core-eigene Citizens-Integration.
     * Das NPCs-Modul stellt nun seine eigene CitizensNPCProvider-Implementierung bereit.
     *
     * WICHTIG: Der Provider wird auch als EventListener registriert für Click-Events!
     */
    private void registerCitizensNPCProvider() {
        // Prüfe ob Citizens verfügbar ist
        if (getServer().getPluginManager().getPlugin("Citizens") == null) {
            getLogger().info("○ Citizens nicht gefunden - verwende NoOp NPCProvider");
            return;
        }

        try {
            // Erstelle CitizensNPCProvider (Modul-eigene Implementierung)
            de.fallenstar.npc.provider.CitizensNPCProvider citizensProvider =
                new de.fallenstar.npc.provider.CitizensNPCProvider();

            // Registriere in ProviderRegistry
            providers.setNpcProvider(citizensProvider);

            // WICHTIG: Registriere als Event-Listener für NPC-Click-Events
            getServer().getPluginManager().registerEvents(citizensProvider, this);

            getLogger().info("✓ CitizensNPCProvider (NPCs-Modul) registriert + Listener aktiviert");

        } catch (Exception e) {
            getLogger().warning("✗ Fehler beim Registrieren von CitizensNPCProvider: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Prüft ob alle kritischen Provider verfügbar sind.
     * Modul wird deaktiviert wenn diese fehlen.
     */
    private boolean checkRequiredFeatures() {
        try {
            // NPCProvider ist KRITISCH
            NPCProvider npcProvider = providers.getNpcProvider();
            if (!npcProvider.isAvailable()) {
                getLogger().severe("NPCProvider nicht verfügbar - NPC-Modul benötigt NPC-Plugin!");
                return false;
            }

            npcSystemEnabled = true;
            getLogger().info("✓ NPCProvider available");

            return true;

        } catch (Exception e) {
            getLogger().severe("Fehler beim Zugriff auf Provider: " + e.getMessage());
            return false;
        }
    }

    /**
     * Prüft welche optionalen Features verfügbar sind.
     */
    private void checkOptionalFeatures() {
        // TownProvider (für Botschafter-NPC) - jetzt OPTIONAL statt kritisch
        TownProvider townProvider = providers.getTownProvider();
        if (townProvider != null && townProvider.isAvailable()) {
            townSystemEnabled = true;
            getLogger().info("✓ TownProvider enabled (Botschafter-NPC verfügbar)");
        } else {
            townSystemEnabled = false;
            getLogger().info("○ TownProvider not available - Botschafter-NPC deaktiviert");
        }

        // PlotProvider (für Gildenhändler)
        PlotProvider plotProvider = providers.getPlotProvider();
        if (plotProvider != null && plotProvider.isAvailable()) {
            plotSystemEnabled = true;
            getLogger().info("✓ PlotProvider enabled (Gildenhändler verfügbar)");
        } else {
            plotSystemEnabled = false;
            getLogger().info("○ PlotProvider not available - Gildenhändler deaktiviert");
        }

        // EconomyProvider
        EconomyProvider economyProvider = providers.getEconomyProvider();
        if (economyProvider != null && economyProvider.isAvailable()) {
            economySystemEnabled = true;
            getLogger().info("✓ EconomyProvider enabled (Teleport-Kosten verfügbar)");
        } else {
            economySystemEnabled = false;
            getLogger().info("○ EconomyProvider not available - Teleport kostenlos");
        }
    }

    /**
     * Initialisiert das Modul mit aktivierten Features.
     */
    private void initializeModule() {
        getLogger().info("Initialisiere NPC-System...");

        // NPCManager erstellen
        this.npcManager = new NPCManager(
            this,
            providers,
            plotTypeRegistry,
            getLogger()
        );

        // NPC-Typen registrieren
        registerNPCTypes();

        // Admin-Command-Handler registrieren
        registerAdminCommands();

        // Initialer Status-Log
        getLogger().info("=== NPC Module Initialized ===");
        getLogger().info("  NPC-System: " + (npcSystemEnabled ? "enabled" : "disabled"));
        getLogger().info("  Town-System: " + (townSystemEnabled ? "enabled" : "disabled"));
        getLogger().info("  Economy-System: " + (economySystemEnabled ? "enabled" : "disabled"));
        getLogger().info("  Plot-System: " + (plotSystemEnabled ? "enabled" : "disabled"));
        getLogger().info("  Registered NPC Types: Ambassador, GuildTrader");
    }

    /**
     * Registriert alle NPC-Typen.
     */
    private void registerNPCTypes() {
        // Botschafter-NPC registrieren (nur wenn TownProvider verfügbar)
        if (townSystemEnabled) {
            AmbassadorNPC ambassadorNPC = new AmbassadorNPC(
                npcManager,
                providers.getTownProvider(),
                providers.getEconomyProvider(),
                economySystemEnabled,
                getConfig()
            );

            npcManager.registerNPCType("ambassador", ambassadorNPC);
            getLogger().info("✓ Ambassador NPC registered");
        } else {
            getLogger().warning("○ Ambassador NPC skipped (TownProvider not available)");
        }

        // Gildenhändler-NPC registrieren (nur wenn PlotProvider verfügbar)
        if (plotSystemEnabled) {
            GuildTraderNPC guildTraderNPC = new GuildTraderNPC(
                npcManager,
                providers.getPlotProvider(),
                providers,
                getConfig()
            );

            npcManager.registerNPCType("guildtrader", guildTraderNPC);

            // GuildTraderManager erstellen
            this.guildTraderManager = new GuildTraderManager(
                npcManager,
                providers.getPlotProvider(),
                guildTraderNPC,
                providers,
                getLogger()
            );

            // Config laden
            guildTraderManager.loadFromConfig(getConfig());

            getLogger().info("✓ GuildTrader NPC registered (" + guildTraderManager.getGuildTraderCount() + " loaded)");
        } else {
            getLogger().warning("○ GuildTrader NPC skipped (PlotProvider not available)");
        }

        // TODO: Banker-NPC registrieren
        // BankerNPC bankerNPC = new BankerNPC(...);
        // npcManager.registerNPCType("banker", bankerNPC);
    }

    /**
     * Registriert Admin-Command-Handler.
     */
    private void registerAdminCommands() {
        AdminCommandRegistry registry = corePlugin.getAdminCommandRegistry();
        if (registry == null) {
            getLogger().warning("AdminCommandRegistry not available - commands disabled");
            return;
        }

        // NPC-Command-Handler registrieren
        NPCAdminHandler npcHandler = new NPCAdminHandler(
            npcManager,
            guildTraderManager,
            providers
        );

        registry.registerHandler("npc", npcHandler);
        getLogger().info("✓ Registered /fscore npc command handler");
    }

    /**
     * Gibt den NPCManager zurück.
     *
     * @return NPCManager
     */
    public NPCManager getNPCManager() {
        return npcManager;
    }

    /**
     * Gibt den GuildTraderManager zurück.
     *
     * @return GuildTraderManager oder null wenn nicht aktiviert
     */
    public GuildTraderManager getGuildTraderManager() {
        return guildTraderManager;
    }

    /**
     * Prüft ob Economy-System aktiviert ist.
     *
     * @return true wenn aktiviert
     */
    public boolean isEconomySystemEnabled() {
        return economySystemEnabled;
    }

    /**
     * Prüft ob Plot-System aktiviert ist.
     *
     * @return true wenn aktiviert
     */
    public boolean isPlotSystemEnabled() {
        return plotSystemEnabled;
    }
}
