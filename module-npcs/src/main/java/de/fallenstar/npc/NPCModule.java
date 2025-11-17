package de.fallenstar.npc;

import de.fallenstar.core.FallenStarCore;
import de.fallenstar.core.event.ProvidersReadyEvent;
import de.fallenstar.core.provider.NPCProvider;
import de.fallenstar.core.provider.PlotProvider;
import de.fallenstar.core.provider.TownProvider;
import de.fallenstar.core.provider.EconomyProvider;
import de.fallenstar.core.registry.PlotTypeRegistry;
import de.fallenstar.core.registry.ProviderRegistry;
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
 * - Banker-NPC (Geldmünzen auszahlen) - TODO
 * - Event-basierte NPC-Konfiguration
 *
 * Abhängigkeiten:
 * - Core Plugin (required)
 * - NPCProvider (required)
 * - TownProvider (required für Botschafter)
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
            // NPCProvider ist KRITISCH
            NPCProvider npcProvider = providers.getNpcProvider();
            if (!npcProvider.isAvailable()) {
                getLogger().severe("NPCProvider nicht verfügbar - NPC-Modul benötigt NPC-Plugin!");
                return false;
            }

            npcSystemEnabled = true;
            getLogger().info("✓ NPCProvider available");

            // TownProvider ist KRITISCH für Botschafter
            TownProvider townProvider = providers.getTownProvider();
            if (!townProvider.isAvailable()) {
                getLogger().severe("TownProvider nicht verfügbar - benötigt für Botschafter-NPC!");
                return false;
            }

            townSystemEnabled = true;
            getLogger().info("✓ TownProvider available");

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

        // Initialer Status-Log
        getLogger().info("=== NPC Module Initialized ===");
        getLogger().info("  NPC-System: " + (npcSystemEnabled ? "enabled" : "disabled"));
        getLogger().info("  Town-System: " + (townSystemEnabled ? "enabled" : "disabled"));
        getLogger().info("  Economy-System: " + (economySystemEnabled ? "enabled" : "disabled"));
        getLogger().info("  Registered NPC Types: Ambassador");
    }

    /**
     * Registriert alle NPC-Typen.
     */
    private void registerNPCTypes() {
        // Botschafter-NPC registrieren
        AmbassadorNPC ambassadorNPC = new AmbassadorNPC(
            npcManager,
            providers.getTownProvider(),
            providers.getEconomyProvider(),
            economySystemEnabled,
            getConfig()
        );

        npcManager.registerNPCType("ambassador", ambassadorNPC);
        getLogger().info("✓ Ambassador NPC registered");

        // Gildenhändler-NPC registrieren (nur wenn PlotProvider verfügbar)
        if (plotSystemEnabled) {
            GuildTraderNPC guildTraderNPC = new GuildTraderNPC(
                npcManager,
                providers.getPlotProvider(),
                getConfig()
            );

            npcManager.registerNPCType("guildtrader", guildTraderNPC);

            // GuildTraderManager erstellen
            this.guildTraderManager = new GuildTraderManager(
                npcManager,
                providers.getPlotProvider(),
                guildTraderNPC,
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
