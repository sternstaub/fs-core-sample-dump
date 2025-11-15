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

        // Plot Provider - NoOp (Towny/Factions support can be added later)
        plotProvider = new NoOpPlotProvider();
        if (isPluginEnabled("TownyAdvanced")) {
            logger.info("○ Towny detected - using NoOp provider (add TownyPlotProvider implementation)");
        } else if (isPluginEnabled("Factions")) {
            logger.info("○ Factions detected - using NoOp provider (add FactionsPlotProvider implementation)");
        } else {
            logger.info("○ No plot plugin found - plot features disabled");
        }

        // Economy Provider - NoOp (Vault support can be added later)
        economyProvider = new NoOpEconomyProvider();
        if (isPluginEnabled("Vault")) {
            logger.info("○ Vault detected - using NoOp provider (add VaultEconomyProvider implementation)");
        } else {
            logger.info("○ No economy plugin found - economy features disabled");
        }

        // NPC Provider - NoOp (Citizens support can be added later)
        npcProvider = new NoOpNPCProvider();
        if (isPluginEnabled("Citizens")) {
            logger.info("○ Citizens detected - using NoOp provider (add CitizensNPCProvider implementation)");
        } else if (isPluginEnabled("ZNPCsPlus")) {
            logger.info("○ ZNPCsPlus detected - using NoOp provider (add ZNPCProvider implementation)");
        } else {
            logger.info("○ No NPC plugin found - NPC features disabled");
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

        logger.info("Provider detection completed - all providers using NoOp implementations");
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
    public EconomyProvider getEconomyProvider() { return economyProvider; }
    public NPCProvider getNpcProvider() { return npcProvider; }
    public ItemProvider getItemProvider() { return itemProvider; }
    public ChatProvider getChatProvider() { return chatProvider; }
    public NetworkProvider getNetworkProvider() { return networkProvider; }
}
