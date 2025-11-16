package de.fallenstar.core.registry;

import de.fallenstar.core.provider.*;
import de.fallenstar.core.provider.impl.*;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import java.util.logging.Logger;

/**
 * Zentrale Registry für alle Provider.
 *
 * Verantwortlich für:
 * - Auto-Detection verfügbarer Plugins
 * - Registrierung entsprechender Provider
 * - Bereitstellung von Provider-Instanzen für Module
 *
 * Pattern: Service Locator + Dependency Injection
 *
 * @author FallenStar
 * @version 1.0
 */
public class ProviderRegistry {

    private final Logger logger;
    private final Plugin plugin;

    private PlotProvider plotProvider;
    private TownProvider townProvider;
    private EconomyProvider economyProvider;
    private NPCProvider npcProvider;
    private ItemProvider itemProvider;
    private ChatProvider chatProvider;
    private NetworkProvider networkProvider;

    /**
     * Erstellt eine neue ProviderRegistry.
     *
     * @param plugin Plugin-Instanz
     * @param logger Logger für Debug-Ausgaben
     */
    public ProviderRegistry(Plugin plugin, Logger logger) {
        this.plugin = plugin;
        this.logger = logger;
    }
    
    /**
     * Erkennt verfügbare Plugins und registriert entsprechende Provider.
     *
     * Wird beim Server-Start vom Core-Plugin aufgerufen.
     *
     * NOTE: Currently all providers use NoOp implementations.
     * Real implementations (Towny, Vault, Citizens) can be added
     * when their JAR files are available at runtime.
     */
    public void detectAndRegister() {
        logger.info("Detecting available providers...");

        // Plot Provider - Towny/Factions support
        if (isPluginEnabled("Towny") || isPluginEnabled("TownyAdvanced")) {
            try {
                plotProvider = new TownyPlotProvider();
                logger.info("✓ Towny detected - TownyPlotProvider registered");
            } catch (Exception e) {
                logger.warning("✗ Towny found but failed to initialize PlotProvider: " + e.getMessage());
                plotProvider = new NoOpPlotProvider();
            }
        } else if (isPluginEnabled("Factions")) {
            plotProvider = new NoOpPlotProvider();
            logger.info("○ Factions detected - using NoOp provider (add FactionsPlotProvider implementation)");
        } else {
            plotProvider = new NoOpPlotProvider();
            logger.info("○ No plot plugin found - plot features disabled");
        }

        // Town Provider - Towny support
        if (isPluginEnabled("Towny") || isPluginEnabled("TownyAdvanced")) {
            try {
                townProvider = new TownyTownProvider();
                logger.info("✓ Towny detected - TownyTownProvider registered");
            } catch (Exception e) {
                logger.warning("✗ Towny found but failed to initialize TownProvider: " + e.getMessage());
                townProvider = new NoOpTownProvider();
            }
        } else {
            townProvider = new NoOpTownProvider();
            logger.info("○ No town plugin found - town features disabled");
        }

        // Economy Provider - NoOp (Vault support can be added later)
        economyProvider = new NoOpEconomyProvider();
        if (isPluginEnabled("Vault")) {
            logger.info("○ Vault detected - using NoOp provider (add VaultEconomyProvider implementation)");
        } else {
            logger.info("○ No economy plugin found - economy features disabled");
        }

        // NPC Provider - Citizens oder NoOp
        if (isPluginEnabled("Citizens")) {
            try {
                CitizensNPCProvider citizensProvider = new CitizensNPCProvider();
                npcProvider = citizensProvider;

                // Registriere als Event-Listener für Click-Events
                Bukkit.getPluginManager().registerEvents(citizensProvider, plugin);

                logger.info("✓ Citizens detected - CitizensNPCProvider registered");
            } catch (Exception e) {
                logger.warning("✗ Citizens found but failed to initialize: " + e.getMessage());
                npcProvider = new NoOpNPCProvider();
            }
        } else if (isPluginEnabled("ZNPCsPlus")) {
            logger.info("○ ZNPCsPlus detected - using NoOp provider (add ZNPCProvider implementation)");
            npcProvider = new NoOpNPCProvider();
        } else {
            logger.info("○ No NPC plugin found - NPC features disabled");
            npcProvider = new NoOpNPCProvider();
        }

        // Item Provider - NoOp
        itemProvider = new NoOpItemProvider();
        if (isPluginEnabled("MMOItems")) {
            logger.info("○ MMOItems detected - using NoOp provider (add MMOItemsProvider implementation)");
        } else {
            logger.info("○ No custom item plugin - using vanilla items only");
        }

        // Chat Provider - NoOp
        chatProvider = new NoOpChatProvider();
        logger.info("○ Chat provider: NoOp (external chat integration disabled)");

        // Network Provider - NoOp
        networkProvider = new NoOpNetworkProvider();
        logger.info("○ Network provider: NoOp (standalone server mode)");

        logger.info("Provider detection completed");
    }
    
    /**
     * Hilfsmethode: Prüft ob ein Plugin geladen und aktiviert ist.
     */
    private boolean isPluginEnabled(String pluginName) {
        Plugin plugin = Bukkit.getPluginManager().getPlugin(pluginName);
        return plugin != null && plugin.isEnabled();
    }
    
    // Getter für Provider
    public PlotProvider getPlotProvider() { return plotProvider; }
    public TownProvider getTownProvider() { return townProvider; }
    public EconomyProvider getEconomyProvider() { return economyProvider; }
    public NPCProvider getNpcProvider() { return npcProvider; }
    public ItemProvider getItemProvider() { return itemProvider; }
    public ChatProvider getChatProvider() { return chatProvider; }
    public NetworkProvider getNetworkProvider() { return networkProvider; }
}
